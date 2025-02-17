/* Copyright (C) 2001-2008 by Peter Eastman
   Changes copyright (C) 2020-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.*;
import artofillusion.ui.*;
import static artofillusion.ui.UIUtilities.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This is a graph used for displaying and editing one or more Track's keyframe values.
 */
public class TrackGraph extends CustomWidget implements TrackDisplay {

    private final LayoutWindow window;
    private double hScale;
    private double vScale;
    private double hStart;
    private double vStart;
    private double[] dragKeyTime;
    private double[][] dragKeyValue;
    private int subdivisions, mode, oldHeight, effectiveMode;
    private final Score score;
    private final VerticalAxis vertAxis;
    private Point lastPos, dragPos;
    private Rectangle lastBounds;
    private boolean draggingBox, lineAtBottom;
    private final List<Marker> markers;

    private TrackInfo[] tracks;
    @org.jetbrains.annotations.TestOnly
    TrackInfo[] getTracks() {
        return tracks;
    }


    private UndoRecord undo;

    public static final int HANDLE_SIZE = 5;
    public static final Color[] LINE_COLOR;
    public static final Color[] LIGHT_LINE_COLOR;
    public static final Color SELECTED_VALUE_COLOR;
    public static final Color SELECTED_KEY_COLOR;

    static {
        LINE_COLOR = new Color[]{new Color(0, 0, 255), new Color(0, 175, 0), new Color(255, 128, 0), new Color(0, 170, 170), new Color(192, 0, 255), new Color(192, 192, 0)};
        LIGHT_LINE_COLOR = new Color[LINE_COLOR.length];
        for (int i = 0; i < LINE_COLOR.length; i++) {
            LIGHT_LINE_COLOR[i] = new Color(191 + LINE_COLOR[i].getRed() / 4, 191 + LINE_COLOR[i].getGreen() / 4, 191 + LINE_COLOR[i].getBlue() / 4);
        }
        SELECTED_VALUE_COLOR = Color.red;
        SELECTED_KEY_COLOR = Color.magenta;
    }

    /**
     * Create a track graph for one or more quantities which can take on any value.
     */
    public TrackGraph(LayoutWindow win, Score sc) {
        window = win;
        score = sc;
        vertAxis = new VerticalAxis();
        hStart = score.getStartTime();
        hScale = score.getScale();
        subdivisions = win.getScene().getFramesPerSecond();
        setBackground(Color.white);
        setPreferredSize(new Dimension(200, 100));
        addEventLink(MousePressedEvent.class, this, "mousePressed");
        addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
        addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
        addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        addEventLink(RepaintEvent.class, this, "paint");
        tracks = new TrackInfo[0];
        markers = new Vector<>();
    }

    /**
     * Adjust the scale when the graph is resized.
     */
    private void sizeChanged() {
        lastBounds = getBounds();
        if (vScale <= 0.0) {
            setDefaultGraphRange(false);
        } else {
            if (oldHeight <= 0 || lastBounds.height <= 0) {
                return;
            }
            vScale *= lastBounds.height / (float) oldHeight;
            oldHeight = lastBounds.height;
        }
    }

    /**
     * Set the starting time to display.
     */
    @Override
    public void setStartTime(double time) {
        hStart = time;
    }

    /**
     * Set the number of pixels per unit time.
     */
    @Override
    public void setScale(double s) {
        hScale = s;
    }

    /**
     * Set the number of subdivisions per unit time.
     */
    @Override
    public void setSubdivisions(int s) {
        subdivisions = s;
    }

    /**
     * This method is required by the TrackDisplay interface. It does nothing in this case.
     */
    @Override
    public void setYOffset(int offset) {
    }

    /**
     * Add a marker to the display.
     */
    @Override
    public void addMarker(Marker m) {
        markers.add(m);
    }

    /**
     * Set the mode (select-and-move or scroll-and-scale) for this display.
     */
    @Override
    public void setMode(int m) {
        mode = m;
    }

    /**
     * Get the vertical axis for this graph.
     */
    public VerticalAxis getAxis() {
        return vertAxis;
    }

    /**
     * Set the list of tracks to display on this graph.
     */
    public void setTracks(Track[] t) {
        boolean listChanged = (t.length != tracks.length);
        for (int i = 0; i < t.length && i < tracks.length; i++) {
            if (t[i] != tracks[i].track) {
                listChanged = true;
            }
        }
        tracks = new TrackInfo[t.length];
        for (int i = 0; i < t.length; i++) {
            tracks[i] = new TrackInfo(t[i]);
        }
        selectionChanged();
        setDefaultGraphRange(!listChanged);
    }

    /**
     * Select the default range for the graph.
     */
    private void setDefaultGraphRange(boolean largerOnly) {
        Rectangle dim = getBounds();
        double oldMin = vStart;
        double oldMax = vStart + dim.height / vScale;

        // Find the range of values.
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (TrackInfo info: tracks) {
            for (int i = 0; i < info.keyValue.length; i++) {
                for (int j = 0; j < info.keyValue[i].length; j++) {
                    if (!info.disabled[j]) {
                        if (info.keyValue[i][j] < min) {
                            min = info.keyValue[i][j];
                        }
                        if (info.keyValue[i][j] > max) {
                            max = info.keyValue[i][j];
                        }
                    }
                }
            }
            for (int i = 0; i < info.graphValue.length; i++) {
                for (int j = 0; j < info.graphValue[i].length; j++) {
                    if (!info.disabled[j]) {
                        if (info.graphValue[i][j] < min) {
                            min = info.graphValue[i][j];
                        }
                        if (info.graphValue[i][j] > max) {
                            max = info.graphValue[i][j];
                        }
                    }
                }
            }
        }
        if (min == Double.MAX_VALUE) {
            min = 0.0;
            max = 1.0;
        }
        if (max == min && min >= 0.0 && min <= 1.0) {
            min = 0.0;
            max = 1.0;
        }
        if (max == min) {
            min = Math.floor(min);
            max = min + 1.0;
        }
        double extra = 0.05 * (max - min);
        min -= extra;
        max += extra;
        if (largerOnly && min > oldMin) {
            min = oldMin;
        }
        if (largerOnly && max < oldMax) {
            max = oldMax;
        }
        vStart = min;
        vScale = dim.height / (max - min);
        vertAxis.setGraphRange(min, max);
        oldHeight = dim.height;
    }

    /**
     * This should be called whenever a track is modified, to update the display.
     */
    public void tracksModified() {
        for (TrackInfo track : tracks) {
            track.findValues();
        }
        selectionChanged();
        repaint();
    }

    /**
     * Update the flags for which keyframes are selected.
     */
    public void selectionChanged() {
        SelectionInfo[] selection = score.getSelectedKeyframes();
        for (TrackInfo info: tracks) {
            Arrays.fill(info.selected, false);

            for (SelectionInfo selectionInfo : selection) {
                if (selectionInfo.track == info.track) {
                    info.selected[selectionInfo.keyIndex] = true;
                }
            }
        }
    }

    /**
     * Record the times and values of any selected keyframes.
     */
    private void findInitialKeyValues() {
        SelectionInfo[] sel = score.getSelectedKeyframes();
        dragKeyTime = new double[sel.length];
        dragKeyValue = new double[sel.length][];
        for (int i = 0; i < sel.length; i++) {
            double[] t = sel[i].track.getKeyTimes();
            dragKeyTime[i] = t[sel[i].keyIndex];
            Keyframe[] key = sel[i].track.getTimecourse().getValues();
            dragKeyValue[i] = key[sel[i].keyIndex].getGraphValues();
        }
    }

    private void mousePressed(MousePressedEvent ev) {
        lastPos = ev.getPoint();

        undo = null;
        dragPos = null;
        draggingBox = false;
        effectiveMode = (mouseButtonThree(ev) ? Score.SCROLL_AND_SCALE : mode);
        if (effectiveMode != Score.SELECT_AND_MOVE) {
            return;
        }

        // Determine whether the click was on a handle.
        Rectangle dim = getBounds();
        for (TrackInfo track : tracks) {
            for (int j = 0; j < track.keyValue.length; j++) {
                int x = (int) Math.round(hScale * (track.keyTime[j] - hStart));
                if (lastPos.x < x - HANDLE_SIZE / 2 || lastPos.x > x + HANDLE_SIZE / 2) {
                    continue;
                }
                for (int k = track.keyValue[j].length - 1; k >= 0; k--) {
                    if (track.disabled[k]) {
                        continue;
                    }
                    int y = dim.height - (int) Math.round(vScale * (track.keyValue[j][k] - vStart));
                    if (lastPos.y < y - HANDLE_SIZE / 2 || lastPos.y > y + HANDLE_SIZE / 2) {
                        continue;
                    }

                    // Select the clicked keyframe.
                    Keyframe key = track.track.getTimecourse().getValues()[j];
                    SelectionInfo newsel = new SelectionInfo(track.track, key);
                    for (int m = 0; m < newsel.selected.length; m++) {
                        newsel.selected[m] = (m == k);
                    }
                    if (ev.isShiftDown()) {
                        if (score.isKeyframeSelected(key, k)) {
                            score.removeSelectedKeyframe(key);
                        } else {
                            score.addSelectedKeyframes(new SelectionInfo[]{newsel});
                        }
                    } else if (!score.isKeyframeSelected(key, k)) {
                        score.setSelectedKeyframes(new SelectionInfo[]{newsel});
                    }
                    findInitialKeyValues();
                    selectionChanged();
                    score.repaintGraphs();
                    return;
                }
            }
        }
        if (!ev.isShiftDown()) {
            score.setSelectedKeyframes(new SelectionInfo[0]);
        }
        selectionChanged();
        draggingBox = true;
        score.repaintGraphs();
    }

    private void mouseDragged(MouseDraggedEvent ev) {
        Point pos = ev.getPoint();

        if (effectiveMode == Score.SELECT_AND_MOVE) {
            if (draggingBox) {
                // Drag a box for selecting keyframes.

                dragPos = pos;
                repaint();
                return;
            }

            // Drag the selected keyframes.
            SelectionInfo[] sel = score.getSelectedKeyframes();
            int i, j;
            if (undo == null) {
                // Duplicate any tracks with selected keyframes, so we can undo the drag.

                undo = new UndoRecord(window);
                for (i = 0; i < sel.length; i++) {
                    Track tr = sel[i].track;
                    for (j = 0; j < i && tr != sel[j].track; j++);
                    if (j == i) {
                        undo.addCommand(UndoRecord.COPY_TRACK, tr, tr.duplicate(tr.getParent()));
                    }
                }
                window.setUndoRecord(undo);
            }
            double dt = (pos.x - lastPos.x) / hScale;
            double dv = (lastPos.y - pos.y) / vScale;

            // Update the values for selected keyframes.
            for (i = 0; i < sel.length; i++) {
                Track tr = sel[i].track;
                for (j = 0; j < tracks.length && tr != tracks[j].track; j++);
                if (j == tracks.length) {
                    continue;
                }
                for (int k = 0; k < sel[i].selected.length; k++) {
                    if (sel[i].selected[k]) {
                        double newval = dragKeyValue[i][k] + dv;
                        if (newval < tracks[j].valueRange[k][0]) {
                            newval = tracks[j].valueRange[k][0];
                        }
                        if (newval > tracks[j].valueRange[k][1]) {
                            newval = tracks[j].valueRange[k][1];
                        }
                        tracks[j].keyValue[sel[i].keyIndex][k] = newval;
                    }
                }
                sel[i].key.setGraphValues(tracks[j].keyValue[sel[i].keyIndex]);
            }
            for (i = 0; i < sel.length; i++) {
                // Move each selected keyframe.

                int oldindex = sel[i].keyIndex;
                double t = dragKeyTime[i] + dt;
                if (sel[i].track.isQuantized()) {
                    t = Math.round(t * subdivisions) / (double) subdivisions;
                }
                int newindex = sel[i].track.moveKeyframe(oldindex, t);

                // If the index of this keyframe within the timecourse has changed, update all
                // the SelectionInfo objects for this track.
                if (oldindex != newindex) {
                    for (j = 0; j < sel.length; j++) {
                        if (sel[j].keyIndex < oldindex && sel[j].keyIndex > newindex) {
                            sel[j].keyIndex++;
                        } else if (sel[j].keyIndex > oldindex && sel[j].keyIndex < newindex) {
                            sel[j].keyIndex--;
                        }
                    }
                    sel[i].keyIndex = newindex;
                }
            }
            score.tracksModified(false);
            return;
        }
        if (effectiveMode != Score.SCROLL_AND_SCALE) {
            return;
        }
        Rectangle dim = getBounds();
        if (ev.isShiftDown()) {
            // Change the scale of the axes.

            hScale *= Math.pow(1.01, pos.x - lastPos.x);
            vScale *= Math.pow(1.01, lastPos.y - pos.y);
            vertAxis.setGraphRange(vStart, vStart + dim.height / vScale);
            if (pos.x == lastPos.x) {
                repaint();
                vertAxis.repaint();
            } else {
                score.setScale(hScale);
            }
            lastPos = pos;
            return;
        }

        // Scroll the display.
        hStart -= (pos.x - lastPos.x) / hScale;
        vStart -= (lastPos.y - pos.y) / vScale;
        vertAxis.setGraphRange(vStart, vStart + dim.height / vScale);
        if (pos.x == lastPos.x) {
            repaint();
            vertAxis.repaint();
        } else {
            score.setStartTime(hStart);
        }
        lastPos = pos;
    }

    private void mouseReleased(MouseReleasedEvent ev) {
        if (dragPos == null) {
            if (effectiveMode == Score.SELECT_AND_MOVE) {
                score.tracksModified(true);
            }
            return;
        }

        // They were dragging a box, so select any keyframes inside it.
        int x1 = Math.min(lastPos.x, dragPos.x), x2 = Math.max(lastPos.x, dragPos.x);
        int y1 = Math.min(lastPos.y, dragPos.y), y2 = Math.max(lastPos.y, dragPos.y);
        dragPos = null;
        List<SelectionInfo> v = new Vector<>();
        Rectangle dim = getBounds();
        for (TrackInfo track : tracks) {
            for (int j = 0; j < track.keyValue.length; j++) {
                int x = (int) Math.round(hScale * (track.keyTime[j] - hStart));
                if (x < x1 || x > x2) {
                    continue;
                }
                Keyframe key = track.track.getTimecourse().getValues()[j];
                SelectionInfo newsel = new SelectionInfo(track.track, key);
                boolean any = false;
                for (int k = 0; k < track.keyValue[j].length; k++) {
                    newsel.selected[k] = false;
                    if (track.disabled[k]) {
                        continue;
                    }
                    int y = dim.height - (int) Math.round(vScale * (track.keyValue[j][k] - vStart));
                    if (y < y1 || y > y2) {
                        continue;
                    }

                    // Select the clicked keyframe.
                    any = true;
                    newsel.selected[k] = true;
                }
                if (any) {
                    v.add(newsel);
                }
            }
        }
        SelectionInfo[] sel = new SelectionInfo[v.size()];
        for (int i = 0; i < sel.length; i++) {
            sel[i] = v.get(i);
        }
        score.addSelectedKeyframes(sel);
        selectionChanged();
        score.repaintGraphs();
    }

    private void mouseClicked(MouseClickedEvent ev) {
        if (ev.getClickCount() == 2 && effectiveMode == Score.SELECT_AND_MOVE) {
            score.editSelectedKeyframe();
        }
    }

    private void paint(RepaintEvent ev) {
        Rectangle dim = getBounds();
        if (lastBounds == null || dim.width != lastBounds.width || dim.height != lastBounds.height) {
            sizeChanged();
        }
        Graphics2D g = ev.getGraphics();
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int num, fontHeight = fm.getMaxAscent() + fm.getMaxDescent();
        int x, y, labels = 0;
        SelectionInfo[] selection = score.getSelectedKeyframes();

        for (TrackInfo info : tracks) {
            num = info.valueName.length;
            for (int i = 0; i < num; i++) {
                if (info.disabled[i]) {
                    continue;
                }

                // Draw the curve.
                g.setColor(info.dimmed ? LIGHT_LINE_COLOR[labels % LINE_COLOR.length] : LINE_COLOR[labels % LINE_COLOR.length]);
                plotLine(g, info.graphTime, info.graphValue, i, dim);

                // Draw the keyframes.
                for (int j = 0; j < info.keyTime.length; j++) {
                    g.setColor(LINE_COLOR[labels % LINE_COLOR.length]);
                    if (info.selected[j]) {
                        for (SelectionInfo selectionInfo : selection) {
                            if (selectionInfo.track != info.track || selectionInfo.keyIndex != j) {
                                continue;
                            }
                            if (selectionInfo.selected[i]) {
                                g.setColor(SELECTED_VALUE_COLOR);
                            } else {
                                g.setColor(SELECTED_KEY_COLOR);
                            }
                        }
                    }
                    x = (int) Math.round(hScale * (info.keyTime[j] - hStart));
                    y = dim.height - (int) Math.round(vScale * (info.keyValue[j][i] - vStart));
                    g.fillRect(x - HANDLE_SIZE / 2, y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
                }
                g.setColor(LINE_COLOR[labels % LINE_COLOR.length]);
                y = fontHeight * labels + fontHeight / 2;
                g.drawLine(dim.width - 15, y, dim.width - 5, y);
                x = dim.width - 20 - fm.stringWidth(info.valueName[i]);
                g.drawString(info.valueName[i], x, fontHeight * (labels + 1));
                labels++;
            }
        }

        // Show an error message if there is nothing to display.
        String message = null;
        if (tracks.length == 0) {
            message = Translate.text("noTracksSelected");
        } else if (labels == 0) {
            if (tracks.length == 1) {
                message = Translate.text("singleTrackNoGraph", tracks[0].track.getName());
            } else {
                message = Translate.text("multiTrackNoGraph");
            }
        }
        if (message != null) {
            x = (dim.width - fm.stringWidth(message)) / 2;
            y = (dim.height + fontHeight) / 2;
            g.drawString(message, x, y);
        }

        // Draw the markers.
        for (Marker m : markers) {
            g.setColor(m.getColor());
            x = (int) Math.round(hScale * (m.getPosition() - hStart));
            g.drawLine(x, 0, x, dim.height);
        }
        if (lineAtBottom) {
            g.setColor(Color.black);
            g.drawLine(0, dim.height - 1, dim.width, dim.height - 1);
        }

        // If a drag is in progress, draw a box.
        if (dragPos != null && tracks.length > 0) {
            g.setColor(Color.BLACK);
            g.drawRect(Math.min(lastPos.x, dragPos.x), Math.min(lastPos.y, dragPos.y),
                    Math.abs(dragPos.x - lastPos.x), Math.abs(dragPos.y - lastPos.y));
        }
    }

    /**
     * Plot a line on the graph.
     */
    private void plotLine(Graphics2D g, double[] x, double[][] y, int which, Rectangle dim) {
        int fromX, fromY, toX = (int) Math.round(hScale * (x[0] - hStart));
        int toY = dim.height - (int) Math.round(vScale * (y[0][which] - vStart));

        if (toX > 0) {
            g.drawLine(0, toY, toX, toY);
        }
        for (int i = 1; i < x.length; i++) {
            fromX = toX;
            fromY = toY;
            toX = (int) Math.round(hScale * (x[i] - hStart));
            toY = dim.height - (int) Math.round(vScale * (y[i][which] - vStart));
            g.drawLine(fromX, fromY, toX, toY);
        }
        if (toX < dim.width) {
            g.drawLine(toX, toY, dim.width, toY);
        }
    }

    /**
     * Inner class which represents information about a particular track being shown on the graph.
     */
    static class TrackInfo {

        final Track track;
        String[] valueName;
        double[] keyTime;
        double[][] keyValue;
        double[] graphTime;
        double[][] graphValue;
        double[][] valueRange;
        boolean[] disabled;
        boolean[] selected;
        boolean dimmed;

        public TrackInfo(Track tr) {
            track = tr;
            findValues();
        }

        /**
         * Calculate all the values for a graph.
         */
        public void findValues() {
            Timecourse tc = track.getTimecourse();
            if (tc == null) {
                // This track is not defined by keyframes.

                valueName = new String[0];
                keyTime = new double[0];
                keyValue = new double[0][0];
                graphTime = new double[0];
                graphValue = new double[0][0];
                disabled = new boolean[0];
                selected = new boolean[0];
                return;
            }
            valueName = track.getValueNames();
            int num = valueName.length;
            valueRange = track.getValueRange();
            keyTime = tc.getTimes();
            Keyframe[] keyframe = tc.getValues();
            Keyframe[] graphKeyframe;

            // Determine which values are disabled.
            if (track instanceof PositionTrack) {
                PositionTrack tr = (PositionTrack) track;
                disabled = new boolean[]{!tr.affectsX(), !tr.affectsY(), !tr.affectsZ()};
            } else if (track instanceof RotationTrack) {
                RotationTrack tr = (RotationTrack) track;
                disabled = new boolean[]{!tr.affectsX(), !tr.affectsY(), !tr.affectsZ()};
            } else {
                disabled = new boolean[num];
            }
            if (selected == null || selected.length != keyTime.length) {
                selected = new boolean[keyTime.length];
            }
            dimmed = (track instanceof RotationTrack && ((RotationTrack) track).getUseQuaternion());

            // Subdivide the track to get the keyframes for the graph.
            if (keyTime.length == 0) {
                // There are no keyframes to display for this track.

                graphTime = new double[]{0.0};
                graphValue = new double[1][];
                graphValue[0] = track.getDefaultGraphValues();
                keyValue = new double[0][0];
                return;
            }
            if (track.getSmoothingMethod() == Timecourse.DISCONTINUOUS) {
                graphTime = new double[keyframe.length * 2 - 1];
                graphKeyframe = new Keyframe[keyframe.length * 2 - 1];
                for (int i = 0; i < keyframe.length; i++) {
                    graphKeyframe[i * 2] = keyframe[i];
                    graphTime[i * 2] = keyTime[i];
                    if (i < keyframe.length - 1) {
                        graphKeyframe[i * 2 + 1] = keyframe[i];
                        graphTime[i * 2 + 1] = keyTime[i + 1];
                    }
                }
            } else {
                Timecourse sub = tc.subdivide(track.getSmoothingMethod());
                sub = sub.subdivide(track.getSmoothingMethod());
                sub = sub.subdivide(track.getSmoothingMethod());
                graphTime = sub.getTimes();
                graphKeyframe = sub.getValues();
            }

            // Get the keyframe values.
            keyValue = new double[keyframe.length][];
            for (int i = 0; i < keyframe.length; i++) {
                keyValue[i] = keyframe[i].getGraphValues();
            }
            graphValue = new double[graphKeyframe.length][1];
            for (int i = 0; i < graphKeyframe.length; i++) {
                graphValue[i] = graphKeyframe[i].getGraphValues();
            }
        }
    }
}
