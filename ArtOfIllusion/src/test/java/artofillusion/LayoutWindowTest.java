/* Copyright (C) 2017-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.ConstraintTrack;
import artofillusion.animation.IKTrack;
import artofillusion.animation.PoseTrack;
import artofillusion.animation.PositionTrack;
import artofillusion.animation.ProceduralPositionTrack;
import artofillusion.animation.ProceduralRotationTrack;
import artofillusion.animation.RotationTrack;
import artofillusion.animation.TextureTrack;
import artofillusion.animation.Track;
import artofillusion.animation.VisibilityTrack;
import artofillusion.animation.distortion.BendTrack;
import artofillusion.animation.distortion.CustomDistortionTrack;
import artofillusion.animation.distortion.ScaleTrack;
import artofillusion.animation.distortion.ShatterTrack;
import artofillusion.animation.distortion.SkeletonShapeTrack;
import artofillusion.animation.distortion.TwistTrack;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.Curve;
import artofillusion.object.ObjectInfo;
import artofillusion.object.Sphere;
import artofillusion.test.util.SetupLocale;
import artofillusion.test.util.UITestWatcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Locale;

import lombok.extern.java.Log;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 * @author MaksK
 */
@Log
@DisplayName("Layout Window Test")
@ExtendWith({UITestWatcher.class, SetupLocale.class})
class LayoutWindowTest {

    private JMenuBarOperator appMainMenu;

    private JFrameOperator appFrame;

    private Scene scene;

    private LayoutWindow layout;

    private static final Bundle bundle = new Bundle();

    @BeforeAll
    public static void setupClass() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, URISyntaxException, IOException {
        new Thread(() -> new JDialogOperator("Art Of Illusion").close()).start();
        Locale.setDefault(Locale.ENGLISH);
        new ClassReference("artofillusion.ArtOfIllusion").startApplication();
        bundle.load(ArtOfIllusion.class.getClassLoader().getResourceAsStream("artofillusion.properties"));
        JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
    }

    @BeforeEach
    public void setUp(TestInfo info) {
        appFrame = new JFrameOperator("Untitled");
        appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.closeSubmenus();
        layout = (LayoutWindow) ArtOfIllusion.getWindows()[0];
        layout.updateImage();
        layout.updateMenus();
        scene = layout.getScene();
        System.out.print("Executing Test Name: " + info.getDisplayName() + "...");
    }

    @AfterEach
    public void done() {
        int scc = scene.getObjects().size();
        for (int i = 2; i < scc; i++) {
            layout.removeObject(2, null);
        }
        layout.updateImage();
        layout.updateMenus();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Preferences Command")
    void testInvokePreferencesCommand() {
        appMainMenu.pushMenuNoBlock("Edit|Preferences...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, bundle.getResource("prefsTitle"));
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @DisplayName("Test Invoke Images Command")
    void testInvokeImagesCommand() {
        appMainMenu.pushMenuNoBlock("Scene|Images...");
        JDialogOperator dialog = new JDialogOperator("Images");
        JButtonOperator ok = new JButtonOperator(dialog, "OK");
        ok.clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Render Scene Command")
    void testInvokeRenderSceneCommand() {
        appMainMenu.pushMenuNoBlock("Scene|Render Scene...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Rendering Options");
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Preview Animation Command")
    void testInvokePreviewAnimationCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Preview Animation");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Render Wireframe Preview");
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    // <editor-fold defaultstate="collapsed" desc="Test bulk keyframe commands">
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Move Key Frames Command")
    void testInvokeMoveKeyFramesCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Bulk Edit Keyframes|Move...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Move Keyframes");
        JButtonOperator cancel = new JButtonOperator(dialog, "Cancel");
        cancel.clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Copy Key Frames Command")
    void testInvokeCopyKeyFramesCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Bulk Edit Keyframes|Copy...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Copy Keyframes");
        JButtonOperator cancel = new JButtonOperator(dialog, "Cancel");
        cancel.clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Rescale Key Frames Command")
    void testInvokeRescaleKeyFramesCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Bulk Edit Keyframes|Rescale...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Rescale Keyframes");
        JButtonOperator cancel = new JButtonOperator(dialog, "Cancel");
        cancel.clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Delete Key Frames Command")
    void testInvokeDeleteKeyFramesCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Bulk Edit Keyframes|Delete...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Delete Keyframes");
        JButtonOperator cancel = new JButtonOperator(dialog, "Cancel");
        cancel.clickMouse();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Loop Key Frames Command")
    void testInvokeLoopKeyFramesCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Bulk Edit Keyframes|Loop...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Loop Keyframes");
        JButtonOperator cancel = new JButtonOperator(dialog, "Cancel");
        cancel.clickMouse();
    }

    // </editor-fold>
    @Test
    @DisplayName("Test Invoke Frame Forward Command")
    void testInvokeFrameForwardCommand() {
        double timeStep = 1.0 / scene.getFramesPerSecond();
        double sceneTime = scene.getTime();
        appMainMenu.pushMenu("Animation|Forward One Frame");
        assertEquals(scene.getTime(), sceneTime + timeStep, timeStep * 0.01);
    }

    @Test
    @DisplayName("Test Invoke Frame Backward Command")
    void testInvokeFrameBackwardCommand() {
        double timeStep = 1.0 / scene.getFramesPerSecond();
        double sceneTime = scene.getTime();
        appMainMenu.pushMenu("Animation|Back One Frame");
        assertEquals(scene.getTime(), sceneTime - timeStep, timeStep * 0.01);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Jump To Time Command")
    void testInvokeJumpToTimeCommand() {
        appMainMenu.pushMenuNoBlock("Animation|Jump To Time...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Jump To Time");
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    // <editor-fold defaultstate="collapsed" desc="Test Add Track commands">
    @Test
    @DisplayName("Test Add Track Position XYZ One Command")
    void testAddTrackPositionXYZOneCommand() {
        executeTrackMenu("Position|XYZ (One Track)", PositionTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Track Position XYZ Three Command")
    void testAddTrackPositionXYZThreeCommand() {
        executeTrackMenu("Position|XYZ (Three Tracks)", PositionTrack.class, 5);
    }

    @Test
    @DisplayName("Test Add Track Position XYZ Procedural Command")
    void testAddTrackPositionXYZProceduralCommand() {
        executeTrackMenu("Position|Procedural", ProceduralPositionTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Track Rotation XYZ One Command")
    void testAddTrackRotationXYZOneCommand() {
        executeTrackMenu("Rotation|XYZ (One Track)", RotationTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Track Rotation XYZ Three Command")
    void testAddTrackRotationXYZThreeCommand() {
        executeTrackMenu("Rotation|XYZ (Three Tracks)", RotationTrack.class, 5);
    }

    @Test
    @DisplayName("Test Add Track Rotation XYZ Procedural Command")
    void testAddTrackRotationXYZProceduralCommand() {
        executeTrackMenu("Rotation|Procedural", ProceduralRotationTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Track Rotation XYZ Quaternion Command")
    void testAddTrackRotationXYZQuaternionCommand() {
        executeTrackMenu("Rotation|Quaternion", RotationTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Pose Track Command")
    void testAddPoseTrackCommand() {
        executeTrackMenu("Pose", PoseTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Bend Track Command")
    void testAddBendTrackCommand() {
        executeTrackMenu("Distortion|Bend", BendTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Custom Track Command")
    void testAddCustomTrackCommand() {
        executeTrackMenu("Distortion|Custom", CustomDistortionTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Scale Track Command")
    void testAddScaleTrackCommand() {
        executeTrackMenu("Distortion|Scale", ScaleTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Shatter Track Command")
    void testAddShatterTrackCommand() {
        executeTrackMenu("Distortion|Shatter", ShatterTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Twist Track Command")
    void testAddTwistTrackCommand() {
        executeTrackMenu("Distortion|Twist", TwistTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add IK Track Command")
    void testAddIKTrackCommand() {
        executeTrackMenu("Distortion|Inverse Kinematics", IKTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Skeleton Shape Track Command")
    void testAddSkeletonShapeTrackCommand() {
        executeTrackMenu("Distortion|Skeleton Shape", SkeletonShapeTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Constraint Track Command")
    void testAddConstraintTrackCommand() {
        executeTrackMenu("Constraint", ConstraintTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Visibility Track Command")
    void testAddVisibilityTrackCommand() {
        executeTrackMenu("Visibility", VisibilityTrack.class, 3);
    }

    @Test
    @DisplayName("Test Add Texture Parameter Track Command")
    void testAddTextureParameterTrackCommand() {
        executeTrackMenu("Texture Parameter", TextureTrack.class, 3);
    }

    private void executeTrackMenu(String path, Class clazz, int count) {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Animation|" + bundle.getResource("menu.addTrack"));
        assertFalse(oto.isEnabled());
        ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Test-" + System.currentTimeMillis());
        layout.addObject(test, null);
        layout.setSelection(2);
        appMainMenu.pushMenu("Animation|Add Track To Selected Objects|" + path);
        test = layout.getScene().getObject(2);
        assertEquals(count, test.getTracks().length);
        Track[] tracks = test.getTracks();
        assertTrue(clazz.isInstance(tracks[0]));
    }

    // </editor-fold>
    @Test
    @DisplayName("Invoke Path From Curve Command")
    void invokePathFromCurveCommand() {
        ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), "Test-" + System.currentTimeMillis());
        Vec3[] points = new Vec3[]{new Vec3(-1.8, 1.2, 0), new Vec3(1.8, -1.2, 0), new Vec3(1.8, -1.2, 0)};
        Curve curve = new Curve(points, new float[]{1.0f, 1.0f, 1.0f}, 3, false);
        ObjectInfo path = new ObjectInfo(curve, new CoordinateSystem(), "Curve-" + System.currentTimeMillis());
        layout.addObject(test, null);
        layout.addObject(path, null);
        layout.setSelection(new int[]{2, 3});
        appMainMenu.pushMenuNoBlock("Animation|Set Path From Curve...");
        JDialogOperator dialog = new JDialogOperator(appFrame, "Set Path From Curve");
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Disabled("No dialog for this command")
    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Invoke Create Scripted Object Command")
    void invokeCreateScriptedObjectCommand() {
        appMainMenu.pushMenuNoBlock("Object|Create primitive|Create Scripted Object...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "New Scripted Object");
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @DisplayName("Invoke Execute Script Window Command")
    void invokeExecuteScriptWindowCommand() {
        appMainMenu.pushMenuNoBlock("Tools|Edit Tool Script...|Edit Script...");
        JFrameOperator scw = new JFrameOperator(1);
        scw.close();
    }

    @Test
    @DisplayName("Invoke New Script As Groovy")
    void invokeNewScriptAsGroovy() {
        appMainMenu.pushMenuNoBlock("Tools|Edit Tool Script...|New Script|Groovy");
        JFrameOperator scw = new JFrameOperator(1);
        scw.close();
    }

    @Test
    @DisplayName("Invoke New Script As Beanshell")
    void invokeNewScriptAsBeanshell() {
        appMainMenu.pushMenuNoBlock("Tools|Edit Tool Script...|New Script|BeanShell");
        JFrameOperator scw = new JFrameOperator(1);
        scw.close();
    }

    @DisplayName("Test Invoke Show Textures Dialog Command")
    @Test
    void testInvokeShowTexturesDialogCommand() {
        appMainMenu.pushMenuNoBlock("Scene|Textures And Materials...");
        JDialogOperator dialog = new JDialogOperator(appFrame, "Textures and Materials");
        dialog.close();
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Invoke Show Environment Dialog")
    void testInvokeShowEnvironmentDialog() {
        appMainMenu.pushMenuNoBlock("Scene|Environment...");
        JDialogOperator dialog = new JDialogOperator(appFrame);
        new JLabelOperator(dialog, "Environment Properties");
        JButtonOperator cancel = new JButtonOperator(dialog, bundle.getResource("button.cancel"));
        cancel.clickMouse();
    }

    @Test
    @DisplayName("Test Invoke Layoutbject Command No Selection")
    void testInvokeLayoutbjectCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Object Layout...");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Layout For Single Object Command")
    void testInvokeLayoutForSingleObjectCommand() throws InterruptedException {
        String objectName = "Test-" + System.currentTimeMillis();
        ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), objectName);
        layout.addObject(test, null);
        layout.setSelection(2);
        appMainMenu.pushMenuNoBlock("Object|Object Layout...");
        JDialogOperator dialog = new JDialogOperator(appFrame, MessageFormat.format(bundle.getResource("objectLayoutTitle"), objectName));
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @DisplayName("Test Invoke Transform Object Command No Selection")
    void testInvokeTransformObjectCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Transform Object...");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Transform For Single Object Command")
    void testInvokeTransformForSingleObjectCommand() {
        String objectName = "Test-" + System.currentTimeMillis();
        ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), objectName);
        layout.addObject(test, null);
        layout.setSelection(2);
        appMainMenu.pushMenuNoBlock("Object|Transform Object...");
        JDialogOperator dialog = new JDialogOperator(appFrame, MessageFormat.format(bundle.getResource("transformObjectTitle"), objectName));
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @DisplayName("Test Invoke Transform For Multiple Objects Command")
    void testInvokeTransformForMultipleObjectsCommand() {
        String objectName = "Test-" + System.currentTimeMillis();
        ObjectInfo test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), objectName);
        layout.addObject(test, null);
        objectName = "Test-" + System.currentTimeMillis();
        test = new ObjectInfo(new Sphere(1d, 1d, 1d), new CoordinateSystem(), objectName);
        layout.addObject(test, null);
        layout.setSelection(new int[]{2, 3});
        appMainMenu.pushMenuNoBlock("Object|Transform Object...");
        JDialogOperator dialog = new JDialogOperator(appFrame, MessageFormat.format(bundle.getResource("transformObjectTitleMultiple"), objectName));
        new JButtonOperator(dialog, bundle.getResource("button.cancel")).clickMouse();
    }

    @Test
    @DisplayName("Test Invoke Hide Selection Command No Selection")
    void testInvokeHideSelectionCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Hide Selection");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Show Selection Command No Selection")
    void testInvokeShowSelectionCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Show Selection");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Show Selection All Command No Selection")
    void testInvokeShowSelectionAllCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Show All");
        assertTrue(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Lock Selection Command No Selection")
    void testInvokeLockSelectionCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Lock Selection");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Unlock Selection Command No Selection")
    void testInvokeUnlockSelectionCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Unlock Selection");
        assertFalse(oto.isEnabled());
    }

    @Test
    @DisplayName("Test Invoke Unlock Selection All Command No Selection")
    void testInvokeUnlockSelectionAllCommandNoSelection() {
        JMenuItemOperator oto = appMainMenu.showMenuItem("Object|Unlock All");
        assertTrue(oto.isEnabled());
    }

}
