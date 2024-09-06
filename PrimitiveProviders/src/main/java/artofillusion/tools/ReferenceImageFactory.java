/* Copyright (C) 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.tools;

import artofillusion.object.Object3D;
import artofillusion.object.ReferenceImage;
import artofillusion.ui.MessageDialog;
import artofillusion.ui.Translate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Optional;

public class ReferenceImageFactory implements PrimitiveFactory {

    private String objectName = "";

    @Override
    public String getCategory() {
        return "Other";
    }

    @Override
    public String getName() {
        return Translate.text("menu.referenceImage");
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    @Override
    public Optional<Object3D> create() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(Translate.text("selectReferenceImage"));

        int ret = chooser.showOpenDialog(null);
        if (ret != JFileChooser.APPROVE_OPTION) return Optional.empty();

        File f = chooser.getSelectedFile();
        Image image = new ImageIcon(f.getAbsolutePath()).getImage();

        if (image == null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0)
        {
            MessageDialog.create().error(Translate.text("errorLoadingImage", f.getName()));
            return Optional.empty();
        }
        objectName = f.getName();
        if (objectName.lastIndexOf('.') > -1) objectName = objectName.substring(0, objectName.lastIndexOf('.'));

        return Optional.of(new ReferenceImage(image));
    }

}
