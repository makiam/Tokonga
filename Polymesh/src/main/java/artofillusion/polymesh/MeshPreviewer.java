/*
    Copyright (C) 1999-2005 by Peter Eastman, 2007 by Francois Guillet
    Modifications Copyright (C) 2019 by Petri Ihalainen
    Changes copyright (C) 2023-2025 by Maksim Khramov

    This program is free software; you can redistribute it and/or modify it under the 
    terms of the GNU General Public License as published by the Free Software 
    Foundation; either version 2 of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
    PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh;

import artofillusion.ArtOfIllusion;
import artofillusion.Camera;
import artofillusion.RenderListener;
import artofillusion.Renderer;
import artofillusion.Scene;
import artofillusion.WireframeMesh;
import artofillusion.image.*;
import artofillusion.material.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import static artofillusion.ui.UIUtilities.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.Timer;

/**
 * MeshPreviewer is a component used for rendering previews of Mesh.
 */
public class MeshPreviewer extends CustomWidget implements RenderListener {

    Scene theScene;
    Camera theCamera;
    ObjectInfo info;
    CoordinateSystem objectCoords;
    Image theImage;
    boolean mouseInside;
    boolean renderInProgress;
    Point clickPoint;
    private Mat4 dragTransform;
    public static final int HANDLE_SIZE = 5;
    static final double DRAG_SCALE = Math.PI / 360.0; // Half a degree
    ArrayList<ObjectInfo> selection;
    ArrayList<Vec3> selPos;
    private Sphere sphere;
    private int spheresIndex;
    private boolean showSelection;
    private double boundR; // Bound radius is used as reference size of the object.
    private boolean reverseWheel;
    private int scrollAmount;
    private Timer scrollTimer;

    /**
     * Same as above, except you can specify a different object to use
     * instead of a sphere.
     */
    public MeshPreviewer(Texture tex, Material mat, Object3D obj, int width, int height) {
        ObjectInfo objInfo = new ObjectInfo(obj, new CoordinateSystem(), "");
        initObject(tex, mat, objInfo);
        init(objInfo, width, height);
    }

    /**
     * Create a MaterialPreviewer to display the specified object, with its
     * current texture and material.
     */
    public MeshPreviewer(ObjectInfo obj, int width, int height) {
        init(obj.duplicate(), width, height);
    }

    /**
     * Initialize the object's texture and material.
     */
    private void initObject(Texture tex, Material mat, ObjectInfo objInfo) {
        if (tex == null) {
            tex = UniformTexture.invisibleTexture();
        }
        objInfo.setTexture(tex, tex.getDefaultMapping(objInfo.object));
        if (mat != null) {
            objInfo.setMaterial(mat, mat.getDefaultMapping(objInfo.object));
        }
    }

    /**
     * Initialize the MaterialPreviewer.
     */
    private void init(ObjectInfo obj, int width, int height) {

        boundR = obj.getBounds().getSize().length() * 0.5;
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, 8.0 * boundR),
                new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        Vec3[] vert = new Vec3[]{new Vec3(100.0 * boundR, -boundR, 100.0 * boundR),
            new Vec3(-100.0 * boundR, -boundR, 100.0 * boundR),
            new Vec3(0.0, -boundR, -100.0 * boundR)};
        int[][] face = {{0, 1, 2}};
        TriangleMesh tri;
        theScene = new Scene();
        theCamera = new Camera();
        theCamera.setCameraCoordinates(coords);
        coords = new CoordinateSystem(new Vec3(), new Vec3(-0.5, -0.4, -1.0), Vec3.vy());
        theScene.addObject(new DirectionalLight(new RGBColor(1.0f, 1.0f, 1.0f), 0.8f), coords, "", null);
        coords = new CoordinateSystem(new Vec3(), Vec3.vz(), Vec3.vy());
        theScene.addObject(tri = new TriangleMesh(vert, face), coords, "", null);
        Texture tex = theScene.getDefaultTexture();
        tri.setTexture(tex, tex.getDefaultMapping(tri));
        info = obj;
        objectCoords = info.coords = new CoordinateSystem();
        theScene.addObject(info, null);
        setPreferredSize(new Dimension(width, height));
        addEventLink(MousePressedEvent.class, this, "mousePressed");
        addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
        addEventLink(MouseEnteredEvent.class, this, "mouseEntered");
        addEventLink(MouseExitedEvent.class, this, "mouseExited");
        addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
        addEventLink(MouseScrolledEvent.class, this, "mouseScrolled");
        addEventLink(RepaintEvent.class, this, "paint");

        // Set up other listeners.
        getComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ev) {
                render();
            }
        });
        getComponent().addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent ev) {
                if ((ev.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                    if (!getComponent().isDisplayable()) {
                        Renderer rend = ArtOfIllusion.getPreferences().getTexturePreviewRenderer();
                        if (rend != null) {
                            rend.cancelRendering(theScene);
                        }
                    }
                }
            }
        });
        selection = new ArrayList<>();
        selPos = new ArrayList<>();
        UniformTexture selTexture;
        theScene.addTexture(selTexture = new UniformTexture());
        selTexture.diffuseColor = new RGBColor(255, 0, 0);
        sphere = new Sphere(0.03, 0.03, 0.03);
        sphere.setTexture(selTexture, selTexture.getDefaultMapping(sphere));
        spheresIndex = theScene.getNumObjects();
        showSelection = true;
        reverseWheel = ArtOfIllusion.getPreferences().getReverseZooming();
        scrollAmount = 0;
        scrollTimer = new Timer(250, (ActionEvent e) -> mouseStoppedScrolling());
        render();
    }

    /**
     * Get the object on which the texture and material are being displayed.
     */
    public ObjectInfo getObject() {
        return info;
    }

    /*
     * The following methods are used to modify the properties of the object
     * being displayed.
     */
    public void setTexture(Texture tex, TextureMapping map) {
        if (tex == null) {
            tex = UniformTexture.invisibleTexture();
        }
        if (map == null) {
            map = tex.getDefaultMapping(info.object);
        }
        info.setTexture(tex, map);
    }

    public void setMaterial(Material mat, MaterialMapping map) {
        info.setMaterial(mat, map);
    }

    /**
     * Render the preview.
     */
    public synchronized void render() {
        Renderer rend = ArtOfIllusion.getPreferences()
                .getTexturePreviewRenderer();
        if (rend == null) {
            return;
        }
        rend.cancelRendering(theScene);
        Rectangle bounds = getBounds();
        if (bounds.width == 0 || bounds.height == 0) {
            return;
        }
        SceneCamera sc = new SceneCamera();
        sc.setFieldOfView(16.0);
        theCamera.setScreenTransform(sc.getScreenTransform(bounds.width, bounds.height),
                bounds.width, bounds.height);
        rend.configurePreview();
        rend.renderScene(theScene, theCamera, this, sc);
        renderInProgress = true;
        repaint();
    }

    /**
     * Cancel rendering.
     */
    public synchronized void cancelRendering() {
        Renderer rend = ArtOfIllusion.getPreferences().getTexturePreviewRenderer();
        if (rend != null) {
            rend.cancelRendering(theScene);
        }
    }

    private void paint(RepaintEvent ev) {
        Graphics2D g = ev.getGraphics();
        if (theImage != null) {
            g.drawImage(theImage, 0, 0, getComponent());
        }
        if (mouseInside) {
            drawHilight(g);
        }
        if (renderInProgress) {
            Rectangle bounds = getBounds();
            g.setColor(Color.red);
            g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
        }
    }

    private void drawHilight(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.red);
        g.fillRect(0, 0, HANDLE_SIZE, HANDLE_SIZE);
        g.fillRect(bounds.width - HANDLE_SIZE, 0, HANDLE_SIZE, HANDLE_SIZE);
        g.fillRect(0, bounds.height - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
        g.fillRect(bounds.width - HANDLE_SIZE, bounds.height - HANDLE_SIZE, HANDLE_SIZE, HANDLE_SIZE);
    }

    private void drawObject(Graphics g) {
        g.setColor(Color.gray);
        Vec3 origin = objectCoords.getOrigin();
        Mat4 m = objectCoords.fromLocal();
        m = Mat4.translation(-origin.x, -origin.y, -origin.z).times(m);
        m = dragTransform.times(m);
        m = Mat4.translation(origin.x, origin.y, origin.z).times(m);
        theCamera.setObjectTransform(m);
        WireframeMesh mesh = info.object.getWireframeMesh();
        int[] from = mesh.from;
        int[] to = mesh.to;
        int last = -1;
        Vec3[] vert = mesh.vert;
        for (int i = 0; i < mesh.from.length; i++) {
            if (from[i] == last) {
                theCamera.drawClippedLineTo(g, vert[(last = to[i])]);
            } else {
                theCamera.drawClippedLine(g, vert[from[i]], vert[(last = to[i])]);
            }
        }
    }

    /**
     * Rotate the object to show a specific side.
     */
    private void changeView(int view) {
        double[][] angles = new double[][]{{0.0, 0.0, 0.0}, {0.0, 180.0, 0.0},
        {0.0, -90.0, 0.0}, {0.0, 90.0, 0.0},
        {-90.0, 0.0, 0.0}, {90.0, 0.0, 0.0}};
        objectCoords.setOrientation(angles[view][0], angles[view][1], angles[view][2]);
        objectCoords.setOrigin(new Vec3());
        updateSelectionPositions();
        render();
    }

    /**
     * Called when more pixels are available for the current image.
     */
    @Override
    public void imageUpdated(Image image) {
        theImage = image;
        repaint();
    }

    /**
     * The renderer may call this method periodically during rendering, to
     * give the listener text descriptions of the current status of
     * rendering.
     */
    @Override
    public void statusChanged(String status) {
    }

    /**
     * Called when rendering is complete.
     */
    @Override
    public void imageComplete(ComplexImage image) {
        theImage = image.getImage();
        renderInProgress = false;
        repaint();
    }

    /**
     * Called when rendering is cancelled.
     */
    @Override
    public void renderingCanceled() {
    }

    private void mouseEntered(MouseEnteredEvent e) {
        mouseInside = true;
        Graphics g = getComponent().getGraphics();
        drawHilight(g);
        g.dispose();
    }

    private void mouseExited(MouseExitedEvent e) {
        mouseInside = false;
        repaint();
    }

    private void mousePressed(MousePressedEvent e) {
        Graphics g = getComponent().getGraphics();
        clickPoint = e.getPoint();
        Renderer rend = ArtOfIllusion.getPreferences().getTexturePreviewRenderer();
        if (rend != null) {
            rend.cancelRendering(theScene);
        }
        dragTransform = Mat4.identity();
        drawObject(g);
        g.dispose();
    }

    private void mouseReleased(MouseReleasedEvent e) {
        if (clickPoint == null) {
            return;
        }
        Point dragPoint = e.getPoint();

        // Why is dragTransform recalculated here, nothing is moving any more?
        if (!clickPoint.equals(dragPoint)) {
            if (mouseButtonThree(e)) {
                if (e.isControlDown()) {
                    dragTransform = Mat4.translation(0.0, 0.0, (dragPoint.y - clickPoint.y) * 0.05);
                } else {
                    dragTransform = Mat4.translation((dragPoint.x - clickPoint.x) * 0.01, (clickPoint.y - dragPoint.y) * 0.01, 0.0);
                }
                objectCoords.transformOrigin(dragTransform);
                updateSelectionPositions();
            } else {
                Vec3 rotAxis = new Vec3((clickPoint.y - dragPoint.y) * DRAG_SCALE, (dragPoint.x - clickPoint.x) * DRAG_SCALE, 0.0);
                double angle = rotAxis.length();
                rotAxis = rotAxis.times(1.0 / angle);
                rotAxis = theCamera.getViewToWorld().timesDirection(rotAxis);
                dragTransform = Mat4.axisRotation(rotAxis, angle);
                objectCoords.transformAxes(dragTransform);
                updateSelectionPositions();
            }
        }
        render();
    }

    private void mouseDragged(MouseDraggedEvent e) {
        if (clickPoint == null) {
            return;
        }
        Graphics g = getComponent().getGraphics();
        Point dragPoint = e.getPoint();
        if (mouseButtonThree(e)) {
            if (e.isControlDown()) {
                dragTransform = Mat4.translation(0.0, 0.0, (dragPoint.y - clickPoint.y) * 0.05);
            } else {
                dragTransform = Mat4.translation((dragPoint.x - clickPoint.x) * 0.01, (clickPoint.y - dragPoint.y) * 0.01, 0.0);
            }
        } else {
            Vec3 rotAxis = new Vec3((clickPoint.y - dragPoint.y) * DRAG_SCALE, (dragPoint.x - clickPoint.x) * DRAG_SCALE, 0.0);
            double angle = rotAxis.length();
            rotAxis = rotAxis.times(1.0 / angle);
            rotAxis = theCamera.getViewToWorld().timesDirection(rotAxis);
            dragTransform = Mat4.axisRotation(rotAxis, angle);
        }
        g.drawImage(theImage, 0, 0, getComponent());
        drawHilight(g);
        drawObject(g);
        g.dispose();
    }

    private void mouseScrolled(MouseScrolledEvent e) {
        scrollTimer.restart();
        int amount = e.getWheelRotation();
        if (!e.isAltDown()) {
            amount *= 5;
        }
        if (reverseWheel) {
            scrollAmount += amount;
        } else {
            scrollAmount -= amount;
        }

        dragTransform = Mat4.translation(0.0, 0.0, scrollAmount * boundR * 0.075);
        Graphics g = getComponent().getGraphics();
        g.drawImage(theImage, 0, 0, getComponent());
        drawHilight(g);
        drawObject(g);
        g.dispose();
    }

    private void mouseStoppedScrolling() {
        scrollTimer.stop();
        scrollAmount = 0;
        objectCoords.transformOrigin(dragTransform);
        render();
    }

    public void setVertexSelection(boolean[] sel) {
        if (!showSelection) {
            return;
        }

        clearVertexSelection();
        PolyMesh mesh = (PolyMesh) info.object;
        MeshVertex[] v = mesh.getVertices();
        selPos.clear();
        Mat4 m = objectCoords.fromLocal();
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                Vec3 pos = new Vec3(v[i].r);
                selPos.add(pos);
                pos = m.times(pos);
                CoordinateSystem c = new CoordinateSystem(pos, 0, 0, 0);
                ObjectInfo sphereInfo = new ObjectInfo(sphere, c, "sphere" + i);
                selection.add(sphereInfo);
                theScene.addObject(sphereInfo, null);
            }
        }
        updateSelectionPositions();
    }

    public void clearVertexSelection() {
        if (selection.isEmpty()) {
            return;
        }

        for (int i = selection.size() - 1; i >= 0; i--) {
            theScene.removeObject(spheresIndex + i, null);
        }
        selection.clear();
        selPos.clear();
    }

    private void updateSelectionPositions() {
        Mat4 m = objectCoords.fromLocal();
        CoordinateSystem c;
        ObjectInfo info;
        for (int i = 0; i < selection.size(); i++) {
            Vec3 pos = selPos.get(i);
            pos = m.times(pos);
            c = new CoordinateSystem(pos, 0, 0, 0);
            info = theScene.getObject(spheresIndex + i);
            info.coords = c;
        }
    }

    public void setShowSelection(boolean state) {
        if (!state) {
            clearVertexSelection();
            render();
            showSelection = false;
        } else {
            showSelection = true;
        }
    }
}
