/*
 *  Copyright 2004-2007 Francois Guillet
    Changes copyright (C) 2017-2019 by Maksim Khramov

 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.polymesh;

import artofillusion.ArtOfIllusion;
import artofillusion.DefaultPluginImplementation;
import artofillusion.LayoutWindow;
import artofillusion.UndoRecord;
import artofillusion.keystroke.KeystrokeManager;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SplineMesh;
import artofillusion.object.TriangleMesh;
import artofillusion.ui.Translate;
import buoy.event.CommandEvent;
import buoy.widget.BMenuItem;
import buoy.widget.BStandardDialog;
import com.google.common.flogger.FluentLogger;
import java.io.InputStream;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.SwingUtilities;

/**
 * This is the plugin class that plugs PolyMesh structure and editing features into AoI
 *
 * @author Francois Guillet
 */
public class PolyMeshPlugin extends DefaultPluginImplementation {

    private static final com.google.common.flogger.FluentLogger logger = FluentLogger.forEnclosingClass();

    public static ResourceBundle resources;

    @Override
    protected void onSceneWindowCreated(LayoutWindow view) {

        BMenuItem convertMenuItem = Translate.menuItem("polymesh:convertToPolyMesh", this, "convertToPolymeshMenuAction");
        convertMenuItem.getComponent().putClientProperty("layout", view);

        SwingUtilities.invokeLater(() -> {
            view.getToolPalette().addTool(8, new CreatePolyMeshTool(view));
            view.getObjectMenu().add(convertMenuItem, 7);
            view.layoutChildren();
        });

    }

    @Override
    protected void onApplicationStarting() {
        resources = ResourceBundle.getBundle("polymesh", ArtOfIllusion.getPreferences().getLocale());
        boolean keysImplemented = false;
        for (KeystrokeRecord key : KeystrokeManager.getAllRecords()) {
            if (key.getName().endsWith("(PolyMesh)")) {
                keysImplemented = true;
                break;
            }
        }
        if (keysImplemented) {
            return;
        }

        try {
            try (InputStream in = getClass().getResourceAsStream("/PMkeystrokes.xml")) {
                KeystrokeManager.addRecordsFromXML(in);
            }
            KeystrokeManager.saveRecords();
        } catch (Exception ex) {
            logger.at(Level.SEVERE).withCause(ex).log("Unable to load keystroke records");
        }
    }


    @SuppressWarnings("unused")
    private void convertToPolymeshMenuAction(CommandEvent event) {
        BMenuItem mi = (BMenuItem) event.getWidget();
        LayoutWindow view = (LayoutWindow) mi.getComponent().getClientProperty("layout");

        Collection<ObjectInfo> selection = view.getSelectedObjects();

        if(selection.isEmpty()) return;

        selection.stream().filter((ObjectInfo test) -> test.getObject() instanceof SplineMesh).forEach((ObjectInfo item) -> {
            view.addObject(new ObjectInfo(new PolyMesh((SplineMesh)item.getObject()), item.getCoords().duplicate(), "Polymesh" + item.getName()), (UndoRecord)null);
        });

        BStandardDialog dlg = new BStandardDialog(Translate.text("polymesh:triangleToPolyTitle"), Translate.text("polymesh:convertToQuads"), BStandardDialog.QUESTION);
        String[] options = new String[]{Translate.text("polymesh:findQuadsDistance"), Translate.text("polymesh:findQuadsAngular"), Translate.text("polymesh:keepTriangles")};

        //NB!!! optionDefault is not match to any option button defined above...
        String optionDefault = Translate.text("polymesh:convertToQuads");

        selection.stream().filter((ObjectInfo test) -> test.getObject() instanceof TriangleMesh).forEach((ObjectInfo item) -> {
            int result = dlg.showOptionDialog(view, options, optionDefault);
            PolyMesh mesh = new PolyMesh((TriangleMesh) item.getObject(), result == 0 || result == 1, result == 1);
            view.addObject(new ObjectInfo(mesh, item.getCoords().duplicate(), "Polymesh" + item.getName()), (UndoRecord)null);
        });
    }
}
