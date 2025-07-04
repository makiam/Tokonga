/*
 *  Copyright (C) 2007 by François Guillet
 *  Modifications Copyright (C) 2019 by Petri Ihalainen
 *  Changes copyright (C) 2022-2025 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the 
 *  terms of the GNU General Public License as published by the Free Software 
 *  Foundation; either version 2 of the License, or (at your option) any later version. 
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY 
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh;

import artofillusion.TextureParameter;
import artofillusion.UndoableEdit;
import artofillusion.math.BoundingBox;
import artofillusion.math.Vec2;
import artofillusion.object.FacetedMesh;
import artofillusion.object.Mesh;
import artofillusion.object.MeshVertex;
import artofillusion.polymesh.UVMappingData.UVMeshMapping;
import artofillusion.polymesh.UnfoldedMesh.UnfoldedEdge;
import artofillusion.polymesh.UnfoldedMesh.UnfoldedFace;
import artofillusion.polymesh.UnfoldedMesh.UnfoldedVertex;
import artofillusion.texture.Texture;
import artofillusion.texture.Texture2D;
import artofillusion.texture.UVMapping;
import buoy.event.RepaintEvent;
import buoy.widget.BScrollPane;
import buoy.widget.CustomWidget;
import lombok.Getter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

/**
 * This canvas displays several mesh pieces over a bitmap image. The goal is to
 * define UV mapping of meshes as the location of the mesh vertices over the
 * background image. Editing tools allow to move, rotate, resize meshes.
 *
 * @author François Guillet
 */
public class UVMappingCanvas extends CustomWidget {

    private final Dimension size; // widget size
    private Dimension oldSize; // old widget size used to track size change
    private final UnfoldedMesh[] meshes; // the mesh pieces to display
    private boolean[] selected;
    private int[] selectionDistance;
    private int currentPiece; // only one piece can be selected for edition
    private final UVMappingEditorDialog parent;
    private Point[] verticesPoints; // vertices locations only vertices with an id != -1 are displayed
    private UVMappingData.UVMeshMapping mapping; // current mapping
    private Rectangle dragBoxRect;
    private UVMappingManipulator manipulator;
    private final UVMappingData mappingData;
    private final Vec2 origin;
    private double scale;
    private double umin, umax, vmin, vmax;
    private Image textureImage;
    private int component;
    private boolean disableImageDisplay;
    private final MeshPreviewer preview;
    private int[][] vertIndexes;
    private int[][] vertMeshes;
    /**
     * -- GETTER --
     *
     * @return the current texture
     */
    @Getter
    private Texture texture;
    private UVMapping texMapping;
    private boolean boldEdges;
    private static final Stroke normal = new BasicStroke();
    private static final Stroke bold = new BasicStroke(2.0f);
    private static final Dimension minSize = new Dimension(640, 640);
    private static final Dimension maxSize = new Dimension(5000, 5000);
    private static final Color unselectedColor = new Color(0, 180, 0);
    private static final Color selectedColor = Color.red;
    private static final Color pinnedColor = new Color(182, 0, 185);
    private static final Color pinnedSelectedColor = new Color(255, 142, 255);
    private final Color bgColor2 = new Color(223, 223, 223);
    private final Color txAreaColor = new Color(255, 255, 255, 159);
    private final Color ink = new Color(63, 127, 191);

    /**
     * Construct a new UVMappingCanvas
     */
    public UVMappingCanvas(UVMappingEditorDialog window,
            UVMappingData mappingData,
            MeshPreviewer preview,
            Texture texture,
            UVMapping texMapping) {

        super();
        parent = window;
        this.preview = preview;
        this.texture = texture;
        this.texMapping = texMapping;
        this.mapping = mappingData.mappings.get(0);
        Color bgColor1 = new Color(239, 239, 239);
        setBackground(bgColor1);
        size = new Dimension(640, 640);
        oldSize = new Dimension(0, 0);
        origin = new Vec2();
        this.mappingData = mappingData;
        meshes = mappingData.getMeshes();
        boldEdges = true;
        addEventLink(RepaintEvent.class, this, "paintCanvas");
        if (meshes == null) {
            return;
        }
        fitRangeToAll();
        initializeTexCoordsIndex();
        updateTextureCoords();
        component = 0;
        createImage();
        currentPiece = 0;
        setSelectedPiece(currentPiece);
    }

    public boolean isBoldEdges() {
        return boldEdges;
    }

    public void setBoldEdges(boolean boldEdges) {
        this.boldEdges = boldEdges;
        repaint();
    }

    /**
     * @return the current mesh mapping
     */
    public UVMappingData.UVMeshMapping getMapping() {
        return mapping;
    }

    /**
     * @param mapping The mapping to set
     */
    public void setMapping(UVMappingData.UVMeshMapping mapping) {
        clearSelection();
        this.mapping = mapping;
        fitRangeToAll();
        update();
    }

    /**
     * @return the current texture mapping
     */
    public UVMapping getTexMapping() {
        return texMapping;
    }

    private void update() {
        createImage();
        updateTextureCoords();
        clearSelection();
        repaint();
        preview.render();
    }

    /**
     * @param texture The texture to set
     * @param texMapping The texture mapping to set
     */
    public void setTexture(Texture texture, UVMapping texMapping) {
        this.texture = texture;
        this.texMapping = texMapping;
        update();
    }

    /*
     * (non-Javadoc)
     * 
     * @see buoy.widget.Widget#getPreferredSize()
     */
    /**
     * The widget dimension adapts itself to the current size except if it's too
     * small
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension viewSize = ((BScrollPane) getParent()).getComponent().getSize();
        size.width = viewSize.width;
        size.height = viewSize.height;
        if (size.width < minSize.width) {
            size.width = minSize.width;
        } else if (size.width > maxSize.width) {
            size.width = maxSize.width;
        }
        if (size.height < minSize.height) {
            size.height = minSize.height;
        } else if (size.height > maxSize.height) {
            size.height = maxSize.height;
        }
        return size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see buoy.widget.Widget#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return minSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see buoy.widget.Widget#getMaximumSize()
     */
    @Override
    public Dimension getMaximumSize() {
        return maxSize;
    }

    /**
     * Draws the mesh pieces, on the current canvas
     */
    private void paintCanvas(RepaintEvent evt) {
        if (meshes == null) {
            return;
        }

        if (oldSize.width != size.width || oldSize.height != size.height) {
            vmax = origin.y + (size.height) / (2 * scale);
            vmin = origin.y - (size.height) / (2 * scale);
            umax = origin.x + (size.width) / (2 * scale);
            umin = origin.x - (size.width) / (2 * scale);
            parent.displayUVMinMax(umin, umax, vmin, vmax);
            createImage();
            refreshVerticesPoints();
            oldSize = new Dimension(size);
        }
        Graphics2D g = evt.getGraphics();

        if (textureImage != null) {
            g.drawImage(textureImage, 0, 0, null);
        } else {
            g.setColor(bgColor2);
            int x0 = (size.width % 40) / 2 - 20;
            int y0 = (size.height % 40) / 2 - 20;
            int x = x0, y;
            while (x < size.width) {
                y = y0;
                while (y < size.height) {
                    g.fillRect(x, y, 10, 10);
                    g.fillRect(x + 10, y + 10, 10, 10);
                    y += 20;
                }
                x += 20;
            }
            Point p1 = VertexToLayout(new Vec2(0, 0));
            Point p2 = VertexToLayout(new Vec2(1, 1));
            g.setColor(txAreaColor);
            g.fillRect(p1.x, p2.y, p2.x - p1.x, p1.y - p2.y);
        }
        drawGrid(g);
        for (int i = 0; i < meshes.length; i++) {
            UnfoldedMesh mesh = meshes[i];
            Vec2[] v = mapping.v[i];
            UnfoldedEdge[] e = mesh.getEdges();
            Point p1;
            Point p2;
            if (currentPiece == i) {
                g.setColor(mapping.edgeColor);
                if (boldEdges) {
                    g.setStroke(bold);
                }

            } else {
                g.setColor(Color.gray);
                g.setStroke(normal);
            }
            for (int j = 0; j < e.length; j++) {
                if (e[j].hidden) {
                    continue;
                }

                p1 = VertexToLayout(v[e[j].v1]);
                p2 = VertexToLayout(v[e[j].v2]);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
        g.setStroke(normal);
        for (int i = 0; i < verticesPoints.length; i++) {
            if (selected[i]) {
                if (mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned) {
                    g.setColor(pinnedSelectedColor);
                } else {
                    g.setColor(selectedColor);
                }

                g.drawOval(verticesPoints[i].x - 3,
                        verticesPoints[i].y - 3, 6, 6);
            } else {
                if (mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned) {
                    g.setColor(pinnedColor);
                } else {
                    g.setColor(unselectedColor);
                }

                g.fillOval(verticesPoints[i].x - 3,
                        verticesPoints[i].y - 3, 6, 6);
            }
        }
        if (dragBoxRect != null) {
            g.setColor(ink);
            g.drawRect(dragBoxRect.x, dragBoxRect.y, dragBoxRect.width, dragBoxRect.height);
        }
        if (manipulator != null) {
            manipulator.paint(g);
        }
    }

    private void drawGrid(Graphics2D g) {
        if (!parent.drawGrid()) {
            return;
        }
        if (scale < 2.0) {
            return;
        }

        // Must use the original AT from the Graphics2D because at startup the coordinates
        // are measured from the content pane of the UVMappingEditorDialog. Later on a new AT would do.
        AffineTransform aBefore = g.getTransform();
        AffineTransform aNow = new AffineTransform(aBefore);
        Point corner = VertexToLayout(new Vec2(0, 0));
        aNow.translate(corner.x, corner.y);
        aNow.scale(scale, -scale);
        g.setTransform(aNow);
        g.setStroke(new BasicStroke((float) (1.0 / scale)));

        //double opacity = Math.min(255*Math.sqrt(scale/640), 223);
        double opacity = Math.min(255.0 * Math.pow(scale / 520.0, 2.0 / 3.0), 223.0);
        Color ink1 = new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), (int) opacity);
        Color ink2 = new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), (int) (opacity / 1.6));
        Color ink3 = new Color(ink.getRed(), ink.getGreen(), ink.getBlue(), (int) (opacity / 2.5));
        double v = Math.round(vmin - 1.0);
        double u = Math.round(umin - 1.0);

        // Avoid to draw lines over already drawn lines because they are transparent
        while (u < umax) {
            g.setColor(ink1);
            g.draw(new Line2D.Double(u, vmin, u, vmax));
            if (scale > 75) {
                g.setColor(ink2);
                g.draw(new Line2D.Double(u + 0.5, vmin, u + 0.5, vmax));
            }
            if (scale > 200) {
                g.setColor(ink3);
                for (double d = 0.1; d < 0.5; d += 0.1) {
                    g.draw(new Line2D.Double(u + d, vmin, u + d, vmax));
                    g.draw(new Line2D.Double(u + 0.5 + d, vmin, u + 0.5 + d, vmax));
                }
            }
            u += 1.0;
        }
        while (v < vmax) {
            g.setColor(ink1);
            g.draw(new Line2D.Double(umin, v, umax, v));
            if (scale > 75) {
                g.setColor(ink2);
                g.draw(new Line2D.Double(umin, v + 0.5, umax, v + 0.5));
            }
            if (scale > 200) {
                g.setColor(ink3);
                for (double d = 0.1; d < 0.5; d += 0.1) {
                    g.draw(new Line2D.Double(umin, v + d, umax, v + d));
                    g.draw(new Line2D.Double(umin, v + 0.5 + d, umax, v + 0.5 + d));
                }
            }
            v += 1.0;
        }
        g.setTransform(aBefore);
    }

    public void fitToAll() {
        fitRangeToAll();
        repaint();
    }

    public void fitToSelection() {
        fitRangeToSelection();
        repaint();
    }

    /**
     * Computes range for fitting the mesh and the unit texture image
     * on the canvas.
     */
    public void fitRangeToAll() {
        vmin = umin = 0.0;
        vmax = umax = 1.0;
        Vec2[] v;
        UnfoldedVertex[] vert;
        for (int i = 0; i < meshes.length; i++) {
            v = mapping.v[i];
            vert = meshes[i].vertices;
            for (int j = 0; j < v.length; j++) {
                if (vert[j].id == -1) {
                    continue;
                }
                umin = Math.min(umin, v[j].x);
                umax = Math.max(umax, v[j].x);
                vmin = Math.min(vmin, v[j].y);
                vmax = Math.max(vmax, v[j].y);
            }
        }
        double margin = Math.max(umax - umin, vmax - vmin) * 0.02;
        umin -= margin;
        vmin -= margin;
        umax += margin;
        vmax += margin;
        setRange(umin, umax, vmin, vmax);
    }

    /**
     * Computes range for fitting the selected piece or the selected vertices
     * on the canvas. (As soon as I find the selected vertices.... now just the piece)
     */
    public void fitRangeToSelection() {
        vmin = umin = Double.MAX_VALUE;
        vmax = umax = -Double.MAX_VALUE;
        Vec2[] v = mapping.v[currentPiece];
        UnfoldedVertex[] vert = meshes[currentPiece].vertices;
        double margin;

        // Check if any vertices are selected and calculate range based on those
        int countSelected = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                countSelected++;
                if (vert[i].id == -1) {
                    continue;
                }
                umin = Math.min(umin, v[i].x);
                umax = Math.max(umax, v[i].x);
                vmin = Math.min(vmin, v[i].y);
                vmax = Math.max(vmax, v[i].y);
            }
        }
        if (countSelected == 1) {
            margin = 0.25; // To do: Find the next closest vertex and fit that in too with margin
        } else if (countSelected > 1) {
            margin = Math.max(umax - umin, vmax - vmin) * 0.25;
        } // If no vertices are selected, fit to the selected piece
        else {
            for (int j = 0; j < v.length; j++) {
                if (vert[j].id == -1) {
                    continue;
                }
                umin = Math.min(umin, v[j].x);
                umax = Math.max(umax, v[j].x);
                vmin = Math.min(vmin, v[j].y);
                vmax = Math.max(vmax, v[j].y);
            }
            margin = Math.max(umax - umin, vmax - vmin) * 0.07;
        }

        umin -= margin;
        vmin -= margin;
        umax += margin;
        vmax += margin;
        setRange(umin, umax, vmin, vmax);
    }

    /**
     * Sets the displayed UV range
     *
     * @param umin Low U limit
     * @param umax High U limit
     * @param vmin Low V Limit
     * @param vmax Hich V Limit
     */
    // Should not blend the uv-space coordinates with the screen aspect ratio.
    // Range needs to be as is given and the corners of the viewable area calculated
    // where they are needed.
    public void setRange(double umin, double umax, double vmin, double vmax) {
        this.umin = umin;
        this.umax = umax;
        this.vmin = vmin;
        this.vmax = vmax;
        scale = ((double) (size.width)) / (umax - umin);
        double scaley = ((double) (size.height)) / (vmax - vmin);
        if (scaley < scale) {
            scale = scaley;
        }
        origin.x = (umax + umin) / 2;
        origin.y = (vmax + vmin) / 2;
        this.vmax = origin.y + (size.height) / (2 * scale);
        this.vmin = origin.y - (size.height) / (2 * scale);
        this.umax = origin.x + (size.width) / (2 * scale);
        this.umin = origin.x - (size.width) / (2 * scale);
        createImage();
        refreshVerticesPoints();
        parent.displayUVMinMax(this.umin, this.umax, this.vmin, this.vmax);
    }

    public Range getRange() {
        Range range = new Range();
        range.umin = umin;
        range.umax = umax;
        range.vmin = vmin;
        range.vmax = vmax;
        return range;
    }

    /**
     * Recomputes mesh vertices positions whenever origin or scaling has changed
     */
    public void refreshVerticesPoints() {
        Vec2[] v = mapping.v[currentPiece];
        int count = mappingData.displayed[currentPiece];
        if (verticesPoints == null || verticesPoints.length != count) {
            verticesPoints = new Point[count];
        }
        for (int j = 0; j < count; j++) {
            verticesPoints[j] = VertexToLayout(v[mappingData.verticesTable[currentPiece][j]]);
        }
    }

    /**
     * This method updates the preview when the selection has been changed
     */
    public void updatePreview() {
        Mesh mesh = (Mesh) preview.getObject().getGeometry();
        MeshVertex[] vert = mesh.getVertices();
        UnfoldedMesh umesh = meshes[currentPiece];
        UnfoldedVertex[] uvert = umesh.getVertices();
        boolean[] meshSel = new boolean[vert.length];
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                meshSel[uvert[mappingData.verticesTable[currentPiece][i]].id] = true;
            }
        }
        preview.setVertexSelection(meshSel);
        preview.render();
    }

    /**
     * @return the vertex selection
     */
    public boolean[] getSelection() {
        return selected;
    }

    public void setSelection(boolean[] selected) {
        setSelection(selected, true);
    }

    /**
     * Sets the selected vertices
     *
     * @param selected
     */
    public void setSelection(boolean[] selected, boolean render) {
        if (selected.length == verticesPoints.length) {
            int[] selChange = checkSelectionChange(this.selected, selected);
            if (selChange != null) {
                parent.addUndoCommand(new SelectionCommand(selChange));
            }
            this.selected = selected;
            if (render) {
                updatePreview();
            } else {
                preview.clearVertexSelection();
            }
            if (parent.tensionOn()) {
                findSelectionDistance();
            }
            repaint();
        }
    }

    /**
     * Computes the difference between to vertex selections
     *
     * @param sel1
     * @param sel2
     * @return An array describing the selection differences
     */
    public int[] checkSelectionChange(boolean[] sel1, boolean[] sel2) {
        if (sel1.length != sel2.length) {
            return null;
        }

        int count = 0;
        for (int i = 0; i < sel1.length; i++) {
            if (sel1[i] != sel2[i]) {
                count++;
            }
        }

        if (count != 0) {
            int[] selChange = new int[count];
            count = 0;
            for (int i = 0; i < sel1.length; i++) {
                if (sel1[i] != sel2[i]) {
                    selChange[count] = i;
                    count++;
                }
            }
            return selChange;

        } else {
            return null;
        }
    }

    public void clearSelection() {
        int count = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                count++;
            }
        }
        int[] selChange = null;
        if (count > 0) {
            selChange = new int[count];
            count = 0;
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    selChange[count] = i;
                    selected[i] = false;
                    count++;
                }
            }
            parent.addUndoCommand(new SelectionCommand(selChange));
        }
        preview.clearVertexSelection();
    }

    /**
     * @return the selected piece index
     */
    public int getSelectedPiece() {
        return currentPiece;
    }

    /**
     * Sets the piece selected for edition
     *
     * @param currentPiece the piece currently selected
     */
    public void setSelectedPiece(int currentPiece) {
        this.currentPiece = currentPiece;
        refreshVerticesPoints();
        selected = new boolean[verticesPoints.length];
        repaint();
    }

    /**
     * @return the vertices 2D points on canvas (only for displayed vertices)
     */
    public Point[] getVerticesPoints() {
        return verticesPoints;
    }

    /**
     * Sets the current drag box and repaints the mesh. Set the drag box to null
     * in order to stop the drag box display.
     *
     * @param dragBoxRect The drag box to display
     */
    public void setDragBox(Rectangle dragBoxRect) {
        this.dragBoxRect = dragBoxRect;
        repaint();
    }

    /**
     * Sets the manipulator that manipulates mesh pieces
     *
     * @param manipulator
     */
    public void setManipulator(UVMappingManipulator manipulator) {
        this.manipulator = manipulator;
    }

    /**
     * Sets the positions of vertices relative to view window (not to UV
     * values). If mask is not null, change is applied only for points for which
     * mask is true.
     *
     * @param newPos
     * @param mask
     */
    public void setPositions(Point[] newPos, boolean[] mask) {
        Vec2[] v = mapping.v[currentPiece];
        for (int i = 0; i < newPos.length; i++) {
            if (mask == null || mask[i]) {
                LayoutToVertex(v[mappingData.verticesTable[currentPiece][i]], newPos[i]);
            }
        }
    }

    /**
     * This function returns the rectangle that encloses the mesh, taking into
     * accout mesh origin, orientation and scale
     *
     * @return mesh bounds
     */
    public BoundingBox getBounds(UnfoldedMesh mesh) {
        int xmin, xmax, ymin, ymax;
        xmax = ymax = Integer.MIN_VALUE;
        xmin = ymin = Integer.MAX_VALUE;
        Point p;
        Vec2[] v = mapping.v[currentPiece];
        for (int i = 0; i < v.length; i++) {
            p = VertexToLayout(v[i]);
            if (xmax < p.x) {
                xmax = p.x;
            }
            if (xmin > p.x) {
                xmin = p.x;
            }
            if (ymax < p.y) {
                ymax = p.y;
            }
            if (ymin > p.y) {
                ymin = p.y;
            }
        }
        BoundingBox b = new BoundingBox(xmin, xmax, ymin, ymax, 0, 0);
        return b;
    }

    /**
     * Computes the position of a vertex on the layout
     *
     * @param r The vertex position
     * @return The position on the layout
     */
    public Point VertexToLayout(Vec2 r) {
        Point p = new Point();
        p.x = (int) Math.round((r.x - origin.x) * scale);
        p.y = (int) Math.round((r.y - origin.y) * scale);
        p.x += size.width / 2;
        p.y = size.height / 2 - p.y;
        return p;
    }

    /**
     * Computes the position of a vertex given its position on the layout
     *
     * @param p The new vertex position
     * @param v The vertex
     */
    public void LayoutToVertex(Vec2 v, Point p) {
        v.x = (p.x - size.width / 2) / scale + origin.x;
        v.y = (size.height / 2 - p.y) / scale + origin.y;
    }

    public Vec2 LayoutToVertex(Point p) {
        Vec2 v = new Vec2();
        v.x = (p.x - size.width / 2) / scale + origin.x;
        v.y = (size.height / 2 - p.y) / scale + origin.y;
        return v;
    }

    public void setComponent(int component) {
        this.component = component;
        createImage();
        repaint();
    }

    /**
     * Recalculate the texture image.
     */
    private void createImage() {
        textureImage = null;
        if (disableImageDisplay) {
            return;
        }
        if (texture == null) {
            return;
        }
        int sampling = mappingData.sampling;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        double uoffset = 0; // 0.5 * sampling * (umax - umin) / size.width;
        double voffset = 0; // 0.5 * sampling * (vmax - vmin) / size.height;
        TextureParameter[] param = texMapping.getParameters();
        double[] paramVal = null;
        if (param != null) {
            paramVal = new double[param.length];
            for (int i = 0; i < param.length; i++) {
                paramVal[i] = param[i].defaultVal;
            }
        }
        textureImage = ((Texture2D) texture.
                duplicate()).
                createComponentImage(umin + uoffset,
                        umax + uoffset,
                        vmin - voffset,
                        vmax - voffset,
                        size.width / sampling,
                        size.height / sampling,
                        component,
                        0.0,
                        paramVal);
        if (sampling > 1) {
            textureImage = textureImage.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
        }
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Sets the texture resolution
     *
     * @param sampling
     */
    public void setSampling(int sampling) {
        mappingData.sampling = sampling;
        createImage();
        repaint();
    }

    /**
     * Returns the current texture resolution
     *
     * @return Sampling
     */
    public int getSampling() {
        return mappingData.sampling;
    }

    /**
     * Scales the selected piece
     *
     * @param sc
     */
    public void scale(double sc) {
        umin = sc * (umin - origin.x) + origin.x;
        umax = sc * (umax - origin.x) + origin.x;
        vmin = sc * (vmin - origin.y) + origin.y;
        vmax = sc * (vmax - origin.y) + origin.y;
        scale /= sc;
        createImage();
        refreshVerticesPoints();
        repaint();
        parent.displayUVMinMax(umin, umax, vmin, vmax);
    }

    /**
     * Disables texture display in order to speed up operations
     */
    public void disableImageDisplay() {
        disableImageDisplay = true;
        textureImage = null;
    }

    /**
     * Enables image display
     */
    public void enableImageDisplay() {
        disableImageDisplay = false;
        createImage();
        repaint();
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(double scale) {
        double f = this.scale / scale;
        umin = f * (umin - origin.x) + origin.x;
        umax = f * (umax - origin.x) + origin.x;
        vmin = f * (vmin - origin.y) + origin.y;
        vmax = f * (vmax - origin.y) + origin.y;
        this.scale = scale;
        createImage();
        refreshVerticesPoints();
        repaint();
        parent.displayUVMinMax(umin, umax, vmin, vmax);
    }

    /**
     * Displaces displayed uv range
     *
     * @param du Shift along U
     * @param dv Shift along V
     */
    public void moveOrigin(double du, double dv) {
        umin += du;
        umax += du;
        vmin += dv;
        vmax += dv;
        origin.x += du;
        origin.y += dv;
        createImage();
        refreshVerticesPoints();
        repaint();
        parent.displayUVMinMax(umin, umax, vmin, vmax);
    }

    /**
     * @return the origin
     */
    public Vec2 getOrigin() {
        return new Vec2(origin);
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(Vec2 origin) {
        setOrigin(origin.x, origin.y);
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(double u, double v) {
        moveOrigin(u - origin.x, v - origin.y);
    }

    /**
     * Sets texture coordinate indices for later use
     */
    public void initializeTexCoordsIndex() {
        FacetedMesh mesh = (FacetedMesh) preview.getObject().getGeometry();
        int[][] texCoordIndex = new int[mesh.getFaceCount()][];
        vertIndexes = new int[texCoordIndex.length][];
        vertMeshes = new int[texCoordIndex.length][];
        int n;
        for (int i = 0; i < texCoordIndex.length; i++) {
            n = mesh.getFaceVertexCount(i);
            texCoordIndex[i] = new int[n];
            vertIndexes[i] = new int[n];
            vertMeshes[i] = new int[n];
            for (int j = 0; j < texCoordIndex[i].length; j++) {
                texCoordIndex[i][j] = mesh.getFaceVertexIndex(i, j);
                vertIndexes[i][j] = -1;
                vertMeshes[i][j] = -1;
            }
        }
        UnfoldedFace[] f;
        UnfoldedVertex[] v;
        int count = 0;
        for (int i = 0; i < meshes.length; i++) {
            v = meshes[i].getVertices();
            count += v.length;
        }
        count = 0;
        for (int i = 0; i < meshes.length; i++) {
            f = meshes[i].getFaces();
            v = meshes[i].getVertices();
            for (int j = 0; j < f.length; j++) {
                if (f[j].id >= 0 && f[j].id < texCoordIndex.length) {
                    for (int k = 0; k < texCoordIndex[f[j].id].length; k++) {
                        if (f[j].v1 >= 0 && v[f[j].v1].id == texCoordIndex[f[j].id][k]) {
                            vertIndexes[f[j].id][k] = f[j].v1;
                            vertMeshes[f[j].id][k] = i;
                        }
                        if (f[j].v2 >= 0 && v[f[j].v2].id == texCoordIndex[f[j].id][k]) {
                            vertIndexes[f[j].id][k] = f[j].v2;
                            vertMeshes[f[j].id][k] = i;
                        }
                        if (f[j].v3 >= 0 && v[f[j].v3].id == texCoordIndex[f[j].id][k]) {
                            vertIndexes[f[j].id][k] = f[j].v3;
                            vertMeshes[f[j].id][k] = i;
                        }
                    }
                }
            }
            count += v.length;
        }
    }

    /**
     * Updates texture coordinates to reflect the mapping
     */
    public void updateTextureCoords() {
        if (texture == null) {
            return;
        }
        FacetedMesh mesh = (FacetedMesh) preview.getObject().getGeometry();
        Vec2[][] texCoord = new Vec2[mesh.getFaceCount()][];
        for (int i = 0; i < texCoord.length; i++) {
            texCoord[i] = new Vec2[mesh.getFaceVertexCount(i)];
            for (int j = 0; j < texCoord[i].length; j++) {
                texCoord[i][j] = new Vec2(mapping.v[vertMeshes[i][j]][vertIndexes[i][j]]);
            }
        }
        texMapping.setFaceTextureCoordinates(preview.getObject().getGeometry(), texCoord);
        preview.render();
    }

    public void selectAll() {
        for (int i = 0; i < verticesPoints.length; i++) {
            selected[i] = true;
        }
        setSelection(selected);
        manipulator.selectionUpdated();
    }

    public UnfoldedMesh[] getMeshes() {
        return meshes;
    }

    public void pinSelection(boolean state) {
        int count = 0;
        for (int i = 0; i < verticesPoints.length; i++) {
            if (selected[i] && mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned != state) {
                count++;
            }
        }

        if (count != 0) {
            int[] pinChange = new int[count];
            count = 0;
            for (int i = 0; i < verticesPoints.length; i++) {
                if (selected[i] && mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned != state) {
                    mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned = state;
                    pinChange[count] = i;
                    count++;
                }
            }
            parent.addUndoCommand(new PinCommand(pinChange));
            repaint();
        }
    }

    public boolean isPinned(int i) {
        return mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][i]].pinned;
    }

    /**
     * Given the index of a displayed vertex, returns the index of this vertex
     * with respect to the unfolded piece of mesh.
     * This comes from the fact that not all vertices are displayed when the
     * original mesh is not a triangle mesh
     *
     * @param index The index of the displayed vertex
     * @return The index within the unfolded mesh
     */
    public int getTrueIndex(int index) {
        return mappingData.verticesTable[currentPiece][index];
    }

    public UVMappingEditorDialog getParentDialog() {
        return parent;
    }

    /**
     * Calculate the distance (in edges) between each vertex and the nearest
     * selected vertex.
     */
    // from triangle mesh editor
    public void findSelectionDistance() {
        int i, j;
        UnfoldedMesh mesh = meshes[currentPiece];
        int[] dist = new int[mesh.getVertices().length];
        UnfoldedEdge[] e = mesh.getEdges();
        int maxDistance = parent.getMaxTensionDistance();

        // First, set each distance to 0 or -1, depending on whether that vertex
        // is part of the
        // current selection.
        for (i = 0; i < selected.length; i++) {
            dist[mappingData.verticesTable[currentPiece][i]] = selected[i] ? 0 : -1;
        }

        // Now extend this outward up to maxDistance.
        for (i = 0; i < maxDistance; i++) {
            for (j = 0; j < e.length; j++) {
                if (e[j].hidden) {
                    continue;
                }
                if (dist[e[j].v1] == -1 && dist[e[j].v2] == i) {
                    dist[e[j].v1] = i + 1;
                } else if (dist[e[j].v2] == -1 && dist[e[j].v1] == i) {
                    dist[e[j].v2] = i + 1;
                }
            }
        }
        selectionDistance = new int[selected.length];
        for (i = 0; i < selected.length; i++) {
            selectionDistance[i] = dist[mappingData.verticesTable[currentPiece][i]];
        }
    }

    /**
     * Given a list of deltas which will be added to the selected vertices,
     * calculate the corresponding deltas for the unselected vertices according
     * to the mesh tension.
     */
    public void adjustDeltas(Vec2[] delta) {
        int[] dist = getSelectionDistance();
        int[] count = new int[delta.length];
        UnfoldedMesh mesh = meshes[currentPiece];
        UnfoldedEdge[] edge = mesh.getEdges();
        int maxDistance = parent.getMaxTensionDistance();
        double tension = parent.getTensionValue();
        double[] scale = new double[maxDistance + 1];

        for (int i = 0; i < delta.length; i++) {
            if (dist[i] != 0) {
                delta[i].set(0.0, 0.0);
            }
        }
        int v1, v2;
        for (int i = 0; i < maxDistance; i++) {
            for (int j = 0; j < count.length; j++) {
                count[j] = 0;
            }
            for (int j = 0; j < edge.length; j++) {
                v1 = mappingData.invVerticesTable[currentPiece][edge[j].v1];
                v2 = mappingData.invVerticesTable[currentPiece][edge[j].v2];
                if (v1 == -1 || v2 == -1) {
                    continue;
                }
                if (dist[v1] == i && dist[v2] == i + 1) {
                    count[v2]++;
                    delta[v2].add(delta[v1]);
                } else if (dist[v2] == i && dist[v1] == i + 1) {
                    count[v1]++;
                    delta[v1].add(delta[v2]);
                }
            }
            for (int j = 0; j < count.length; j++) {
                if (count[j] > 1) {
                    delta[j].scale(1.0 / count[j]);
                }
            }
        }
        for (int i = 0; i < scale.length; i++) {
            scale[i] = Math.pow((maxDistance - i + 1.0) / (maxDistance + 1.0), tension);
        }
        for (int i = 0; i < delta.length; i++) {
            if (dist[i] > 0) {
                delta[i].scale(scale[dist[i]]);
            }
        }
    }

    public int[] getSelectionDistance() {
        return selectionDistance;
    }

    /**
     * Undo/Redo command for whole set of mesh pieces vertices change
     */
    public class MappingPositionsCommand implements UndoableEdit {

        private Vec2[][] oldPos;
        private Vec2[][] newPos;
        private double oldUmin, oldUmax, oldVmin, oldVmax;
        private double newUmin, newUmax, newVmin, newVmax;

        public MappingPositionsCommand() {
            oldPos = newPos = null;
        }

        public MappingPositionsCommand(Vec2[][] oldPos, Vec2[][] newPos) {
            this.oldPos = oldPos;
            this.newPos = newPos;
        }

        public void setOldRange(double oldUmin, double oldUmax, double oldVmin, double oldVmax) {
            this.oldUmin = oldUmin;
            this.oldUmax = oldUmax;
            this.oldVmin = oldVmin;
            this.oldVmax = oldVmax;
        }

        public void setNewRange(double newUmin, double newUmax, double newVmin, double newVmax) {
            this.newUmin = newUmin;
            this.newUmax = newUmax;
            this.newVmin = newVmin;
            this.newVmax = newVmax;
        }

        /**
         * @return the newPos
         */
        public Vec2[][] getNewPos() {
            return newPos;
        }

        /**
         * @param newPos the newPos to set
         */
        public void setNewPos(Vec2[][] newPos) {
            this.newPos = new Vec2[newPos.length][];
            for (int i = 0; i < newPos.length; i++) {
                this.newPos[i] = new Vec2[newPos[i].length];
                for (int j = 0; j < newPos[i].length; j++) {
                    this.newPos[i][j] = new Vec2(newPos[i][j]);
                }
            }
        }

        /**
         * @return the oldPos
         */
        public Vec2[][] getOldPos() {
            return oldPos;
        }

        /**
         * @param oldPos the oldPos to set
         */
        public void setOldPos(Vec2[][] oldPos) {
            this.oldPos = new Vec2[oldPos.length][];
            for (int i = 0; i < oldPos.length; i++) {
                this.oldPos[i] = new Vec2[oldPos[i].length];
                for (int j = 0; j < oldPos[i].length; j++) {
                    this.oldPos[i][j] = new Vec2(oldPos[i][j]);
                }
            }
        }

        @Override
        public void redo() {
            for (int i = 0; i < mapping.v.length; i++) {
                for (int j = 0; j < mapping.v[i].length; j++) {
                    mapping.v[i][j] = new Vec2(newPos[i][j]);
                }
            }

            setRange(newUmin, newUmax, newVmin, newVmax);
            manipulator.selectionUpdated();
            repaint();
        }

        @Override
        public void undo() {
            for (int i = 0; i < mapping.v.length; i++) {
                for (int j = 0; j < mapping.v[i].length; j++) {
                    mapping.v[i][j] = new Vec2(oldPos[i][j]);
                }
            }
            setRange(oldUmin, oldUmax, oldVmin, oldVmax);
            manipulator.selectionUpdated();
            repaint();
        }
    }

    /**
     * Undo/Redo command for pinning vertices
     */
    public class PinCommand implements UndoableEdit {

        public final int[] selection;

        public PinCommand(int[] selection) {
            this.selection = selection;
        }

        @Override
        public void redo() {
            for (int i = 0; i < selection.length; i++) {
                mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][selection[i]]].pinned
                        = !mappingData.meshes[currentPiece].vertices[mappingData.verticesTable[currentPiece][selection[i]]].pinned;
            }
            repaint();
        }

        @Override
        public void undo() {
            redo();
        }
    }

    /**
     * Undo/Redo command for selected vertices
     */
    public class SelectionCommand implements UndoableEdit {

        private final int[] selection;

        public SelectionCommand(int[] selection) {
            super();
            this.selection = selection;
        }

        @Override
        public void redo() {
            for (int i = 0; i < selection.length; i++) {
                selected[selection[i]] = !selected[selection[i]];
            }
            manipulator.selectionUpdated();
            repaint();
        }

        @Override
        public void undo() {
            redo();
        }
    }

    /**
     * Undo/Redo command for dragged vertices
     *
     * @author François Guillet
     */
    public class DragMappingVerticesCommand implements UndoableEdit {

        private final int[] vertIndices;
        private final Vec2[] undoPositions;
        private final Vec2[] redoPositions;
        private final UVMeshMapping mapping;
        private final int piece;

        /**
         * Creates a DragMappingVerticesCommand
         *
         * @param vertIndexes The indexes of vertices to move
         * @param undoPositions The original positions
         * @param redoPositions The positions to move to
         */
        public DragMappingVerticesCommand(int[] vertIndexes,
                Vec2[] undoPositions,
                Vec2[] redoPositions,
                UVMeshMapping mapping,
                int piece) {
            this.vertIndices = vertIndexes;
            this.undoPositions = undoPositions;
            this.redoPositions = redoPositions;
            this.mapping = mapping;
            this.piece = piece;
        }

        @Override
        public void redo() {
            Vec2[] v = mapping.v[piece];
            for (int i = 0; i < vertIndices.length; i++) {
                v[vertIndices[i]] = new Vec2(redoPositions[i]);
            }
            refreshVerticesPoints();
            manipulator.selectionUpdated();
            repaint();
        }

        @Override
        public void undo() {
            Vec2[] v = mapping.v[piece];
            for (int i = 0; i < vertIndices.length; i++) {
                v[vertIndices[i]] = new Vec2(undoPositions[i]);
            }
            refreshVerticesPoints();
            manipulator.selectionUpdated();
            repaint();
        }
    }

    public class Range {

        public double umin;
        public double umax;
        public double vmin;
        public double vmax;
    }
}
