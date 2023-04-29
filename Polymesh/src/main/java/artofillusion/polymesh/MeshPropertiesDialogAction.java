/* Copyright (C) 2023 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.polymesh;

import artofillusion.polymesh.ui.ColorButton;
import artofillusion.ui.PanelDialog;
import artofillusion.ui.Translate;
import buoy.event.ValueChangedEvent;
import buoy.widget.BCheckBox;
import buoy.widget.BSpinner;
import buoy.widget.FormContainer;
import buoy.widget.LayoutInfo;
import java.awt.Insets;
import java.util.stream.IntStream;

/**
 *
 * @author MaksK
 */
public class MeshPropertiesDialogAction {

    public MeshPropertiesDialogAction(PolyMeshEditorWindow owner) {
        PolyMesh mesh = (PolyMesh) owner.getObject().getObject();

        ColorButton vertColorButton = new ColorButton(mesh.getVertColor());
        ColorButton selectedVertColorButton = new ColorButton(mesh.getSelectedVertColor());
        ColorButton edgeColorButton = new ColorButton(mesh.getEdgeColor());
        ColorButton selectedEdgeColorButton = new ColorButton(mesh.getSelectedEdgeColor());
        ColorButton seamColorButton = new ColorButton(mesh.getSeamColor());
        ColorButton selectedSeamColorButton = new ColorButton(mesh.getSelectedSeamColor());
        ColorButton meshColorButton = new ColorButton(mesh.getMeshColor());
        ColorButton selectedFaceColorButton = new ColorButton(mesh.getSelectedFaceColor());
        BSpinner handleSpinner = new BSpinner(mesh.getHandleSize(), 2, 100, 1);
        BCheckBox useCustomColors = new BCheckBox(Translate.text("polymesh:useCustomColors"), mesh.useCustomColors());

        final FormContainer propertiesPanel = new FormContainer(2, 12);
        propertiesPanel.setColumnWeight(1, 1.0);
        LayoutInfo labelLayout = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 0, 2, 5), null);
        LayoutInfo widgetLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null);
        LayoutInfo centerLayout = new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(2, 0, 2, 0), null);
        propertiesPanel.add(useCustomColors, 0, 0, 2, 1, centerLayout);
        propertiesPanel.add(Translate.label("polymesh:vertColor"), 0, 1, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:selectedVertColor"), 0, 2, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:edgeColor"), 0, 3, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:selectedEdgeColor"), 0, 4, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:seamColor"), 0, 5, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:selectedSeamColor"), 0, 6, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:meshColor"), 0, 7, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:selectedFaceColor"), 0, 8, labelLayout);
        propertiesPanel.add(Translate.label("polymesh:handleSize"), 0, 9, labelLayout);
        propertiesPanel.add(vertColorButton, 1, 1, labelLayout);
        propertiesPanel.add(selectedVertColorButton, 1, 2, widgetLayout);
        propertiesPanel.add(edgeColorButton, 1, 3, widgetLayout);
        propertiesPanel.add(selectedEdgeColorButton, 1, 4, widgetLayout);
        propertiesPanel.add(seamColorButton, 1, 5, widgetLayout);
        propertiesPanel.add(selectedSeamColorButton, 1, 6, widgetLayout);
        propertiesPanel.add(meshColorButton, 1, 7, widgetLayout);
        propertiesPanel.add(selectedFaceColorButton, 1, 8, widgetLayout);
        propertiesPanel.add(handleSpinner, 1, 9, widgetLayout);

        IntStream.range(1, propertiesPanel.getChildCount()).forEach(index -> {
            propertiesPanel.getChild(index).setEnabled(useCustomColors.getState());
        });

        useCustomColors.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent(ValueChangedEvent event)
            {
                final Boolean state = ((BCheckBox)event.getWidget()).getState();
                IntStream.range(1, propertiesPanel.getChildCount()).forEach(index -> {
                    propertiesPanel.getChild(index).setEnabled(state);
                });
            }
          });

        PanelDialog dlg = new PanelDialog(owner, Translate.text("polymesh:setMeshProperties"), propertiesPanel);
            if (dlg.clickedOk()) {
                    mesh.setUseCustomColors(useCustomColors.getState());
                    if (mesh.useCustomColors()) {
                            mesh.setVertColor(vertColorButton.getColor());
                            mesh.setSelectedVertColor(selectedVertColorButton.getColor());
                            mesh.setEdgeColor(edgeColorButton.getColor());
                            mesh.setSelectedEdgeColor(selectedEdgeColorButton.getColor());
                            mesh.setSeamColor(seamColorButton.getColor());
                            mesh.setSelectedSeamColor(selectedSeamColorButton.getColor());
                            mesh.setMeshColor(meshColorButton.getColor());
                            mesh.setSelectedFaceColor(selectedFaceColorButton.getColor());
                            mesh.setHandleSize(((Integer) handleSpinner.getValue()));
                    }
                    owner.updateImage();
            }
    }

}
