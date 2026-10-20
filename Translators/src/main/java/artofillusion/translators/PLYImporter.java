/* Copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators;

import artofillusion.ArtOfIllusion;
import artofillusion.Camera;
import artofillusion.Scene;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import artofillusion.object.DirectionalLight;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import artofillusion.ui.Translate;
import buoy.widget.BFrame;
import buoy.widget.BStandardDialog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Optional;

@Slf4j
public class PLYImporter {


    public static void importFile(@NotNull BFrame parent)  {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogTitle(Translate.text("Translators:importOBJ"));
        Optional.ofNullable(ArtOfIllusion.getCurrentDirectory()).ifPresent(dir -> jfc.setCurrentDirectory(new File(dir)));

        FileNameExtensionFilter objFilter = new FileNameExtensionFilter(Translate.text("Translators:fileFilter.ply"), "ply");
        jfc.addChoosableFileFilter(objFilter);
        jfc.setAcceptAllFileFilterUsed(true);
        jfc.setFileFilter(objFilter);
        if (jfc.showOpenDialog(parent.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        ArtOfIllusion.setCurrentDirectory(jfc.getCurrentDirectory().getAbsolutePath());
        try {
            Scene scene = importFile(jfc.getSelectedFile());
            scene.setName(jfc.getSelectedFile().getName());
            ArtOfIllusion.newWindow(scene);
        } catch (Exception ex) {
            new BStandardDialog("", new String[]{Translate.text("errorLoadingFile"), ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(parent);
        }
    }

    private static Scene importFile(File f) throws Exception {
        var scene = new Scene();
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");

        scene.addObject(info, null);
        info = new ObjectInfo(new DirectionalLight(new RGBColor(1.0f, 1.0f, 1.0f), 0.8f), coords.duplicate(), "Light 1");

        scene.addObject(info, null);
        Vec3 center = new Vec3();
        coords = new CoordinateSystem(center, Vec3.vz(), Vec3.vy());
        var mesh = PLYReader.read(f.getPath());
        info = new ObjectInfo(mesh, coords, f.getName().replaceAll("(?i)\\.ply$", ""));
        scene.addObject(info, null);
        return scene;
    }
}
