/* Copyright (C) 1999-2009 by Peter Eastman
   Modifications Copyright 2016 by Petri Ihalainen
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.image.*;
import artofillusion.image.filter.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * SceneCamera is a type of Object3D. It represents a camera which the user can position
 * within a scene. It should not be confused with the Camera class.
 */
@Slf4j
public class SceneCamera extends Object3D {

    private double fov = 30.0;
    @Getter @Setter
    private double depthOfField = Camera.DEFAULT_DISTANCE_TO_SCREEN / 2.0;
    private double focalDist = Camera.DEFAULT_DISTANCE_TO_SCREEN;
    @Getter @Setter
    private double distToPlane = Camera.DEFAULT_DISTANCE_TO_SCREEN;
    @Getter @Setter
    private boolean perspective = true;
    private List<ImageFilter> filters = new ArrayList<>();
    private int extraComponents;

    private static final BoundingBox bounds;
    private static final WireframeMesh mesh;
    private static final int SEGMENTS = 8;
    private static final Property[] PROPERTIES = new Property[]{
        new Property(Translate.text("fieldOfView"), 0.0, 180.0, 30.0),
        new Property(Translate.text("depthOfField"), Double.MIN_VALUE, Double.MAX_VALUE, Camera.DEFAULT_DISTANCE_TO_SCREEN / 2.0),
        new Property(Translate.text("focalDist"), Double.MIN_VALUE, Double.MAX_VALUE, Camera.DEFAULT_DISTANCE_TO_SCREEN),
        new Property(Translate.text("Perspective"), true),};

    static {
        double[] sine, cosine;
        int i;
        int[] t;
        int[] f;
        int[] to;
        int[] from;
        int index = 0;
        Vec3[] vert;

        bounds = new BoundingBox(-0.25, 0.25, -0.15, 0.20, -0.2, 0.2);
        sine = new double[SEGMENTS];
        cosine = new double[SEGMENTS];
        for (i = 0; i < SEGMENTS; i++) {
            sine[i] = Math.sin(i * 2.0 * Math.PI / SEGMENTS);
            cosine[i] = Math.cos(i * 2.0 * Math.PI / SEGMENTS);
        }
        vert = new Vec3[24 + 2 * SEGMENTS];
        from = new int[34 + 3 * SEGMENTS];
        to = new int[34 + 3 * SEGMENTS];

        // Create the body.
        vert[0] = new Vec3(-0.25, -0.15, -0.2);
        vert[1] = new Vec3(0.25, -0.15, -0.2);
        vert[2] = new Vec3(0.25, 0.15, -0.2);
        vert[3] = new Vec3(-0.25, 0.15, -0.2);
        vert[4] = new Vec3(-0.25, -0.15, 0.0);
        vert[5] = new Vec3(0.25, -0.15, 0.0);
        vert[6] = new Vec3(0.25, 0.15, 0.0);
        vert[7] = new Vec3(-0.25, 0.15, 0.0);
        f = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3,};
        t = new int[]{1, 2, 3, 0, 5, 6, 7, 4, 4, 5, 6, 7,};
        for (i = 0; i < f.length; i++, index++) {
            from[index] = f[i];
            to[index] = t[i];
        }

        // Create the shutter.
        vert[8] = new Vec3(-0.25, 0.15, -0.15);
        vert[9] = new Vec3(-0.2, 0.15, -0.15);
        vert[10] = new Vec3(-0.2, 0.2, -0.15);
        vert[11] = new Vec3(-0.25, 0.2, -0.15);
        vert[12] = new Vec3(-0.25, 0.15, -0.1);
        vert[13] = new Vec3(-0.2, 0.15, -0.1);
        vert[14] = new Vec3(-0.2, 0.2, -0.1);
        vert[15] = new Vec3(-0.25, 0.2, -0.1);
        for (i = 0; i < f.length; i++, index++) {
            from[index] = f[i] + 8;
            to[index] = t[i] + 8;
        }

        // Create the viewfinder.
        vert[16] = new Vec3(-0.2, 0.15, 0.0);
        vert[17] = new Vec3(-0.05, 0.2, 0.0);
        vert[18] = new Vec3(0.05, 0.2, 0.0);
        vert[19] = new Vec3(0.2, 0.15, 0.0);
        vert[20] = new Vec3(0.2, 0.15, -0.2);
        vert[21] = new Vec3(0.05, 0.2, -0.2);
        vert[22] = new Vec3(-0.05, 0.2, -0.2);
        vert[23] = new Vec3(-0.2, 0.15, -0.2);
        f = new int[]{16, 17, 18, 19, 20, 21, 22, 23, 17, 18};
        t = new int[]{17, 18, 19, 20, 21, 22, 23, 16, 22, 21};
        for (i = 0; i < f.length; i++, index++) {
            from[index] = f[i];
            to[index] = t[i];
        }

        // Create the lens.
        for (i = 0; i < SEGMENTS; i++, index++) {
            vert[24 + i] = new Vec3(0.1 * cosine[i], 0.1 * sine[i], 0.0);
            vert[24 + i + SEGMENTS] = new Vec3(0.1 * cosine[i], 0.1 * sine[i], 0.2);
            from[index] = 24 + i;
            to[index] = 24 + (i + 1) % SEGMENTS;
            from[index + SEGMENTS] = 24 + i;
            to[index + SEGMENTS] = 24 + i + SEGMENTS;
            from[index + 2 * SEGMENTS] = 24 + i + SEGMENTS;
            to[index + 2 * SEGMENTS] = 24 + (i + 1) % SEGMENTS + SEGMENTS;
        }
        mesh = new WireframeMesh(vert, from, to);
    }

    public SceneCamera() {
    }

    public double getFieldOfView() {
        return fov;
    }

    public void setFieldOfView(double fieldOfView) {
        fov = fieldOfView;
    }

    public double getFocalDistance() {
        return focalDist;
    }

    public void setFocalDistance(double dist) {
        focalDist = dist;
    }

    /**
     * Get the list of ImageFilters for this camera.
     */
    public ImageFilter[] getImageFilters() {
        return filters.toArray(ImageFilter[]::new);
    }

    /**
     * Set the list of ImageFilters for this camera.
     */
    public void setImageFilters(ImageFilter[] filters) {
        this.filters = new ArrayList<>(Arrays.asList(filters));
    }

    /**
     * Get a list of additional image components, beyond those required by the camera's filters,
     * which should be included in rendered images. This is a sum of the constants defined in
     * ComplexImage.
     */
    public int getExtraRequiredComponents() {
        return extraComponents;
    }

    /**
     * Set a list of additional image components, beyond those required by the camera's filters,
     * which should be included in rendered images. This is a sum of the constants defined in
     * ComplexImage.
     */
    public void setExtraRequiredComponents(int components) {
        extraComponents = components;
    }

    /**
     * Get a list of all image components that should be included in rendered images. This includes
     * all those required by this camera's filters, as well as ones specified by
     * setExtraRequiredComponents(). This is a sum of the constants defined in ComplexImage.
     */
    public int getComponentsForFilters() {
        int components = extraComponents;
        for (var filter: filters) {
            components |= filter.getDesiredComponents();
        }
        return components;
    }

    /**
     * Apply all of this camera's filters to an image.
     *
     * @param image the image to filter
     * @param scene the Scene which was rendered to create the image
     * @param coords the position of this camera in the scene
     */
    public void applyImageFilters(ComplexImage image, Scene scene, CoordinateSystem coords) {
        filters.forEach( flt -> flt.filterImage(image, scene, this, coords));
        image.rebuildImage();
    }

    /**
     * Get the transform which maps between view coordinates and screen coordinates for this camera.
     *
     * @param width the image width in pixels
     * @param height the image height in pixels
     */
    public Mat4 getScreenTransform(int width, int height) {
        if (perspective) {
            double scale = 0.5 * height / Math.tan(getFieldOfView() * Math.PI / 360.0);
            Mat4 screenTransform = Mat4.scale(-scale, -scale, scale).times(Mat4.perspective(0.0));
            screenTransform = Mat4.translation((double) width / 2.0, (double) height / 2.0, 0.0).times(screenTransform);
            return screenTransform;
        } else {
            double scale = 0.5 * height / (Math.tan(getFieldOfView() * Math.PI / 360.0) * getFocalDistance());
            Mat4 screenTransform = Mat4.scale(-scale, -scale, scale).times(Mat4.identity());
            screenTransform = Mat4.translation((double) width / 2.0, (double) height / 2.0, 0.0).times(screenTransform);
            return screenTransform;
        }
    }

    /**
     * Compute a ray from the camera location through a point in its field of, represented in the camera's local
     * coordinate system.
     *
     * @param x the x coordinate of the point in the plane z=1 through which the ray passes
     * @param y the y coordinate of the point in the plane z=1 through which the ray passes
     * @param dof1 this is used for simulating depth of field. dof1 and dof2 are independent values uniformly distributed
     * between 0 and 1. Together, they select the point on the camera which should serve as the ray's origin.
     * @param origin on exit, this contains the ray origin
     * @param direction on exit, this contains the normalized ray direction
     */
    public void getRayFromCamera(double x, double y, double dof1, double dof2, Vec3 origin, Vec3 direction) {
        if (perspective) {
            origin.set(0.0, 0.0, 0.0);
            double scale = focalDist * 2.0 * Math.tan(getFieldOfView() * Math.PI / 360.0);
            if (dof1 != 0.0) {
                double angle = dof1 * 2.0 * Math.PI;
                double dofScale = 0.01 * scale * dof2 * focalDist / depthOfField;
                origin.x = dofScale * Math.cos(angle);
                origin.y = dofScale * Math.sin(angle);
            }
            direction.set(-x * scale - origin.x, -y * scale - origin.y, focalDist);
            direction.normalize();
        } else {
            double scale = focalDist * 2.0 * Math.tan(getFieldOfView() * Math.PI / 360.0);
            origin.set(-scale * x, -scale * y, 0);
            if (dof1 != 0.0) {
                double angle = dof1 * 2.0 * Math.PI;
                double dofScale = 0.01 * scale * dof2 * focalDist / depthOfField;
                double dx = dofScale * Math.cos(angle);
                double dy = dofScale * Math.sin(angle);
                origin.x += dx;
                origin.y += dy;
                direction.set(-dx, -dy, focalDist);
                direction.normalize();
            } else {
                direction.set(0.0, 0.0, 1.0);
            }
        }
    }

    @Override
    public SceneCamera duplicate() {
        SceneCamera sc = new SceneCamera();

        sc.distToPlane = distToPlane;
        sc.fov = fov;
        sc.depthOfField = depthOfField;
        sc.focalDist = focalDist;
        sc.perspective = perspective;
        filters.forEach(filter -> sc.filters.add(filter.duplicate()));
        return sc;
    }

    @Override
    public void copyObject(Object3D obj) {
        SceneCamera sc = (SceneCamera) obj;

        distToPlane = sc.distToPlane;
        fov = sc.fov;
        depthOfField = sc.depthOfField;
        focalDist = sc.focalDist;
        perspective = sc.perspective;
        filters.clear();
        sc.filters.forEach(filter -> filters.add(filter.duplicate()));
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    /* A SceneCamera has no size, so calls to setSize() are ignored. */
    @Override
    public void setSize(double xsize, double ysize, double zsize) {
    }

    @Override
    public boolean canSetTexture() {
        return false;
    }

    @Override
    public WireframeMesh getWireframeMesh() {
        return mesh;
    }

    /**
     * Create a Camera object representing the view through this SceneCamera.
     *
     * @param width the width of the image viewed through the Camera
     * @param height the height of the image viewed through the Camera
     * @param coords the CoordinateSystem of this SceneCamera
     * @return an appropriately configured Camera
     */
    public Camera createCamera(int width, int height, CoordinateSystem coords) {
        Camera cam = new Camera();
        cam.setCameraCoordinates(coords.duplicate());
        cam.setScreenTransform(getScreenTransform(width, height), width, height);
        return cam;
    }

    /**
     * This is a utility method which synchronously renders an image of the scene
     * from the viewpoint of this camera.
     */
    public ComplexImage renderScene(Scene theScene, int width, int height, Renderer rend, CoordinateSystem cameraPos) {
        Camera cam = createCamera(width, height, cameraPos);
        final ComplexImage[] theImage = new ComplexImage[1];
        RenderListener rl = new RenderListener() {
            @Override
            public synchronized void imageComplete(ComplexImage image) {
                theImage[0] = image;
                notify();
            }

            @Override
            public void renderingCanceled() {
                notify();
            }
        };
        rend.renderScene(theScene, cam, rl, this);
        synchronized (rl) {
            try {
                rl.wait();
            } catch (InterruptedException ex) {
                rend.cancelRendering(theScene);
                return null;
            }
        }
        applyImageFilters(theImage[0], theScene, cameraPos);
        return theImage[0];
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void edit(final EditingWindow parent, final ObjectInfo info, Runnable cb) {
        final ValueSlider fovSlider = new ValueSlider(0.0, 180.0, 90, fov);
        final ValueField dofField = new ValueField(depthOfField, ValueField.POSITIVE);
        final ValueField fdField = new ValueField(focalDist, ValueField.POSITIVE);
        BCheckBox perspectiveBox = new BCheckBox(Translate.text("Perspective"), perspective);
        BButton filtersButton = Translate.button("filters", new Object() {
            void processEvent() {
                SceneCamera temp = SceneCamera.this.duplicate();
                temp.fov = fovSlider.getValue();
                temp.depthOfField = dofField.getValue();
                temp.focalDist = fdField.getValue();
                new CameraFilterDialog(UIUtilities.findWindow(fovSlider), parent.getScene(), temp, info.getCoords());
                filters = temp.filters;
            }
        }, "processEvent");
        ComponentsDialog dlg = new ComponentsDialog(parent.getFrame(), Translate.text("editCameraTitle"),
                new Widget[]{fovSlider, dofField, fdField, perspectiveBox, filtersButton},
                new String[]{Translate.text("fieldOfView"), Translate.text("depthOfField"), Translate.text("focalDist"), null, null});
        if (dlg.clickedOk()) {
            fov = fovSlider.getValue();
            depthOfField = dofField.getValue();
            focalDist = fdField.getValue();
            perspective = perspectiveBox.getState();
        }

        // If there are any Pose tracks for this object, they need to have their subtracks updated
        // to reflect the current list of filters.

        for (ObjectInfo oi: parent.getScene().getObjects()) {
            if (oi.getObject() == this) {
                // This ObjectInfo corresponds to this SceneCamera.  Check each of its tracks.


                for (Track<?> track: oi.getTracks()) {
                    if (track instanceof PoseTrack) {
                        // This is a Pose track, so update its subtracks.

                        PoseTrack pose = (PoseTrack) track;
                        Track[] old = pose.getSubtracks();
                        Track[] newtracks = new Track[filters.size()];
                        for (int k = 0; k < filters.size(); k++) {
                            Track existing = null;
                            for (int m = 0; m < old.length && existing == null; m++) {
                                if (old[m] instanceof FilterParameterTrack && ((FilterParameterTrack) old[m]).getFilter() == filters.get(k)) {
                                    existing = old[m];
                                }
                            }
                            if (existing == null) {
                                existing = new FilterParameterTrack(pose, filters.get(k));
                            }
                            newtracks[k] = existing;
                        }
                        pose.setSubtracks(newtracks);
                    }
                }
            }
        }
        if (parent instanceof LayoutWindow) {
            ((LayoutWindow) parent).getScore().rebuildList();
        }
        cb.run();
    }

    /* The following two methods are used for reading and writing files.  The first is a
     constructor which reads the necessary data from an input stream.  The other writes
     the object's representation to an output stream. */
    public SceneCamera(DataInputStream in, Scene theScene) throws IOException {
        super(in, theScene);

        short version = in.readShort();
        if(version < 1) {
            throw new InvalidObjectException("SceneCamera version 0 is no more supported since release 13.05.2025");
        }
        if (version > 4) {
            throw new InvalidObjectException("Unexpected SceneCamera version " + version);
        }

        if (version >= 3) {
            distToPlane = in.readDouble();
        }
        fov = in.readDouble();
        depthOfField = in.readDouble();
        focalDist = in.readDouble();
        if (version < 2) {
            perspective = true;
        } else {
            perspective = in.readBoolean();
        }

        var filtersCount = in.readInt();

        if(version <= 3) {
            SceneCamera.loadFiltersV3(in, theScene, this, filtersCount);
        } else {
            SceneCamera.loadFiltersV4(in, theScene, this, filtersCount);
        }

    }
    private static void loadFiltersV4(DataInputStream in, Scene scene, SceneCamera owner, int count) throws IOException {
        var bus = org.greenrobot.eventbus.EventBus.getDefault();

        var filterClassName = "";
        var filterDataSize = 0;
        byte[] filterData;
        ImageFilter filter;

        for (int i = 0; i < count; i++) {
            // At first read binary data from input. If IOException is thrown we cannot recover data and aborting
            try {
                filterClassName = in.readUTF();
                filterDataSize = in.readInt();
                filterData = new byte[filterDataSize];
                in.read(filterData);

            } catch(IOException ie) {
                throw ie;
            }
            //Now try to discover ImageFilter plugin. On exception, we cannot recover plugin, but can bypass it
            try {
                Class<?> filterClass = ArtOfIllusion.getClass(filterClassName);
                if(null == filterClass) {
                    bus.post(new BypassEvent(scene, "Scene camera filter: " + filterClassName + " was not found"));
                    continue;
                }
                filter = (ImageFilter) filterClass.getDeclaredConstructor().newInstance();
            } catch(ReflectiveOperationException cne) {
                bus.post(new BypassEvent(scene, "Filter class: " + filterClassName + " was not found or cannot instantiate", cne));
                continue;
            }
            //On exception, we cannot recover plugin, but can bypass it
            try {
                filter.initFromStream(new DataInputStream(new ByteArrayInputStream(filterData)), scene);
            } catch(IOException ie) {
                bus.post(new BypassEvent(scene, "Scene camera filter: " + filterClassName + " initialization error", ie));
                continue;
            }

            owner.filters.add(filter);

        }

    }

    private static void loadFiltersV3(DataInputStream in, Scene theScene, SceneCamera owner, int count) throws IOException {
        try {
            for (int i = 0; i < count; i++) {
                var filterClassName = in.readUTF();
                log.debug("Restoring: {}", filterClassName);
                Class<?> filterClass = ArtOfIllusion.getClass(filterClassName);
                if(null == filterClass) {
                    throw new IOException("Application cannot find given scene filter class: " + filterClassName);
                }
                var filter = (ImageFilter) filterClass.getDeclaredConstructor().newInstance();
                filter.initFromStream(in, theScene);
                owner.filters.add(filter);
            }
        } catch (IOException | ReflectiveOperationException | SecurityException ex) {
            log.atError().setCause(ex).log("Unable to instantiate Scene filter {}", ex.getMessage());
            throw new IOException(ex);
        }
    }


    @Override
    public void writeToFile(DataOutputStream out, Scene scene) throws IOException {
        super.writeToFile(out, scene);

        out.writeShort(4);
        out.writeDouble(distToPlane);
        out.writeDouble(fov);
        out.writeDouble(depthOfField);
        out.writeDouble(focalDist);
        out.writeBoolean(perspective);
        out.writeInt(filters.size());
        log.debug("Scene camera writes filters: {}", filters.size());
        for (var filter: filters) {
            SceneCamera.writeFilterBuffered(out, filter, scene);
        }
    }

    private static void writeFilter(DataOutputStream out, ImageFilter filter, Scene scene, boolean buffered) throws IOException {
        var fc = filter.getClass().getName();
        out.writeUTF(fc);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        filter.writeToStream(new DataOutputStream(bos), scene);
        byte[] ba = bos.toByteArray();
        var size = ba.length;
        if(buffered) out.writeInt(size);
        out.write(ba, 0, size);
    }

    private static void writeFilterDirect(DataOutputStream out, ImageFilter filter, Scene scene) throws IOException {
        writeFilter(out, filter, scene, false);
    }

    private static void writeFilterBuffered(DataOutputStream out, ImageFilter filter, Scene scene) throws IOException {
        writeFilter(out, filter, scene, true);
    }

    @Override
    public Property[] getProperties() {
        return PROPERTIES.clone();
    }

    @Override
    public Object getPropertyValue(int index) {
        switch (index) {
            case 0:
                return fov;
            case 1:
                return depthOfField;
            case 2:
                return focalDist;
            case 3:
                return perspective;
        }
        return null;
    }

    @Override
    public void setPropertyValue(int index, Object value) {
        if (index == 0) {
            fov = (Double) value;
        } else if (index == 1) {
            depthOfField = (Double) value;
        } else if (index == 2) {
            focalDist = (Double) value;
        } else if (index == 3) {
            perspective = (Boolean) value;
        }
    }

    /* Return a Keyframe which describes the current pose of this object. */
    @Override
    public Keyframe getPoseKeyframe() {
        return new CameraKeyframe(fov, depthOfField, focalDist);
    }

    /* Modify this object based on a pose keyframe. */
    @Override
    public void applyPoseKeyframe(Keyframe k) {
        CameraKeyframe key = (CameraKeyframe) k;

        fov = key.fov;
        depthOfField = key.depthOfField;
        focalDist = key.focalDist;
    }

    /**
     * This will be called whenever a new pose track is created for this object. It allows
     * the object to configure the track by setting its graphable values, subtracks, etc.
     */
    @Override
    public void configurePoseTrack(PoseTrack track) {
        track.setGraphableValues(new String[]{"Field of View", "Depth of Field", "Focal Distance"},
                new double[]{fov, depthOfField, focalDist},
                new double[][]{{0.0, 180.0}, {0.0, Double.MAX_VALUE}, {0.0, Double.MAX_VALUE}});
        FilterParameterTrack[] subtrack = new FilterParameterTrack[filters.size()];
        for (int i = 0; i < subtrack.length; i++) {
            subtrack[i] = new FilterParameterTrack(track, filters.get(i));
        }
        track.setSubtracks(subtrack);
    }

    /* Allow the user to edit a keyframe returned by getPoseKeyframe(). */
    @Override
    public void editKeyframe(EditingWindow parent, Keyframe k, ObjectInfo info) {
        CameraKeyframe key = (CameraKeyframe) k;
        ValueSlider fovSlider = new ValueSlider(0.0, 180.0, 90, key.fov);
        ValueField dofField = new ValueField(key.depthOfField, ValueField.POSITIVE);
        ValueField fdField = new ValueField(key.focalDist, ValueField.POSITIVE);
        ComponentsDialog dlg = new ComponentsDialog(parent.getFrame(), Translate.text("editCameraTitle"),
                new Widget[]{fovSlider, dofField, fdField},
                new String[]{Translate.text("fieldOfView"), Translate.text("depthOfField"), Translate.text("focalDist")});
        if (!dlg.clickedOk()) {
            return;
        }
        key.fov = fovSlider.getValue();
        key.depthOfField = dofField.getValue();
        key.focalDist = fdField.getValue();
    }

    /* Inner class representing a pose for a Scene Camera */
    public static class CameraKeyframe implements Keyframe {

        public double fov, depthOfField, focalDist;

        public CameraKeyframe(double fov, double depthOfField, double focalDist) {
            this.fov = fov;
            this.depthOfField = depthOfField;
            this.focalDist = focalDist;
        }

        /* Create a duplicate of this keyframe. */
        @Override
        public CameraKeyframe duplicate() {
            return new CameraKeyframe(fov, depthOfField, focalDist);
        }

        /* Create a duplicate of this keyframe for a (possibly different) object. */
        @Override
        public CameraKeyframe duplicate(Object owner) {
            return new CameraKeyframe(fov, depthOfField, focalDist);
        }

        /* Get the list of graphable values for this keyframe. */
        @Override
        public double[] getGraphValues() {
            return new double[]{fov, depthOfField, focalDist};
        }

        /* Set the list of graphable values for this keyframe. */
        @Override
        public void setGraphValues(double[] values) {
            fov = values[0];
            depthOfField = values[1];
            focalDist = values[2];
        }

        /* These methods return a new Keyframe which is a weighted average of this one and one,
       two, or three others. */
        @Override
        public CameraKeyframe blend(Keyframe o2, double weight1, double weight2) {
            CameraKeyframe k2 = (CameraKeyframe) o2;

            return new CameraKeyframe(weight1 * fov + weight2 * k2.fov, weight1 * depthOfField + weight2 * k2.depthOfField,
                    weight1 * focalDist + weight2 * k2.focalDist);
        }

        @Override
        public CameraKeyframe blend(Keyframe o2, Keyframe o3, double weight1, double weight2, double weight3) {
            CameraKeyframe k2 = (CameraKeyframe) o2, k3 = (CameraKeyframe) o3;

            return new CameraKeyframe(weight1 * fov + weight2 * k2.fov + weight3 * k3.fov,
                    weight1 * depthOfField + weight2 * k2.depthOfField + weight3 * k3.depthOfField,
                    weight1 * focalDist + weight2 * k2.focalDist + weight3 * k3.focalDist);
        }

        @Override
        public CameraKeyframe blend(Keyframe o2, Keyframe o3, Keyframe o4, double weight1, double weight2, double weight3, double weight4) {
            CameraKeyframe k2 = (CameraKeyframe) o2, k3 = (CameraKeyframe) o3, k4 = (CameraKeyframe) o4;

            return new CameraKeyframe(weight1 * fov + weight2 * k2.fov + weight3 * k3.fov + weight4 * k4.fov,
                    weight1 * depthOfField + weight2 * k2.depthOfField + weight3 * k3.depthOfField + weight4 * k4.depthOfField,
                    weight1 * focalDist + weight2 * k2.focalDist + weight3 * k3.focalDist + weight4 * k4.focalDist);
        }

        /* Determine whether this keyframe is identical to another one. */
        @Override
        public boolean equals(Keyframe k) {
            if (!(k instanceof CameraKeyframe)) {
                return false;
            }
            CameraKeyframe key = (CameraKeyframe) k;
            return (key.fov == fov && key.depthOfField == depthOfField && key.focalDist == focalDist);
        }

        /* Write out a representation of this keyframe to a stream. */
        @Override
        public void writeToStream(DataOutputStream out) throws IOException {
            out.writeDouble(fov);
            out.writeDouble(depthOfField);
            out.writeDouble(focalDist);
        }

        /* Reconstructs the keyframe from its serialized representation. */
        public CameraKeyframe(DataInputStream in, Object parent) throws IOException {
            this(in.readDouble(), in.readDouble(), in.readDouble());
        }
    }
}
