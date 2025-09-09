/*
 *  Copyright 2004-2007 Francois Guillet
 *  Changes copyright 2023 Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.polymesh;

import artofillusion.*;
import artofillusion.keystroke.KeystrokeManager;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.object.SplineMesh;
import artofillusion.object.TriangleMesh;
import artofillusion.ui.ToolPalette;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import buoy.widget.BStandardDialog;
import buoy.widget.MenuWidget;
import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the plugin class that plugs PolyMesh structure and editing features into AoI
 *
 * @author Francois Guillet
 */
@Slf4j
public class PolyMeshPlugin implements Plugin {

    @Override
    public void onApplicationStarting() {
        boolean keysImplemented = false;
        for (KeystrokeRecord key : KeystrokeManager.getRecords()) {
            if (key.getGroup().equals("Polymesh")) {
                keysImplemented = true;
                break;
            }
        }
        if (keysImplemented) {
            return;
        }

        try (InputStream in = getClass().getResourceAsStream("/PMkeystrokes.xml")) {
            KeystrokeManager.addRecordsFromXML(in);
            KeystrokeManager.saveRecords();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Unable to read configuration: {}", ex.getMessage());
        }
    }

    @Override
    public void onSceneWindowCreated(LayoutWindow view) {
        ToolPalette palette = view.getToolPalette();
        palette.addTool(8, new CreatePolyMeshTool(view));
        palette.toggleDefaultTool();

        BMenuItem menuItem = Translate.menuItem("polymesh:convertToPolyMesh", new ConvertObject(view), "doConvert");
        BMenu toolsMenu = view.getObjectMenu();
        int count = toolsMenu.getChildCount();
        MenuWidget[] mw = new MenuWidget[count];
        for (int i = count - 1; i >= 0; i--) {
            mw[i] = toolsMenu.getChild(i);
        }
        toolsMenu.removeAll();
        for (int i = 0; i <= 7; i++) {
            toolsMenu.add(mw[i]);
        }
        toolsMenu.add(menuItem);
        for (int i = 8; i < count; i++) {
            toolsMenu.add(mw[i]);
        }
        view.layoutChildren();
    }

    @AllArgsConstructor
    private class ConvertObject {

        private final LayoutWindow window;

        private void doConvert() {

            BStandardDialog dlg = new BStandardDialog(Translate.text("polymesh:triangleToPolyTitle"), Translate.text("polymesh:convertToQuads"), BStandardDialog.QUESTION);
            String[] options = new String[]{Translate.text("polymesh:findQuadsDistance"), Translate.text("polymesh:findQuadsAngular"), Translate.text("polymesh:keepTriangles")};

            //NB!!! optionDefault is not match to any option button defined above... 
            String optionDefault = Translate.text("polymesh:convertToQuads");
            var objects = window.getSelectedObjects();
            if(objects.isEmpty()) return;

            CompoundUndoableEdit convert = new CompoundUndoableEdit();

                for (var item: objects) {
                    if (item.getObject() instanceof SplineMesh) {
                        convert.add(new CreatePolymeshFromSpline(window, item));
                    } else if (item.getObject() instanceof TriangleMesh) {
                        int response = dlg.showOptionDialog(window, options, optionDefault);
                        convert.add(new CreatePolymeshFromMesh(window, item, response));
                    }
                }

            window.setUndoRecord(new UndoRecord(window, false, convert.execute()));
            window.updateImage();
        }
    }
}
