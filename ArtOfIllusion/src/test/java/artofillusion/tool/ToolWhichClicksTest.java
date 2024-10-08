/* Copyright (C) 2022-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tool;

import artofillusion.ArtOfIllusion;
import artofillusion.BevelExtrudeTool;
import artofillusion.CreateCameraTool;
import artofillusion.CreateCubeTool;
import artofillusion.CreateCurveTool;
import artofillusion.CreateCylinderTool;
import artofillusion.CreateLightTool;
import artofillusion.CreatePolygonTool;
import artofillusion.CreateSphereTool;
import artofillusion.CreateSplineMeshTool;
import artofillusion.CreateVertexTool;
import artofillusion.MoveObjectTool;
import artofillusion.MoveScaleRotateMeshTool;
import artofillusion.MoveScaleRotateObjectTool;
import artofillusion.MoveViewTool;
import artofillusion.PluginRegistry;
import artofillusion.ReshapeMeshTool;
import artofillusion.RotateMeshTool;
import artofillusion.RotateObjectTool;
import artofillusion.RotateViewTool;
import artofillusion.ScaleMeshTool;
import artofillusion.ScaleObjectTool;
import artofillusion.SkewMeshTool;
import artofillusion.TaperMeshTool;
import artofillusion.ThickenMeshTool;
import artofillusion.animation.SkeletonTool;
import artofillusion.test.util.RegisterTestResources;
import artofillusion.test.util.SetupLocale;
import artofillusion.test.util.SetupLookAndFeel;
import artofillusion.test.util.SetupTheme;
import artofillusion.texture.MoveUVViewTool;
import artofillusion.ui.EditingTool;
import artofillusion.ui.GenericTool;
import artofillusion.ui.ThemeManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author MaksK
 */
@DisplayName("Tool Which Clicks Test")
@ExtendWith({SetupLocale.class, SetupLookAndFeel.class, RegisterTestResources.class, SetupTheme.class})  
class ToolWhichClicksTest {

    @BeforeAll
    public static void setUpClass() {
        PluginRegistry.registerResource("TranslateBundle", "artofillusion", ArtOfIllusion.class.getClassLoader(), "artofillusion", null);
        PluginRegistry.registerResource("UITheme", "default", ArtOfIllusion.class.getClassLoader(), "artofillusion/Icons/defaultTheme.xml", null);
        ThemeManager.initThemes();
    }

    @Test
    @DisplayName("Test Create Camera Tool Check Which Click Value")
    void testCreateCameraToolCheckWhichClickValue() {
        EditingTool tool = new CreateCameraTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Light Tool Check Which Click Value")
    void testCreateLightToolCheckWhichClickValue() {
        EditingTool tool = new CreateLightTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Curve Tool Check Which Click Value")
    void testCreateCurveToolCheckWhichClickValue() {
        EditingTool tool = new CreateCurveTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Move View Tool Check Which Click Value")
    void testMoveViewToolCheckWhichClickValue() {
        EditingTool tool = new MoveViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Cube Tool Check Which Click Value")
    void testCreateCubeToolCheckWhichClickValue() {
        EditingTool tool = new CreateCubeTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Sphere Tool Check Which Click Value")
    void testCreateSphereToolCheckWhichClickValue() {
        EditingTool tool = new CreateSphereTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Cyliner Tool Check Which Click Value")
    void testCreateCylinerToolCheckWhichClickValue() {
        EditingTool tool = new CreateCylinderTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Vertex Tool Check Which Click Value")
    void testCreateVertexToolCheckWhichClickValue() {
        EditingTool tool = new CreateVertexTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Polygon Tool Check Which Click Value")
    void testCreatePolygonToolCheckWhichClickValue() {
        EditingTool tool = new CreatePolygonTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Thicken Mesh Tool Check Which Click Value")
    void testThickenMeshToolCheckWhichClickValue() {
        EditingTool tool = new ThickenMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Skew Mesh Tool Check Which Click Value")
    void testSkewMeshToolCheckWhichClickValue() {
        EditingTool tool = new SkewMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Scale Mesh Tool Check Which Click Value")
    void testScaleMeshToolCheckWhichClickValue() {
        EditingTool tool = new ScaleMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Taper Mesh Tool Check Which Click Value")
    void testTaperMeshToolCheckWhichClickValue() {
        EditingTool tool = new TaperMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Rotate Object Tool Check Which Click Value")
    void testRotateObjectToolCheckWhichClickValue() {
        EditingTool tool = new RotateObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    @DisplayName("Test Bevel Extrude Tool Check Which Click Value")
    void testBevelExtrudeToolCheckWhichClickValue() {
        EditingTool tool = new BevelExtrudeTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Move Scale Rotate Mesh Tool Check Which Click Value")
    void testMoveScaleRotateMeshToolCheckWhichClickValue() {
        EditingTool tool = new MoveScaleRotateMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS + EditingTool.HANDLE_CLICKS, click);
    }

    @Test
    @DisplayName("Test Create Spline Mesh Tool Check Which Click Value")
    void testCreateSplineMeshToolCheckWhichClickValue() {
        EditingTool tool = new CreateSplineMeshTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Reshape Mesh Tool Check Which Click Value")
    void testReshapeMeshToolCheckWhichClickValue() {
        EditingTool tool = new ReshapeMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS, click);
    }

    @Test
    @DisplayName("Test Move UV View Tool Check Which Click Value")
    void testMoveUVViewToolCheckWhichClickValue() {
        EditingTool tool = new MoveUVViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Scale Object Tool Check Which Click Value")
    void testScaleObjectToolCheckWhichClickValue() {
        EditingTool tool = new ScaleObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    @DisplayName("Test Skeleton Tool Check Which Click Value")
    void testSkeletonToolCheckWhichClickValue() {
        EditingTool tool = new SkeletonTool(null, true);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Rotate Mesh Tool Check Which Click Value")
    void testRotateMeshToolCheckWhichClickValue() {
        EditingTool tool = new RotateMeshTool(null, null, true);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Move Object Tool Check Which Click Value")
    void testMoveObjectToolCheckWhichClickValue() {
        EditingTool tool = new MoveObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    @DisplayName("Test Move Scale Rotate Object Tool Check Which Click Value")
    void testMoveScaleRotateObjectToolCheckWhichClickValue() {
        EditingTool tool = new MoveScaleRotateObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    @DisplayName("Test Rotate View Tool Check Which Click Value")
    void testRotateViewToolCheckWhichClickValue() {
        EditingTool tool = new RotateViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    @DisplayName("Test Generic Tool Check Which Click Value")
    void testGenericToolCheckWhichClickValue() {
        EditingTool tool = new GenericTool(null, "", "Generic Tool");
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS, click);
    }
}
