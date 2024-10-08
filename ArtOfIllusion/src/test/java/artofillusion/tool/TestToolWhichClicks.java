/* Copyright (C) 2022-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tool;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author MaksK
 */
@ExtendWith({SetupLocale.class, SetupLookAndFeel.class, RegisterTestResources.class, SetupTheme.class})
public class TestToolWhichClicks {

    @Test
    public void testCreateCameraToolCheckWhichClickValue() {
        EditingTool tool = new CreateCameraTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateLightToolCheckWhichClickValue() {
        EditingTool tool = new CreateLightTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateCurveToolCheckWhichClickValue() {
        EditingTool tool = new CreateCurveTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testMoveViewToolCheckWhichClickValue() {
        EditingTool tool = new MoveViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateCubeToolCheckWhichClickValue() {
        EditingTool tool = new CreateCubeTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateSphereToolCheckWhichClickValue() {
        EditingTool tool = new CreateSphereTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateCylinerToolCheckWhichClickValue() {
        EditingTool tool = new CreateCylinderTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreateVertexToolCheckWhichClickValue() {
        EditingTool tool = new CreateVertexTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testCreatePolygonToolCheckWhichClickValue() {
        EditingTool tool = new CreatePolygonTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testThickenMeshToolCheckWhichClickValue() {
        EditingTool tool = new ThickenMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testSkewMeshToolCheckWhichClickValue() {
        EditingTool tool = new SkewMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testScaleMeshToolCheckWhichClickValue() {
        EditingTool tool = new ScaleMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testTaperMeshToolCheckWhichClickValue() {
        EditingTool tool = new TaperMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testRotateObjectToolCheckWhichClickValue() {
        EditingTool tool = new RotateObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    public void testBevelExtrudeToolCheckWhichClickValue() {
        EditingTool tool = new BevelExtrudeTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testMoveScaleRotateMeshToolCheckWhichClickValue() {
        EditingTool tool = new MoveScaleRotateMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS + EditingTool.HANDLE_CLICKS, click);
    }

    @Test
    public void testCreateSplineMeshToolCheckWhichClickValue() {
        EditingTool tool = new CreateSplineMeshTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testReshapeMeshToolCheckWhichClickValue() {
        EditingTool tool = new ReshapeMeshTool(null, null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS, click);
    }

    @Test
    public void testMoveUVViewToolCheckWhichClickValue() {
        EditingTool tool = new MoveUVViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testScaleObjectToolCheckWhichClickValue() {
        EditingTool tool = new ScaleObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    public void testSkeletonToolCheckWhichClickValue() {
        EditingTool tool = new SkeletonTool(null, true);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testRotateMeshToolCheckWhichClickValue() {
        EditingTool tool = new RotateMeshTool(null, null, true);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testMoveObjectToolCheckWhichClickValue() {
        EditingTool tool = new MoveObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    public void testMoveScaleRotateObjectToolCheckWhichClickValue() {
        EditingTool tool = new MoveScaleRotateObjectTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS + EditingTool.OBJECT_CLICKS, click);
    }

    @Test
    public void testRotateViewToolCheckWhichClickValue() {
        EditingTool tool = new RotateViewTool(null);
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.ALL_CLICKS, click);
    }

    @Test
    public void testGenericToolCheckWhichClickValue() {
        EditingTool tool = new GenericTool(null, "", "Generic Tool");
        int click = tool.whichClicks();
        Assertions.assertEquals(EditingTool.HANDLE_CLICKS, click);
    }
}
