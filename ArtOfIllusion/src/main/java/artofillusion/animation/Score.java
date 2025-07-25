/* Copyright (C) 2001-2012 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.animation;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a Widget which displays all the tracks for objects in a scene, and shows
 * where their keyframes are.
 */
@Slf4j
public class Score extends BorderContainer implements EditingWindow, PopupMenuManager {

    final LayoutWindow window;
    final Scene scene;
    
    private final TreeList theList;
    final TimeAxis theAxis;
    private final List<TrackDisplay> graphs;
    final BScrollBar scroll;
    final ToolPalette viewTools;
    final ToolPalette modeTools;
    final BLabel helpText;
    final BSplitPane div;
    /**
     * -- GETTER --
     *  Get the popup menu for the score.
     */
    @Getter
    BPopupMenu popupMenu;
    BMenuItem[] popupMenuItem;
    final Marker timeMarker;
    private SelectionInfo[] selection = new SelectionInfo[0];
    int scrollPos;
    int mode;
    int view;
    @Getter double startTime;
    double timeScale;
    private boolean[] hasRepaintedView;
    private boolean isAnimating;
    private long animateStartClockTime;
    private double animateStartSceneTime;
    /**
     * -- GETTER --
     *  Get the playback speed.
     */
    @Getter
    private double playbackSpeed;
    private final BButton playButton;
    private final BButton rewindButton;
    private final BButton endButton;
    private final BSlider speedSlider;
    private final BLabel speedLabel;
    private final BLabel timeFrameLabel;
    private final ImageIcon playIcon = ThemeManager.getIcon("play");
    private final ImageIcon stopIcon = ThemeManager.getIcon("stop");

    public static final int TRACKS_MODE = 0;
    public static final int SINGLE_GRAPH_MODE = 1;
    public static final int MULTI_GRAPH_MODE = 2;

    public static final int SELECT_AND_MOVE = 0;
    public static final int SCROLL_AND_SCALE = 1;

    private final String[] MODE_HELP_TEXT = new String[]{
            Translate.text("moveKeyframeTool.helpText"),
            Translate.text("moveScoreTool.helpText")};

    private final double[] SPEEDS = {0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1, 1.5, 2, 2.5, 3, 4, 5};

    public Score(LayoutWindow win) {
        window = win;
        scene = window.getScene()   ;

        playButton = new BButton(playIcon);
        rewindButton = new BButton(ThemeManager.getIcon("rewind"));
        endButton = new BButton(ThemeManager.getIcon("forward"));
        playButton.getComponent().addActionListener(event -> clickedPlay());
        rewindButton.getComponent().addActionListener(event -> clickedRewind());
        endButton.getComponent().addActionListener(event -> clickedEnd());
        speedSlider = new BSlider(SPEEDS.length / 2, 0, SPEEDS.length - 1, BSlider.HORIZONTAL);
        speedSlider.getComponent().addChangeListener(event -> speedChanged());

        speedSlider.setMinorTickSpacing(1);
        speedSlider.setSnapToTicks(true);
        speedSlider.getComponent().setPreferredSize(new Dimension(1, speedSlider.getPreferredSize().height));
        speedLabel = new BLabel();
        speedLabel.setAlignment(BLabel.CENTER);
        timeFrameLabel = new BLabel();
        timeFrameLabel.setAlignment(BLabel.NORTH);
        RowContainer controlButtons = new RowContainer();
        controlButtons.add(rewindButton);
        controlButtons.add(playButton);
        controlButtons.add(endButton);
        FormContainer controlsContainer = new FormContainer(new double[]{1, 0}, new double[]{0, 0, 0, 1});
        controlsContainer.add(controlButtons, 0, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE));
        controlsContainer.add(speedLabel, 0, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL));
        controlsContainer.add(speedSlider, 0, 2, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL));
        controlsContainer.add(timeFrameLabel, 0, 3, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH));
        controlsContainer.add(new BSeparator(BSeparator.VERTICAL), 1, 0, 1, 4);
        theList = new TreeList(win);
        theList.setPreferredSize(new Dimension(130, 0));
        theList.addEventLink(TreeList.ElementMovedEvent.class, this, "elementsMoved");
        theList.addEventLink(TreeList.ElementExpandedEvent.class, this, "elementOpenedOrClosed");
        theList.addEventLink(TreeList.ElementDoubleClickedEvent.class, this, "elementDoubleClicked");
        theList.addEventLink(SelectionChangedEvent.class, this, "treeSelectionChanged");
        theList.setPopupMenuManager(this);

        timeScale = scene.getFramesPerSecond() * 5.0;
        theAxis = new TimeAxis(scene.getFramesPerSecond(), timeScale, this);
        graphs = new Vector<>();
        timeMarker = new Marker(scene.getTime(), Translate.text("Time"), Color.green);
        theAxis.addMarker(timeMarker);
        scroll = new BScrollBar(0, 0, 0, 0, BScrollBar.VERTICAL);
        scroll.addEventLink(ValueChangedEvent.class, this, "scrollbarChanged");
        viewTools = new ToolPalette(1, 3);
        modeTools = new ToolPalette(1, 2);
        viewTools.addTool(new GenericTool(this, "trackMode", Translate.text("trackModeTool.tipText")));
        viewTools.addTool(new GenericTool(this, "singleMode", Translate.text("singleGraphModeTool.tipText")));
        viewTools.addTool(new GenericTool(this, "multiMode", Translate.text("multiGraphModeTool.tipText")));
        modeTools.addTool(new GenericTool(this, "moveKey", Translate.text("moveKeyframeTool.tipText")));
        modeTools.addTool(new GenericTool(this, "panTrack", Translate.text("moveScoreTool.tipText")));
        BorderContainer treeContainer = new BorderContainer();
        treeContainer.add(new Spacer(theList, theAxis), BorderContainer.NORTH);
        treeContainer.add(theList, BorderContainer.CENTER);
        div = new BSplitPane(BSplitPane.HORIZONTAL, treeContainer, null);
        div.setResizeWeight(0.0);
        div.resetToPreferredSizes();
        div.getComponent().setBorder(null);
        layoutGraphs();
        add(div, BorderContainer.CENTER);
        FormContainer rightSide = new FormContainer(new double[]{1.0, 1.0}, new double[]{0.0, 0.0, 1.0});
        rightSide.add(scroll, 0, 0, 1, 3, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.VERTICAL, null, null));
        rightSide.add(viewTools, 1, 0);
        rightSide.add(modeTools, 1, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(0, 5, 0, 0), null));
        add(controlsContainer, BorderContainer.WEST);
        add(rightSide, BorderContainer.EAST);
        add(helpText = new BLabel(MODE_HELP_TEXT[mode]), BorderContainer.SOUTH);
        rebuildList();
        createPopupMenu();
        setFocusable(true);
        setPlaybackSeed(1);
        UIUtilities.applyDefaultBackground(controlsContainer);
        UIUtilities.applyDefaultBackground(rightSide);
    }

    /**
     * Create the popup menu.
     */
    private void createPopupMenu() {
        popupMenu = new BPopupMenu();
        popupMenuItem = new BMenuItem[5];
        popupMenu.add(popupMenuItem[0] = Translate.menuItem("editTrack", event -> editSelectedTrack()));
        popupMenu.add(popupMenuItem[1] = Translate.menuItem("duplicateTracks", event -> duplicateSelectedTracks()));
        popupMenu.add(popupMenuItem[2] = Translate.menuItem("deleteTracks", event -> deleteSelectedTracks()));
        popupMenu.add(popupMenuItem[3] = Translate.menuItem("enableTracks", event -> setTracksEnabled(true)));
        popupMenu.add(popupMenuItem[4] = Translate.menuItem("disableTracks", event -> setTracksEnabled(false)));
    }

    /**
     * Display the popup menu.
     */
    @Override
    public void showPopupMenu(Widget w, int x, int y) {
        Track[] selTrack = getSelectedTracks();
        boolean enable = false;
        boolean disable = false;

        for (Track track : selTrack) {
            if (track.isEnabled()) {
                disable = true;
            } else {
                enable = true;
            }
        }

        popupMenuItem[0].setEnabled(selTrack.length == 1); // Edit Track
        popupMenuItem[1].setEnabled(selTrack.length > 0); // Duplicate Tracks
        popupMenuItem[2].setEnabled(selTrack.length > 0); // Delete Tracks
        popupMenuItem[3].setEnabled(enable); // Enable Tracks
        popupMenuItem[4].setEnabled(disable); // Disable Tracks
        popupMenu.show(w, x, y);
    }

    /**
     * Allow the score to be fully hidden.
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    /**
     * Get the currently selected tracks.
     */
    public Track[] getSelectedTracks() {
        Object[] obj = theList.getSelectedObjects();
        Track[] tr = new Track[obj.length];

        for (int i = 0; i < tr.length; i++) {
            tr[i] = (Track) obj[i];
        }
        return tr;
    }

    /**
     * Get the currently selected keyframes.
     */
    public SelectionInfo[] getSelectedKeyframes() {
        return selection;
    }

    /**
     * Set the currently selected keyframes.
     */
    public void setSelectedKeyframes(SelectionInfo[] sel) {
        selection = sel;
        graphs.forEach(graph -> ((Widget) graph).repaint());
        window.updateMenus();
    }

    /**
     * Add a set of keyframes to the selection.
     */
    public void addSelectedKeyframes(SelectionInfo[] newSelection) {
        List<SelectionInfo> currentSelection = new Vector<>();
        Collections.addAll(currentSelection, selection);

        for (SelectionInfo item : newSelection) {
            int j;
            for (j = 0; j < selection.length; j++)
                if (item.key == selection[j].key) {
                    for (int k = 0; k < item.selected.length; k++)
                        selection[j].selected[k] |= item.selected[k];
                    break;
                }
            if (j == selection.length)
                currentSelection.add(item);
        }

        selection = currentSelection.toArray(SelectionInfo[]::new);
        window.updateMenus();
    }

    /**
     * Remove a keyframe from the selection.
     */
    public void removeSelectedKeyframe(Keyframe key) {
        List<SelectionInfo> filtered = new Vector<>();

        for (SelectionInfo selectionInfo : selection) {
            if (selectionInfo.key == key) continue;
            filtered.add(selectionInfo);
        }
        selection = filtered.toArray(SelectionInfo[]::new);
        window.updateMenus();
    }

    /**
     * Determine whether a particular keyframe is selected.
     */
    public boolean isKeyframeSelected(Keyframe k) {
        for (SelectionInfo selectionInfo : selection) {
            if (selectionInfo.key == k) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the handle for a particular value of a keyframe is selected.
     */
    public boolean isKeyframeSelected(Keyframe k, int value) {
        for (SelectionInfo selectionInfo: selection) {
            if (selectionInfo.key == k) {
                return (selectionInfo.selected.length > value && selectionInfo.selected[value]);
            }
        }
        return false;
    }

    /**
     * Rebuild the TreeList, attempting as much as possible to preserve its current state.
     */
    public void rebuildList() {
        Scene theScene = scene;
        TreeElement[] allEl = theList.getElements();

        theList.setUpdateEnabled(false);
        theList.removeAllElements();

        for (ObjectInfo info : theScene.getObjects()) {
            if (info.isSelected()) {
                TreeElement el = new ObjectTreeElement(info, null, theList, false);
                theList.addElement(el);
                el.setExpanded(true);
            }
        }
        for (ObjectInfo info : theScene.getObjects()) {
            if (!info.isSelected() || info.getTracks().length == 0) {
                continue;
            }
            ObjectTreeElement el = (ObjectTreeElement) theList.findElement(info);
            el.addTracks();
        }

        for (int i = 0; i < allEl.length; i++) {
            TreeElement el = theList.findElement(allEl[i].getObject());
            if (el == null) {
                continue;
            }
            el.setExpanded(allEl[i].isExpanded());
            el.setSelected(allEl[i].isSelected());
        }
        allEl = theList.getElements();
        for (int i = 0; i < allEl.length; i++) {
            allEl[i].setSelectable(allEl[i] instanceof TrackTreeElement);
        }
        theList.setUpdateEnabled(true);
        selectedTracksChanged();
        updateScrollbar();
        repaintGraphs();
        setScrollPosition(scrollPos);
    }

    /**
     * Layout the display in the right side of the Score, based on the current view mode
     * and selected tracks.
     */
    private void layoutGraphs() {
        Track[] tr = getSelectedTracks();
        graphs.clear();
        int divider = div.getDividerLocation();
        if (view == TRACKS_MODE) {
            FormContainer graphContainer = new FormContainer(new double[]{1.0}, new double[]{0.0, 1.0});
            TracksPanel theTracks = new TracksPanel(window, theList, this, scene.getFramesPerSecond(), timeScale);
            theTracks.setStartTime(startTime);
            theTracks.addMarker(timeMarker);
            theTracks.setMode(mode);
            graphContainer.add(theAxis, 0, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(0, 0, 2, 0), null));
            graphContainer.add(theTracks, 0, 1, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
            graphs.add(theTracks);
            div.add(graphContainer, 1);
            graphContainer.addEventLink(MouseScrolledEvent.class, this, "mouseScrolled");
        } else if (view == SINGLE_GRAPH_MODE || tr.length == 0) {
            FormContainer graphContainer = new FormContainer(new double[]{0.0, 1.0}, new double[]{0.0, 1.0});
            TrackGraph gr = new TrackGraph(window, this);
            gr.setSubdivisions(scene.getFramesPerSecond());
            gr.setStartTime(startTime);
            gr.setScale(timeScale);
            gr.addMarker(timeMarker);
            gr.setMode(mode);
            gr.setTracks(tr);
            gr.setBackground(Color.white);
            graphContainer.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
            graphContainer.add(theAxis, 1, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(0, 0, 2, 0), null));
            graphContainer.add(gr.getAxis(), 0, 1);
            graphContainer.add(gr, 1, 1);
            graphs.add(gr);
            div.add(graphContainer, 1);
            graphContainer.addEventLink(MouseScrolledEvent.class, this, "mouseScrolled");
        } else if (view == MULTI_GRAPH_MODE) {
            double[] weight = new double[tr.length + 1];
            for (int i = 1; i < weight.length; i++) {
                weight[i] = 1.0;
            }
            FormContainer graphContainer = new FormContainer(new double[]{0.0, 1.0}, weight);
            graphContainer.add(theAxis, 1, 0, new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(0, 0, 2, 0), null));
            for (int i = 0; i < tr.length; i++) {
                TrackGraph gr = new TrackGraph(window, this);
                gr.setSubdivisions(scene.getFramesPerSecond());
                gr.setStartTime(startTime);
                gr.setScale(timeScale);
                gr.addMarker(timeMarker);
                gr.setMode(mode);
                gr.setTracks(new Track[]{tr[i]});
                gr.setBackground(Color.white);
                LayoutInfo layout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, new Insets(i == 0 ? 0 : 4, 0, 0, 0), null);
                graphContainer.add(gr.getAxis(), 0, i + 1, layout);
                graphContainer.add(gr, 1, i + 1, layout);
                graphs.add(gr);
            }
            div.add(graphContainer, 1);
            graphContainer.addEventLink(MouseScrolledEvent.class, this, "mouseScrolled");
        }
        for (Widget child : div.getChildren()) {
            UIUtilities.applyDefaultBackground(child);
        }
        UIUtilities.applyDefaultFont(div);
        div.setDividerLocation(divider);
        div.layoutChildren();
        repaintAll();
    }

    /**
     * Set the starting time to display.
     */
    public void setStartTime(double time) {
        theAxis.setStartTime(time);
        graphs.forEach(graph -> graph.setStartTime(time));
        startTime = time;
        repaintGraphs();
    }

    /**
     * Get the number of pixels per unit time.
     */
    public double getScale() {
        return timeScale;
    }

    /**
     * Set the number of pixels per unit time.
     */
    public void setScale(double s) {
        theAxis.setScale(s);
        graphs.forEach(track -> track.setScale(s));
        timeScale = s;
        repaintGraphs();
    }

    /**
     * Set the current time.
     */
    public void setTime(double time) {
        if (hasRepaintedView != null) {
            Arrays.fill(hasRepaintedView, false);
        }
        timeMarker.setPosition(time);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(3);
        timeFrameLabel.setText(Translate.text("timeFrameLabel", nf.format(time), Integer.toString((int) Math.round(time * scene.getFramesPerSecond()))));
        int graphWidth = ((Widget) graphs.get(0)).getBounds().width;
        if (time < startTime) {
            setStartTime(time);
        } else if (time > startTime + graphWidth / timeScale) {
            setStartTime(time - 0.5 * graphWidth / timeScale);
        } else {
            repaintGraphs();
        }
    }

    /**
     * Start animating the display.
     */
    public void startAnimating() {
        if (hasRepaintedView == null) {
            // The first time this is called, add a listener to all the views in the window, so we can tell when they've all
            // been repainted.

            hasRepaintedView = new boolean[window.getAllViews().length];
            Object listener = new Object() {
                void viewRepainted(RepaintEvent ev) {
                    boolean allRepainted = true;
                    for (int i = 0; i < hasRepaintedView.length; i++) {
                        if (ev.getWidget() == window.getAllViews()[i]) {
                            hasRepaintedView[i] = true;
                        }
                        allRepainted &= hasRepaintedView[i];
                    }
                    if (isAnimating && (!window.getSplitView() || allRepainted)) {
                        // Update the time to show the next frame.

                        double elapsedTime = (System.currentTimeMillis() - animateStartClockTime) * 0.001;
                        double sceneTime = animateStartSceneTime + elapsedTime * playbackSpeed;
                        int fps = scene.getFramesPerSecond();
                        sceneTime = ((int) (sceneTime * fps)) / (double) fps;
                        window.setTime(sceneTime);
                    }
                }
            };
            for (ViewerCanvas cView : window.getAllViews()) {
                cView.addEventLink(RepaintEvent.class, listener, "viewRepainted");
            }
        }
        animateStartSceneTime = scene.getTime();
        animateStartClockTime = System.currentTimeMillis();
        isAnimating = true;
        playButton.setIcon(stopIcon);
        rewindButton.setEnabled(false);
        endButton.setEnabled(false);
        window.setTime(animateStartSceneTime);
    }

    /**
     * Stop animating the display.
     */
    public void stopAnimating() {
        isAnimating = false;
        playButton.setIcon(playIcon);
        rewindButton.setEnabled(true);
        endButton.setEnabled(true);
    }

    /**
     * Set the playback speed.
     */
    public void setPlaybackSeed(double speed) {
        playbackSpeed = speed;
        int speedIndex;
        for (speedIndex = 0; speedIndex < SPEEDS.length && speed > SPEEDS[speedIndex]; speedIndex++)
            ;
        speedSlider.setValue(speedIndex);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        speedLabel.setText(Translate.text("playbackSpeedLabel", nf.format(speed)));
        animateStartSceneTime = scene.getTime();
        animateStartClockTime = System.currentTimeMillis();
    }

    /**
     * Respond to the scroll wheel.
     */
    private void mouseScrolled(MouseScrolledEvent ev) {
        setStartTime(startTime + ev.getUnitsToScroll() / (double) scene.getFramesPerSecond());
    }

    /**
     * Update the menus when the selection changes.
     */
    private void treeSelectionChanged() {
        selectedTracksChanged();
        window.updateMenus();
    }

    /**
     * This is called whenever there is a change to which tracks are selected. It updates
     * the list of selected keyframes, removing any which are no longer appropriate.
     */
    private void selectedTracksChanged() {
        List<SelectionInfo> v = new Vector<>();
        Track[] sel = getSelectedTracks();

        for (int i = 0; i < selection.length; i++) {
            for (int j = 0; j < sel.length; j++) {
                if (selection[i].track == sel[j]) {
                    v.add(selection[i]);
                    break;
                }
            }
        }
        selection = new SelectionInfo[v.size()];
        for (int i = 0; i < selection.length; i++) {
            selection[i] = v.get(i);
        }
        if (view == SINGLE_GRAPH_MODE) {
            ((TrackGraph) graphs.get(0)).setTracks(sel);
            repaintAll();
        }
        if (view == MULTI_GRAPH_MODE) {
            layoutGraphs();
        }
    }

    /**
     * This is called whenever a track is modified. It causes all graphs to be
     * properly updated and, optionally, updates the Scene to reflect any changes
     * to selected keyframes.
     */
    public void tracksModified(boolean updateScene) {
        for (int i = 0; i < graphs.size(); i++) {
            if (graphs.get(i) instanceof TrackGraph) {
                ((TrackGraph) graphs.get(i)).tracksModified();
            } else {
                ((Widget) graphs.get(i)).repaint();
            }
        }
        if (!updateScene) {
            return;
        }

        // Find the list of tracks with selected keyframes.
        List<Track> v = new Vector<>();
        for (int i = 0; i < selection.length; i++) {
            if (!v.contains(selection[i].track)) {
                v.add(selection[i].track);
            }
        }

        // Now update them.
        for (int i = 0; i < v.size(); i++) {
            Track tr = v.get(i);
            Object parent = tr.getParent();
            while (parent != null && parent instanceof Track) {
                parent = ((Track) parent).getParent();
            }
            if (parent instanceof ObjectInfo) {
                scene.applyTracksToObject((ObjectInfo) parent);
            }
        }
        window.updateImage();
    }

    /**
     * Repaint all of the graphs.
     */
    public void repaintGraphs() {
        for (int i = 0; i < graphs.size(); i++) {
            Widget gr = (Widget) graphs.get(i);
            gr.repaint();
            if (gr instanceof TrackGraph) {
                ((TrackGraph) gr).getAxis().repaint();
            }
        }
        theAxis.repaint();
    }

    /**
     * Repaint all of the child Widgets.
     */
    public void repaintAll() {
        repaintGraphs();
        theList.repaint();
    }

    private void elementsMoved() {
        repaintGraphs();
    }

    private void elementOpenedOrClosed() {
        repaintGraphs();
        updateScrollbar();
    }

    private void elementDoubleClicked(TreeList.ElementDoubleClickedEvent ev) {
        TreeElement el = ev.getElement();
        if (el != null && el.getObject() instanceof Track) {
            Track tr = (Track) el.getObject();
            tr.edit(window);
            Object parent = tr.getParent();
            while (parent != null && parent instanceof Track) {
                parent = ((Track) parent).getParent();
            }
            if (parent instanceof ObjectInfo) {
                scene.applyTracksToObject((ObjectInfo) parent);
            }
            window.updateImage();
            selectedTracksChanged();
            repaintAll();
        }
    }

    /**
     * Scroll the list and the tracks together.
     */
    public void setScrollPosition(int pos) {
        Rectangle size = theList.getBounds();
        Dimension prefSize = theList.getPreferredSize();

        if (pos > prefSize.height - size.height) {
            pos = prefSize.height - size.height;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (pos == scrollPos) {
            return;
        }
        theList.setYOffset(-pos);
        for (TrackDisplay graph : graphs) graph.setYOffset(-pos);

        theList.repaint();
        repaintGraphs();
        scrollPos = pos;
        updateScrollbar();
    }

    /**
     * Respond to changes on the scrollbar.
     */
    private void scrollbarChanged(ValueChangedEvent ev) {
        setScrollPosition(scroll.getValue());
    }

    /**
     * Update the bounds of the scrollbar.
     */
    private void updateScrollbar() {
        int height = theList.getPreferredSize().height;
        int showing = theList.getBounds().height;
        scroll.setMaximum(height);
        scroll.setExtent(showing);
        scroll.setValue(scrollPos);
        scroll.setUnitIncrement(Math.max(theList.getRowHeight(), 1));
        scroll.setBlockIncrement(Math.max(showing - theList.getRowHeight(), 1));
    }

    /**
     * This is called when a time marker has been moved. If this is an intermediate
     * position in the middle of a drag, then intermediate will be true.
     */
    public void markerMoved(Marker m, boolean intermediate) {
        if (/*!intermediate && */m == timeMarker && m.getPosition() != scene.getTime()) {
            window.setTime(m.getPosition());
        } else {
            repaintGraphs();
        }
    }

    /**
     * Make sure the scrollbar gets adjusted when the score is resized.
     */
    @Override
    public void layoutChildren() {
//    theAxis.setSize(theAxis.getSize().width, theAxis.getPreferredSize().height); // Workaround for layout manager bug.
        super.layoutChildren();
        updateScrollbar();
    }

    /**
     * Allow the user to edit the currently selected track.
     */
    public void editSelectedTrack() {
        Object[] sel = theList.getSelectedObjects();
        if (sel.length == 1 && sel[0] instanceof Track) {
            Track tr = (Track) sel[0];
            tr.edit(window);
            finishEditingTrack(tr);
        }
    }

    /**
     * This method should be called when a track is done being edited.
     */
    public void finishEditingTrack(Track tr) {
        Object parent = tr.getParent();
        while (parent != null && parent instanceof Track) {
            parent = ((Track) parent).getParent();
        }
        if (parent instanceof ObjectInfo) {
            scene.applyTracksToObject((ObjectInfo) parent);
        }
        setModified();
        window.updateImage();
        selectedTracksChanged();
        repaintAll();
    }

    public void enableTracks() {
        setTracksEnabled(true);
    }

    public void disableTracks() {
        setTracksEnabled(false);
    }

    /**
     * Enable or disable all selected tracks.
     */
    public void setTracksEnabled(boolean enable) {
        Object[] sel = theList.getSelectedObjects();
        UndoRecord undo = new UndoRecord(window);
        List<ObjectInfo> owners = new Vector<>();

        for (int i = 0; i < sel.length; i++) {
            if (sel[i] instanceof Track) {
                Track tr = (Track) sel[i];
                Object parent = tr.getParent();
                while (parent instanceof Track) {
                    parent = ((Track) parent).getParent();
                }
                if (parent instanceof ObjectInfo && owners.indexOf(parent) == -1) {
                    owners.add((ObjectInfo) parent);
                    undo.addCommand(UndoRecord.COPY_OBJECT_INFO, parent, ((ObjectInfo) parent).duplicate());
                }
                tr.setEnabled(enable);
            }
        }
        owners.forEach(scene::applyTracksToObject);
        theList.repaint();
        window.setUndoRecord(undo);
        window.updateImage();
        window.updateMenus();
    }

    /**
     * Add a keyframe to each selected track, based on the current state of the scene.
     */
    public void keyframeSelectedTracks() {

        final double time = scene.getTime();
        UndoRecord undo = new UndoRecord(window);
        List<SelectionInfo> newKeys = new Vector<>();

        for (var si: theList.getSelectedObjects()) {
            if (si instanceof Track) {
                Track tr = (Track) si;
                if (tr.getParent() instanceof ObjectInfo) {
                    ObjectInfo info = (ObjectInfo) tr.getParent();
                    for (int j = 0; j < info.getTracks().length; j++) {
                        if (info.getTracks()[j] == tr) {
                            undo.addCommand(UndoRecord.SET_TRACK, info, j, tr.duplicate(info));
                        }
                    }
                }
                Optional.ofNullable(tr.setKeyframe(time)).ifPresent(k -> newKeys.add(new SelectionInfo(tr, k)));
            }
        }

        window.setUndoRecord(undo);
        if (!newKeys.isEmpty()) {
            setSelectedKeyframes(newKeys.toArray(SelectionInfo[]::new));
        }
        selectedTracksChanged();
        repaintGraphs();
        window.updateMenus();
    }

    /**
     * Add a keyframe to the tracks of selected objects which have been modified.
     */
    public void keyframeModifiedTracks() {
        Scene theScene = scene;

        double time = theScene.getTime();
        UndoRecord undo = new UndoRecord(window);
        List<SelectionInfo> newKeys = new Vector<>();

        for (int si: window.getSelectedIndices()) {
            ObjectInfo info = theScene.getObject(si);
            boolean posx = false;
            boolean posy = false;
            boolean posz = false;
            boolean rotx = false;
            boolean roty = false;
            boolean rotz = false;

            for (int j = 0; j < info.getTracks().length; j++) {
                Track tr = info.getTracks()[j];
                if (!tr.isEnabled()) {
                    continue;
                }
                if (tr instanceof PositionTrack && posx && posy && posz) {
                    continue;
                }
                if (tr instanceof RotationTrack && rotx && roty && rotz) {
                    continue;
                }
                undo.addCommand(UndoRecord.SET_TRACK, info, j, tr.duplicate(info));
                Keyframe k = tr.setKeyframeIfModified(time);
                if (k == null) continue;

                newKeys.add(new SelectionInfo(tr, k));
                if (tr instanceof PositionTrack) {
                    PositionTrack pt = (PositionTrack) tr;
                    posx |= pt.affectsX();
                    posy |= pt.affectsY();
                    posz |= pt.affectsZ();
                }
                if (tr instanceof RotationTrack) {
                    RotationTrack rt = (RotationTrack) tr;
                    rotx |= rt.affectsX();
                    roty |= rt.affectsY();
                    rotz |= rt.affectsZ();
                }
            }
        }
        window.setUndoRecord(undo);
        if (!newKeys.isEmpty()) {
            setSelectedKeyframes(newKeys.toArray(SelectionInfo[]::new));
        }
        selectedTracksChanged();
        repaintGraphs();
        window.updateMenus();
    }

    /**
     * Duplicate the selected tracks.
     */
    public void duplicateSelectedTracks() {

        UndoRecord undo = new UndoRecord(window);
        List<ObjectInfo> modifiedObj = new Vector<>();
        List<Track> addedTrack = new Vector<>();

        for (var sel: theList.getSelectedObjects()) {
            if (sel instanceof Track) {
                Track tr = (Track) sel;
                if (!(tr.getParent() instanceof ObjectInfo)) {
                    continue;
                }
                ObjectInfo info = (ObjectInfo) tr.getParent();
                if (modifiedObj.indexOf(info) < 0) {
                    undo.addCommand(UndoRecord.SET_TRACK_LIST, info, info.getTracks());
                    modifiedObj.add(info);
                }
                for (int j = 0; j < info.getTracks().length; j++) {
                    if (info.getTracks()[j] == tr) {
                        Track newtr = tr.duplicate(info);
                        newtr.setName("Copy of " + tr.getName());
                        info.addTrack(newtr, j + 1);
                        addedTrack.add(newtr);
                    }
                }
            }
        }
        window.setUndoRecord(undo);
        rebuildList();
        for (Track track : addedTrack) {
            TreeElement el = theList.findElement(track);
            if (el == null) continue;
            el.setSelected(true);
        }
        repaintGraphs();
    }

    /**
     * Delete the selected tracks.
     */
    public void deleteSelectedTracks() {

        UndoRecord undo = new UndoRecord(window);
        List<ObjectInfo> modifiedObj = new Vector<>();

        for (var item: theList.getSelectedObjects()) {
            if (item instanceof Track) {
                Track tr = (Track) item;
                if (!(tr.getParent() instanceof ObjectInfo)) {
                    continue;
                }
                ObjectInfo info = (ObjectInfo) tr.getParent();
                if (modifiedObj.indexOf(info) < 0) {
                    undo.addCommand(UndoRecord.SET_TRACK_LIST, info, info.getTracks());
                    modifiedObj.add(info);
                }
                info.removeTrack(tr);
            }
        }
        window.setUndoRecord(undo);
        rebuildList();
        repaintGraphs();
    }

    /**
     * Select all tracks of selected objects.
     */
    public void selectAllTracks() {
        Scene theScene = scene;
        int[] sel = window.getSelectedIndices();

        theList.setUpdateEnabled(false);
        for (int i = 0; i < sel.length; i++) {
            ObjectInfo info = theScene.getObject(sel[i]);
            TreeElement el = theList.findElement(info);
            if (el != null) {
                for (int j = 0; j < el.getNumChildren(); j++) {
                    theList.setSelected(el.getChild(j), true);
                }
            }
        }
        theList.setUpdateEnabled(true);
        selectedTracksChanged();
        window.updateMenus();
    }

    public static Constructor<Track> getTrackConstructor(Class<? extends Track> clazz, int argsCount) {
        Constructor[] con = clazz.getConstructors();
        return Arrays.stream(con).filter(c -> c.getParameterTypes().length == argsCount).findFirst().orElse(con[0]);
    }

    public static List<ObjectInfo> filterTargets(Object[] obj) {
        return Arrays.stream(obj).filter(ObjectInfo.class::isInstance).map(ObjectInfo.class::cast).collect(Collectors.toList());
    }

    public void addTrack(Object[] obj, boolean deselectOthers, TrackSupplier supplier) {

    }

    public void addTrack(Object[] obj, Class<? extends Track> trackClass, Object[] extraArgs) {
        addTrack(obj, trackClass, extraArgs, false);
    }
    /**
     * Add a track to the specified objects.
     */
    public void addTrack(Object[] obj, Class<? extends Track> trackClass, Object[] extraArgs, boolean deselectOthers) {
        UndoRecord undo = new UndoRecord(window);
        Object[] args;
        if (extraArgs == null) {
            args = new Object[1];
        } else {
            args = new Object[extraArgs.length + 1];
            System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
        }

        Constructor<? extends Track> match = Score.getTrackConstructor(trackClass, args.length);
        List<Track> added = new Vector<>();
        try {
            for (ObjectInfo info : Score.filterTargets(obj)) {
                if (trackClass == PoseTrack.class) {
                    Object3D posable = info.getObject().getPosableObject();
                    if (posable == null)
                        continue;
                    if (posable != info.getObject()) {
                        String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};
                        BStandardDialog dlg = new BStandardDialog("", UIUtilities.breakString(Translate.text("mustConvertToActor", info.getName())), BStandardDialog.QUESTION);
                        int choice = dlg.showOptionDialog(window, options, options[0]);
                        if (choice == 1)
                            continue;
                        scene.replaceObject(info.getObject(), posable, undo);
                    }
                }
                undo.addCommand(UndoRecord.SET_TRACK_LIST, info, info.getTracks());
                args[0] = info;
                Track newtrack = match.newInstance(args);
                info.addTrack(newtrack, 0);
                added.add(newtrack);
            }
        } catch (ReflectiveOperationException ex) {
            log.atError().setCause(ex).log("Unable to create track: {}", ex.getMessage());
        }

        window.setUndoRecord(undo);
        if (deselectOthers) {
            theList.deselectAll();
        }
        rebuildList();
        added.forEach(tr -> theList.setSelected(tr, true));
        selectedTracksChanged();
        window.updateMenus();
    }

    /**
     * Edit the selected keyframe.
     */
    public void editSelectedKeyframe() {
        if (selection.length != 1) {
            return;
        }
        selection[0].track.editKeyframe(window, selection[0].keyIndex);
        tracksModified(true);
    }

    /**
     * Delete all selected keyframes.
     */
    public void deleteSelectedKeyframes() {
        Hashtable<Track, Track> changedTracks = new Hashtable<>();
        for (int i = 0; i < selection.length; i++) {
            Track tr = selection[i].track;
            Keyframe[] keys = tr.getTimecourse().getValues();
            for (int j = 0; j < keys.length; j++) {
                if (keys[j] == selection[i].key) {
                    if (changedTracks.get(tr) == null) {
                        changedTracks.put(tr, tr.duplicate(tr.getParent()));
                    }
                    tr.deleteKeyframe(j);
                    break;
                }
            }
        }
        selection = new SelectionInfo[0];
        UndoRecord undo = new UndoRecord(window);
        Enumeration<Track> tracks = changedTracks.keys();
        while (tracks.hasMoreElements()) {
            Track tr = tracks.nextElement();
            Object parent = tr.getParent();
            while (parent != null && parent instanceof Track) {
                parent = ((Track) parent).getParent();
            }
            if (parent instanceof ObjectInfo) {
                scene.applyTracksToObject((ObjectInfo) parent);
            }
            undo.addCommand(UndoRecord.COPY_TRACK, tr, changedTracks.get(tr));
        }
        window.setUndoRecord(undo);
        window.updateMenus();
        tracksModified(true);
    }

    private void clickedPlay() {
        if (isAnimating) {
            stopAnimating();
        } else {
            startAnimating();
        }
    }

    private void clickedRewind() {
        // Find the earliest keyframe on any track of any object.

        double minTime = Math.min(0.0, scene.getTime());
        for (ObjectInfo obj : scene.getObjects()) {
            for (Track track : obj.getTracks()) {
                double[] keyTimes = track.getKeyTimes();
                if (keyTimes.length > 0) {
                    minTime = Math.min(minTime, keyTimes[0]);
                }
            }
        }
        if (minTime != scene.getTime()) {
            window.setTime(minTime);
        }
    }

    private void clickedEnd() {
        // Find the latest keyframe on any track of any object.

        double maxTime = Math.max(0.0, scene.getTime());
        for (ObjectInfo obj : scene.getObjects()) {
            for (Track track : obj.getTracks()) {
                double[] keyTimes = track.getKeyTimes();
                if (keyTimes.length > 0) {
                    maxTime = Math.max(maxTime, keyTimes[keyTimes.length - 1]);
                }
            }
        }
        if (maxTime != scene.getTime()) {
            window.setTime(maxTime);
        }
    }

    private void speedChanged() {
        setPlaybackSeed(SPEEDS[speedSlider.getValue()]);
    }

    /**
     * EditingWindow methods. Most of these either do nothing, or simply call through to
     * the corresponding methods of the LayoutWindow the Score is in.
     */
    @Override
    public boolean confirmClose() {
        return true;
    }

    @Override
    public ToolPalette getToolPalette() {
        return modeTools;
    }

    @Override
    public void setTool(EditingTool tool) {
        if (view != viewTools.getSelection()) {
            view = viewTools.getSelection();
            layoutGraphs();
        }
        if (mode != modeTools.getSelection()) {
            mode = modeTools.getSelection();
            for (TrackDisplay graph : graphs) graph.setMode(mode);
            setHelpText(MODE_HELP_TEXT[mode]);
        }
    }

    @Override
    public void setHelpText(String text) {
        helpText.setText(text);
    }

    @Override
    public BFrame getFrame() {
        return window;
    }

    @Override
    public void updateImage() {
    }

    @Override
    public void updateMenus() {
    }

    @Override
    public void setUndoRecord(UndoRecord command) {
        window.setUndoRecord(command);
    }

    @Override
    public void setModified() {
        window.setModified();
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public ViewerCanvas getView() {
        return null;
    }

    @Override
    public ViewerCanvas[] getAllViews() {
        return null;
    }
}
