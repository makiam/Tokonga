/* Copyright (C) 2006-2007 by Francois Guillet
 *  Changes copyright (C) 2023-2025 by Maksim Khramov

 This program is free software; you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation; either version 2 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY 
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.MeshEditorWindow;
import artofillusion.MeshViewer;
import artofillusion.UndoRecord;
import artofillusion.ViewerCanvas;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.ui.ComponentsDialog;
import artofillusion.ui.EditingTool;
import artofillusion.ui.EditingWindow;
import artofillusion.ui.MeshEditController;
import artofillusion.ui.ThemeManager;
import artofillusion.ui.Translate;
import buoy.widget.BComboBox;
import buoy.widget.Widget;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
 * AdvancedExtrudeTool is the tool used to extrude selection. In addition, it
 * can scale/rotate the selection (e.g. extruded faces.)
 */
@EditingTool.ButtonImage("polymesh:bevel")
@EditingTool.Tooltip("polymesh:advancedBevelExtrudeTool.tipText")
@EditingTool.ActivatedToolText("polymesh:advancedBevelExtrudeTool.helpText")
public class AdvancedBevelExtrudeTool extends AdvancedEditingTool {

    private Vec3[] baseVertPos;
    private final Map<ViewerCanvas, Manipulator> mouseDragManipHashMap;
    private boolean[] selected;
    private boolean separateFaces;
    private PolyMesh origMesh;
    private final short EXTRUDE_FACES = 1;
    private int mode;
    private static ImageIcon bevelExtrudeFacesIcon, bevelExtrudeEdgesIcon, bevelExtrudeVerticesIcon;

    public AdvancedBevelExtrudeTool(EditingWindow fr, MeshEditController controller) {
        super(fr, controller);
        if (AdvancedBevelExtrudeTool.bevelExtrudeFacesIcon == null) {
            AdvancedBevelExtrudeTool.bevelExtrudeFacesIcon = ThemeManager.getIcon("polymesh:bevelextrudefaces");
            AdvancedBevelExtrudeTool.bevelExtrudeEdgesIcon = ThemeManager.getIcon("polymesh:bevelextrudeedges");
            AdvancedBevelExtrudeTool.bevelExtrudeVerticesIcon = ThemeManager.getIcon("polymesh:bevelextrudevertices");
        }
        mouseDragManipHashMap = new HashMap<>();
    }

    @Override
    public void activateManipulators(ViewerCanvas view) {
        if (!mouseDragManipHashMap.containsKey(view)) {
            PolyMeshValueWidget valueWidget = null;
            Manipulator mouseDragManip = new MouseDragManipulator(this, view, AdvancedBevelExtrudeTool.bevelExtrudeFacesIcon);
            mouseDragManip.addEventLink(Manipulator.ManipulatorPrepareChangingEvent.class, this, "doManipulatorPrepareShapingMesh");
            mouseDragManip.addEventLink(Manipulator.ManipulatorCompletedEvent.class, this, "doManipulatorShapedMesh");
            mouseDragManip.addEventLink(Manipulator.ManipulatorAbortChangingEvent.class, this, "doAbortChangingMesh");
            mouseDragManip.addEventLink(MouseDragManipulator.ManipulatorMouseDragEvent.class, this, "doManipulatorMouseDragEvent");
            mouseDragManip.setActive(true);
            ((PolyMeshViewer) view).setManipulator(mouseDragManip);
            mouseDragManipHashMap.put(view, mouseDragManip);
            selectionModeChanged(controller.getSelectionMode());
        } else {
            ((PolyMeshViewer) view).addManipulator(mouseDragManipHashMap.get(view));
        }
    }

    @Override
    public void activate() {
        super.activate();
        selectionModeChanged(controller.getSelectionMode());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        mouseDragManipHashMap.forEach((ViewerCanvas view, Manipulator manipulator) -> {
            ((PolyMeshViewer) view).removeManipulator(manipulator);
        });
    }

    private void doManipulatorPrepareShapingMesh(Manipulator.ManipulatorEvent e) {
        PolyMesh mesh = (PolyMesh) controller.getObject().getGeometry();
        baseVertPos = mesh.getVertexPositions();
        origMesh = mesh.duplicate();
        selected = controller.getSelection();
        int selectMode = controller.getSelectionMode();
        if (selectMode == PolyMeshEditorWindow.FACE_MODE) {
            short EXTRUDE_FACE_GROUPS = 2;
            mode = (separateFaces ? EXTRUDE_FACE_GROUPS : EXTRUDE_FACES);
        } else {
            short NO_EXTRUDE = 0;
            mode = NO_EXTRUDE;
        }
    }

    private void doAbortChangingMesh() {
        if (origMesh != null) {
            PolyMesh mesh = (PolyMesh) controller.getObject().getGeometry();
            mesh.copyObject(origMesh);
            controller.objectChanged();
        }
        origMesh = null;
        baseVertPos = null;
        theWindow.setHelpText(Translate.text("polymesh:advancedBevelExtrudeTool.helpText"));
        controller.objectChanged();
        theWindow.updateImage();
    }

    private void doManipulatorShapedMesh(Manipulator.ManipulatorEvent e) {
        PolyMesh mesh = (PolyMesh) controller.getObject().getGeometry();
        UndoRecord undo = new UndoRecord(theWindow, false, UndoRecord.COPY_OBJECT, new Object[]{mesh, origMesh});
        theWindow.setUndoRecord(undo);
        baseVertPos = null;
        origMesh = null;
        theWindow.setHelpText(Translate.text("polymesh:advancedBevelExtrudeTool.helpText"));
        theWindow.updateImage();
    }

    private void doManipulatorMouseDragEvent(MouseDragManipulator.ManipulatorMouseDragEvent e) {
        MeshViewer mv = (MeshViewer) e.getView();
        PolyMesh mesh = (PolyMesh) controller.getObject().getGeometry();
        Vec2 drag = e.getDrag();
        Vec3 camZ = mv.getCamera().getCameraCoordinates().getZDirection();
        int selectMode = controller.getSelectionMode();
        boolean shiftMod = e.isShiftDown() && e.isCtrlDown();
        boolean ctrlMod = (!e.isShiftDown()) && e.isCtrlDown();

        if (selectMode == MeshEditorWindow.FACE_MODE) {
            if (e.isShiftDown() && !e.isCtrlDown()) {
                if (Math.abs(drag.x) > Math.abs(drag.y)) {
                    drag.y = 0.0;
                } else {
                    drag.x = 0.0;
                }
            }
        } else {
            if (e.isShiftDown() && !e.isCtrlDown()) {
                drag.y = 0.0;
            }
            if (drag.x < 0.0) {
                drag.x = 0.0;
            }
        }

        if (selectMode == PolyMeshEditorWindow.POINT_MODE) {
            mesh.copyObject(origMesh);
            boolean[] sel = mesh.bevelVertices(selected, drag.y);
            theWindow.setHelpText(Translate.text("polymesh:advancedBevelExtrudeTool.pointEdgeDragText", drag.y));
            for (int i = 0; i < selected.length; ++i) {
                sel[i] = selected[i];
            }
            controller.objectChanged();
            controller.setSelection(sel);
        } else if (selectMode == PolyMeshEditorWindow.EDGE_MODE) {
            mesh.copyObject(origMesh);
            boolean[] sel = mesh.bevelEdges(selected, drag.y);
            theWindow.setHelpText(Translate.text("polymesh:advancedBevelExtrudeTool.pointEdgeDragText", drag.y));
            for (int i = 0; i < selected.length; ++i) {
                sel[i] = selected[i];
            }
            controller.objectChanged();
            controller.setSelection(sel);
        } else {
            mesh.copyObject(origMesh);
            if (mode == EXTRUDE_FACES) {
                mesh.extrudeRegion(selected, drag.y, (Vec3) null, Math.abs(1.0 - drag.x), camZ, ctrlMod, shiftMod);
            } else {
                mesh.extrudeFaces(selected, drag.y, (Vec3) null, Math.abs(1.0 - drag.x), camZ, ctrlMod, shiftMod);
            }
            boolean[] sel = new boolean[mesh.getFaces().length];
            for (int i = 0; i < selected.length; ++i) {
                sel[i] = selected[i];
            }
            theWindow.setHelpText(Translate.text("polymesh:advancedBevelExtrudeTool.faceDragText", 1.0 - drag.x, drag.y));
            controller.objectChanged();
            controller.setSelection(sel);
        }
        controller.objectChanged();
        theWindow.updateImage();
    }

    @Override
    public void selectionModeChanged(int selectionMode) {
        ImageIcon image = null;
        switch (selectionMode) {
            case MeshEditorWindow.POINT_MODE:
                image = AdvancedBevelExtrudeTool.bevelExtrudeVerticesIcon;
                break;
            case MeshEditorWindow.EDGE_MODE:
                image = AdvancedBevelExtrudeTool.bevelExtrudeEdgesIcon;
                break;
            case MeshEditorWindow.FACE_MODE:
                image = AdvancedBevelExtrudeTool.bevelExtrudeFacesIcon;
                break;
        }
        final ImageIcon tmpImage = image;
        mouseDragManipHashMap.forEach((ViewerCanvas view, Manipulator manipulator) -> {
            ((MouseDragManipulator) manipulator).setImage(tmpImage);
        });
    }

    @Override
    public void iconDoubleClicked() {
        BComboBox c = new BComboBox(new String[]{
            Translate.text("selectionAsWhole"),
            Translate.text("individualFaces")
        });
        c.setSelectedIndex(separateFaces ? 1 : 0);
        ComponentsDialog dlg = new ComponentsDialog(theFrame, Translate.text("applyExtrudeTo"),
                new Widget[]{c}, new String[]{null});
        if (dlg.clickedOk()) {
            separateFaces = (c.getSelectedIndex() == 1);
        }
    }
}
