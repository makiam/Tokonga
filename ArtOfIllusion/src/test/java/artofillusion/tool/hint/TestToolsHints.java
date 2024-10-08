/* Copyright (C) 2017 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tool.hint;

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
import artofillusion.texture.MoveUVViewTool;
import artofillusion.ui.EditingTool;
import artofillusion.ui.GenericTool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author MaksK
 */
@ExtendWith({SetupLocale.class, SetupLookAndFeel.class, RegisterTestResources.class})  
public class TestToolsHints {



    @Test
    public void testCreateCameraToolHintText() {
        EditingTool tool = new CreateCameraTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Camera", tip);
    }

    @Test
    public void testCreateLightToolHintText() {
        EditingTool tool = new CreateLightTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Light", tip);
    }

    @Test
    public void testCreateCurveToolHintText() {
        EditingTool tool = new CreateCurveTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Curve", tip);
    }

    @Test
    public void testMoveViewToolHintText() {
        EditingTool tool = new MoveViewTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Move Viewpoint", tip);
    }

    @Test
    public void testCreateCubeToolHintText() {
        EditingTool tool = new CreateCubeTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Cube", tip);
    }

    @Test
    public void testCreateSphereToolHintText() {
        EditingTool tool = new CreateSphereTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Sphere", tip);
    }

    @Test
    public void testCreateCylinerToolHintText() {
        EditingTool tool = new CreateCylinderTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Cylinder/Cone", tip);
    }

    @Test
    public void testCreateVertexToolHintText() {
        EditingTool tool = new CreateVertexTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Point", tip);
    }

    @Test
    public void testCreatePolygonToolHintText() {
        EditingTool tool = new CreatePolygonTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Polygon", tip);
    }

    @Test
    public void testThickenMeshToolHintText() {
        EditingTool tool = new ThickenMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Inset/Outset Selection", tip);
    }

    @Test
    public void testSkewMeshToolHintText() {
        EditingTool tool = new SkewMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Skew Selection", tip);
    }

    @Test
    public void testScaleMeshToolHintText() {
        EditingTool tool = new ScaleMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Scale Selection", tip);
    }

    @Test
    public void testTaperMeshToolHintText() {
        EditingTool tool = new TaperMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Taper Selection", tip);
    }

    @Test
    public void testRotateObjectToolHintText() {
        EditingTool tool = new RotateObjectTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Rotate Object", tip);
    }

    @Test
    public void testBevelExtrudeToolHintText() {
        EditingTool tool = new BevelExtrudeTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Bevel/Extrude", tip);
    }

    @Test
    public void testMoveScaleRotateMeshToolHintText() {
        EditingTool tool = new MoveScaleRotateMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Move/Scale/Rotate Selection", tip);
    }

    @Test
    public void testCreateSplineMeshToolHintText() {
        EditingTool tool = new CreateSplineMeshTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Create Spline Mesh", tip);
    }

    @Test
    public void testReshapeMeshToolHintText() {
        EditingTool tool = new ReshapeMeshTool(null, null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Select and Move", tip);
    }

    @Test
    public void testMoveUVViewToolHintText() {
        EditingTool tool = new MoveUVViewTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Move Viewpoint", tip);
    }

    @Test
    public void testScaleObjectToolHintText() {
        EditingTool tool = new ScaleObjectTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Resize Object", tip);
    }

    @Test
    public void testSkeletonToolHintText() {
        EditingTool tool = new SkeletonTool(null, true);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Edit Skeleton", tip);
    }

    @Test
    public void testRotateMeshToolHintText() {
        EditingTool tool = new RotateMeshTool(null, null, true);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Rotate Selection", tip);
    }

    @Test
    public void testMoveObjectToolHintText() {
        EditingTool tool = new MoveObjectTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Move Object", tip);
    }

    @Test
    public void testMoveScaleRotateObjectToolHintText() {
        EditingTool tool = new MoveScaleRotateObjectTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Move/Rotate/Resize Object", tip);
    }

    @Test
    public void testRotateViewToolHintText() {
        EditingTool tool = new RotateViewTool(null);
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Rotate Viewpoint", tip);
    }

    @Test
    public void testGenericToolHintText() {
        EditingTool tool = new GenericTool(null, "", "Generic Tool");
        String tip = tool.getToolTipText();
        Assertions.assertEquals("Generic Tool", tip);
    }
}
