/*
 *  Copyright (C) 2002-2004 by Peter Eastman
 *  Changes copyright (C) 2023-2025 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.polymesh;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.object.ObjectInfo;
import artofillusion.polymesh.PolyMesh.Wedge;
import artofillusion.polymesh.PolyMesh.Wface;
import artofillusion.polymesh.PolyMesh.Wvertex;
import artofillusion.texture.TextureImageInfo;
import artofillusion.texture.TextureSpec;
import artofillusion.ui.*;
import buoy.event.ValueChangedEvent;
import buoy.widget.BCheckBox;
import buoy.widget.BFrame;
import buoy.widget.Widget;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;

/**
 * PMOBJExporter contains the actual routines for exporting OBJ files for
 * PolyMeshes.
 *
 * @author pims
 * @created 13 juin 2005
 */
@Slf4j
public class PMOBJExporter {

    /**
     * Display a dialog which allows the user to export a scene to an OBJ file.
     *
     * @param parent Description of the Parameter
     * @param theScene Description of the Parameter
     */
    public static void exportFile(BFrame parent, Scene theScene) {

        if (theScene.getSelection().length == 0) {
            return;
        }
        boolean valid = false;
        for (ObjectInfo info : theScene.getObjects()) {
            if (info.isSelected() && info.getGeometry() instanceof PolyMesh) {
                valid = true;
            }
        }
        if (!valid) {
            return;
        }

        // Display a dialog box with options on how to export the scene.
        final ValueField widthField = new ValueField(200.0, ValueField.INTEGER + ValueField.POSITIVE);
        final ValueField heightField = new ValueField(200.0, ValueField.INTEGER + ValueField.POSITIVE);
        final ValueSlider qualitySlider = new ValueSlider(0.0, 1.0, 100, 0.5);
        final BCheckBox mtlBox = new BCheckBox(Translate.text("Translators:writeTexToMTL"), false);
        mtlBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                widthField.setEnabled(mtlBox.getState());
                heightField.setEnabled(mtlBox.getState());
                qualitySlider.setEnabled(mtlBox.getState());
            }
        });
        mtlBox.dispatchEvent(new ValueChangedEvent(mtlBox));
        ComponentsDialog dlg;
        dlg = new ComponentsDialog(parent, Translate.text("Translators:exportToOBJ"),
                new Widget[]{mtlBox, Translate.label("Translators:imageSizeForTextures"), widthField, heightField, qualitySlider},
                new String[]{null, null, Translate.text("Width"), Translate.text("Height"), Translate.text("Translators:imageQuality")});
        if (!dlg.clickedOk()) {
            return;
        }

        // Ask the user to select the output file.
        var jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogTitle(Translate.text("Translators:exportToOBJ"));
        jfc.setSelectedFile(new File("Untitled.obj"));
        Optional.ofNullable(ArtOfIllusion.getCurrentDirectory()).ifPresent(dir -> jfc.setCurrentDirectory(new File(dir)));

        if (jfc.showSaveDialog(parent.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dir = jfc.getCurrentDirectory();
        File f = jfc.getSelectedFile();
        String name = f.getName();
        String baseName = (name.endsWith(".obj") ? name.substring(0, name.length() - 4) : name);
        ArtOfIllusion.setCurrentDirectory(dir.getAbsolutePath());

        // Create the output files.
        try {
            TextureImageExporter textureExporter = null;
            String mtlFilename = null;
            if (mtlBox.getState()) {
                textureExporter = new TextureImageExporter(dir, baseName, (int) (100 * qualitySlider.getValue()),
                        TextureImageExporter.DIFFUSE + TextureImageExporter.HILIGHT + TextureImageExporter.EMISSIVE,
                        (int) widthField.getValue(), (int) heightField.getValue());
                mtlFilename = baseName + ".mtl";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir, mtlFilename))));
                writeTextures(theScene, out, false, textureExporter);
                out.close();
                textureExporter.saveImages();
            }
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
            writePolyMesh(theScene, out, textureExporter, mtlFilename);
            out.close();
        } catch (IOException | InterruptedException ex) {
            log.atError().setCause(ex).log(Translate.text("Translators:errorExportingScene"));
            MessageDialog.create().withOwner(parent.getComponent()).error(new String[]{Translate.text("Translators:errorExportingScene"), ex.getMessage()});
        }
    }

    /**
     * Write out the scene in OBJ format to the specified PrintWriter. The
     * other parameters correspond to the options in the dialog box displayed
     * by exportFile().
     *
     * @param scene Description of the Parameter
     * @param out Description of the Parameter
     * @param textureExporter Description of the Parameter
     * @param mtlFilename Description of the Parameter
     */
    public static void writePolyMesh(Scene scene, PrintWriter out, TextureImageExporter textureExporter, String mtlFilename) {

        // Write the header information.
        //out.println( "#Produced by Art of Illusion " + ArtOfIllusion.VERSION + ", PolyMesh Plugin, " + ( new Date() ).toString() );
        if (mtlFilename != null) {
            out.println("mtllib " + mtlFilename);
        }

        // Write the objects in the scene.
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(5);
        nf.setGroupingUsed(false);
        for (ObjectInfo info : scene.getObjects()) {

            if (!info.isSelected() || !(info.object instanceof PolyMesh)) {
                continue;
            }
            PolyMesh mesh = (PolyMesh) info.object;
            if (mesh == null) {
                continue;
            }

            Wvertex[] vertices = (Wvertex[]) mesh.getVertices();
            out.println(vertices.length);
            for (int j = 0; j < vertices.length; j++) {
                out.println(vertices[j].r.x + " " + vertices[j].r.y + " " + vertices[j].r.z + " " + vertices[j].edge);
            }
            Wedge[] edges = mesh.getEdges();
            out.println(edges.length);
            for (int j = 0; j < edges.length; j++) {
                out.println(edges[j].vertex + " " + edges[j].next + " " + edges[j].hedge + " " + edges[j].face);
            }
            Wface[] faces = mesh.getFaces();
            out.println(faces.length);
            for (int j = 0; j < faces.length; j++) {
                out.println(faces[j].edge);
            }
        }
    }

    /**
     * Write out the .mtl file describing the textures.
     */
    private static void writeTextures(Scene scene, PrintWriter out, boolean wholeScene, TextureImageExporter textureExporter) {
        // Find all the textures.

        for (ObjectInfo info: scene.getObjects()) {
            if (!wholeScene && !info.isSelected()) {
                continue;
            }
            textureExporter.addObject(info);
        }

        // Write out the .mtl file.
        out.println("#Produced by Art of Illusion " + ArtOfIllusion.getVersion() + ", PolyMesh Plugin, " + new Date());

        Map<String, TextureImageInfo> names = new Hashtable<>();
        TextureSpec spec = new TextureSpec();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(5);
        for (TextureImageInfo info : textureExporter.getTextures()) {

            // Select a name for the texture.
            String baseName = info.getTexture().getName().replace(' ', '_');
            if (names.get(baseName) == null) {
                info.setName(baseName);
            } else {
                int i = 1;
                while (names.get(baseName + i) != null) {
                    i++;
                }
                info.setName( baseName + i);
            }
            names.put(info.getName(), info);

            // Write the texture.
            out.println("newmtl " + info.getName());
            info.getTexture().getAverageSpec(spec, 0.0, info.getParamValues());
            if (info.diffuseFilename == null) {
                out.println("Kd " + nf.format(spec.diffuse.getRed()) + " " + nf.format(spec.diffuse.getGreen()) + " " + nf.format(spec.diffuse.getBlue()));
            } else {
                out.println("Kd 1 1 1");
                out.println("map_Kd " + info.diffuseFilename);
            }
            if (info.hilightFilename == null) {
                out.println("Ks " + nf.format(spec.hilight.getRed()) + " " + nf.format(spec.hilight.getGreen()) + " " + nf.format(spec.hilight.getBlue()));
            } else {
                out.println("Ks 1 1 1");
                out.println("map_Ks " + info.hilightFilename);
            }
            if (info.emissiveFilename == null) {
                out.println("Ka " + nf.format(spec.emissive.getRed()) + " " + nf.format(spec.emissive.getGreen()) + " " + nf.format(spec.emissive.getBlue()));
            } else {
                out.println("Ka 1 1 1");
                out.println("map_Ka " + info.emissiveFilename);
            }
            if (info.hilightFilename == null && spec.hilight.getRed() == 0.0f && spec.hilight.getGreen() == 0.0f && spec.hilight.getBlue() == 0.0f) {
                out.println("illum 1");
            } else {
                out.println("illum 2");
                out.println("Ns " + (int) ((1.0 - spec.roughness) * 128.0 + 1.0));
            }
        }
    }
}
