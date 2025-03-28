/* Copyright (C) 2001-2012 by Peter Eastman
   Changes copyright (C) 2016-2024 by Maksim Khramov

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
import artofillusion.view.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.*;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

/**
 * This class generates a wireframe preview of an animation.
 */
@Slf4j
public class AnimationPreviewer implements Runnable {

    private double originalTime;
    private BLabel timeLabel, frameLabel;
    private final Scene theScene;
    private ObjectInfo sceneCamera;
    private SceneViewer canvas;
    private Thread previewThread;
    private BDialog display;
    private byte[][] imageData;
    private NumberFormat format;

    static int currentCamera = 0, width = 320, height = 240, fps = 15, mode = 2;
    static double startTime = 0.0, endTime = 1.0;

    public AnimationPreviewer(LayoutWindow parent) {
        theScene = parent.getScene();

        // Find all the cameras in the scene.
        List<ObjectInfo> cameras = theScene.getCameras();

        if (cameras.isEmpty()) {
            new BStandardDialog("", Translate.text("noCameraError"), BStandardDialog.INFORMATION).showMessageDialog(parent);
            return;
        }
        if (cameras.size() <= currentCamera) {
            currentCamera = 0;
        }

        // Create the components.
        BComboBox camChoice = new BComboBox();
        for (ObjectInfo camera : cameras) {
            camChoice.add(camera.getName());
        }
        camChoice.setSelectedIndex(currentCamera);
        BComboBox modeChoice = new BComboBox(new Object[]{
            Translate.text("menu.wireframeDisplay"),
            Translate.text("menu.shadedDisplay"),
            Translate.text("menu.smoothDisplay"),
            Translate.text("menu.texturedDisplay"),
            Translate.text("menu.transparentDisplay")
        });
        modeChoice.setSelectedIndex(mode);
        ValueField widthField = new ValueField((double) width, ValueField.POSITIVE + ValueField.INTEGER);
        ValueField heightField = new ValueField((double) height, ValueField.POSITIVE + ValueField.INTEGER);
        ValueField startField = new ValueField(startTime, ValueField.NONE);
        ValueField endField = new ValueField(endTime, ValueField.NONE);
        ValueField fpsField = new ValueField(fps, ValueField.POSITIVE + ValueField.INTEGER);

        // Display a dialog with the various options.
        ComponentsDialog dlg = new ComponentsDialog(parent, Translate.text("renderPreview"),
                new Widget[]{camChoice, modeChoice, startField, endField, widthField, heightField, fpsField},
                new String[]{Translate.text("Camera"), Translate.text("menu.displayMode"), Translate.text("StartTime"),
                    Translate.text("EndTime"), Translate.text("Width"), Translate.text("Height"),
                    Translate.text("FramesPerSec")});
        if (!dlg.clickedOk()) {
            return;
        }
        currentCamera = camChoice.getSelectedIndex();
        sceneCamera = cameras.get(currentCamera);
        mode = modeChoice.getSelectedIndex();
        width = (int) widthField.getValue();
        height = (int) heightField.getValue();
        startTime = startField.getValue();
        endTime = endField.getValue();
        fps = (int) fpsField.getValue();
        originalTime = theScene.getTime();

        // Display a dialog to show the preview.
        display = new BDialog(parent, Translate.text("Preview"), true);
        format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(3);
        ColumnContainer content = new ColumnContainer();
        display.setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.HORIZONTAL, new Insets(2, 0, 2, 0), null));
        content.add(timeLabel = new BLabel());
        content.add(frameLabel = new BLabel());
        setLabels(0.0, 0);
        content.add(canvas = new SceneViewer(theScene, new RowContainer(), parent, true));
        canvas.setPreferredSize(new Dimension(AnimationPreviewer.width, AnimationPreviewer.height));
        canvas.setRenderMode(mode);
        canvas.setBoundCamera(sceneCamera);
        canvas.setPerspective(true);
        canvas.setTool(new EditingTool(parent) {
            @Override
            public boolean hilightSelection() {
                return false;
            }
        });
        content.add(Translate.button("close", event -> doClose()), new LayoutInfo());
        display.pack();
        UIUtilities.centerDialog(display, parent);
        display.setResizable(false);
        previewThread = new Thread(this);
        previewThread.setPriority(Thread.NORM_PRIORITY - 1);
        previewThread.start();
        display.setVisible(true);
    }

    /**
     * Generate and display all of the frames in a loop.
     */
    @Override
    public void run() {
        int totalFrames = (int) Math.ceil((endTime - startTime) * fps);
        if (totalFrames <= 0) {
            totalFrames = 1;
        }
        imageData = new byte[totalFrames][];
        Camera cam = canvas.getCamera();
        long lastUpdate = 0L, ms, delay = 1000 / fps;

        // In the first loop, we render all of the images.
        try {
            for (int i = 0; i < totalFrames; i++) {
                final double time = startTime + i / (double) fps;
                theScene.setTime(time);
                SceneCamera sc = (SceneCamera) sceneCamera.getObject();
                cam.setCameraCoordinates(sceneCamera.getCoords().duplicate());
                cam.setScreenTransform(sc.getScreenTransform(width, height), width, height);
                final int frame = i;
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            setLabels(time, frame);
                            SoftwareCanvasDrawer drawer = (SoftwareCanvasDrawer) canvas.getCanvasDrawer();
                            Graphics2D canvasGraphics = (Graphics2D) canvas.getComponent().getGraphics();
                            drawer.paint(new RepaintEvent(canvas, canvasGraphics));
                            canvasGraphics.dispose();
                            imageData[frame] = recordImage(drawer.getImage());
                        }
                    });
                } catch (InterruptedException | InvocationTargetException ex) {
                    return;
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                ms = System.currentTimeMillis();
                if (ms < lastUpdate + delay) {
                    try {
                        Thread.sleep(lastUpdate + delay - ms);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
                lastUpdate = System.currentTimeMillis();
            }

            // In later loops, we simply retrieve the data from the array.
            while (!Thread.currentThread().isInterrupted()) {
                for (int i = 0; i < totalFrames; i++) {
                    final double time = startTime + i / (double) fps;
                    final int frame = i;
                    final Image image = retrieveImage(imageData[i]);
                    try {
                        EventQueue.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                setLabels(time, frame);
                                Graphics2D canvasGraphics = (Graphics2D) canvas.getComponent().getGraphics();
                                canvasGraphics.drawImage(image, 0, 0, null);
                                canvasGraphics.dispose();
                            }
                        });
                    } catch (InterruptedException | InvocationTargetException ex) {
                        return;
                    }
                    ms = System.currentTimeMillis();
                    if (ms < lastUpdate + delay) {
                        try {
                            Thread.sleep(lastUpdate + delay - ms);
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                    lastUpdate = System.currentTimeMillis();
                }
            }
        } catch (IOException ex) {
            log.atError().setCause(ex).log("IO Error: {}", ex.getMessage());
        }
    }

    /**
     * Close the window.
     */
    private void doClose() {
        previewThread.interrupt();
        try {
            previewThread.join();
        } catch (InterruptedException ex) {
        }
        theScene.setTime(originalTime);
        display.dispose();
    }

    /**
     * Set the labels for the current time and frame.
     */
    private void setLabels(double time, int frame) {
        timeLabel.setText(Translate.text("Time") + ": " + format.format(time));
        frameLabel.setText(Translate.text("Frame") + ": " + (frame + 1));
    }

    /**
     * Record the bitmap for an image.
     */
    private byte[] recordImage(BufferedImage image) {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", stream);
            return stream.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Copy a bitmap into the image.
     */
    private Image retrieveImage(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }
}
