/* Copyright (C) 2003 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators;

import artofillusion.image.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import java.awt.Image;
import java.io.*;
import java.util.*;

import static artofillusion.texture.Texture.*;
import static artofillusion.texture.Texture.DIFFUSE_COLOR_COMPONENT;
import static artofillusion.texture.Texture.SPECULAR_COLOR_COMPONENT;

/**
 * This class can be used by various other exporters. It collects information about the
 * textures used by a set of objects, and writes out image files for the 2D textures.
 */
public class TextureImageExporter {

    private final Map<Texture, TextureImageInfo> texturesMap = new Hashtable<>();

    private final File dir;
    private final String baseFilename;
    private final int quality;
    private final int components;
    private final int width;
    private final int height;
    private int nextID;

    public static final int DIFFUSE = 1;
    public static final int SPECULAR = 2;
    public static final int HILIGHT = 4;
    public static final int TRANSPARENT = 8;
    public static final int EMISSIVE = 16;
    public static final int BUMP = 32;

    /**
     * Create a new TextureImageExporter.
     *
     * @param dir the directory in which to save images
     * @param baseFilename the base filename to use for image files
     * @param quality the JPEG image quality (from 0 to 100)
     * @param components specifies which components to write images for (a sum of the flags given above)
     * @param width the width to use for images
     * @param height the height to use for images
     */
    public TextureImageExporter(File dir, String baseFilename, int quality, int components, int width, int height) {

        this.dir = dir;
        this.baseFilename = baseFilename;
        this.quality = quality;
        this.components = components;
        this.width = width;
        this.height = height;
        nextID = 1;
    }

    /**
     * Check the texture of an object, and record what information needs to be exported.
     */
    public void addObject(ObjectInfo obj) {
        Texture tex = obj.getGeometry().getTexture();
        if (tex == null) {
            return;
        }
        TextureImageInfo info = texturesMap.get(tex);
        if (info == null) {
            // We haven't encountered this texture before, so create a new TextureImageInfo for it.

            info = new TextureImageInfo(tex, obj.getGeometry().getAverageParameterValues());
            texturesMap.put(tex, info);
            if (tex instanceof ImageMapTexture) {
                // Go through the image maps, and see which ones are being used.

                ImageMapTexture imt = (ImageMapTexture) tex;
                info.diffuseFilename = imt.diffuseColor.getImage() == null ? null : newName();
                info.specularFilename = (imt.specularColor.getImage() != null || imt.specularity.getImage() != null ? newName() : null);
                info.hilightFilename = (imt.specularColor.getImage() != null || imt.shininess.getImage() != null ? newName() : null);
                info.transparentFilename = (imt.transparentColor.getImage() != null || imt.transparency.getImage() != null ? newName() : null);
                info.emissiveFilename = imt.emissiveColor.getImage() == null ? null : newName();
            } else if (tex instanceof ProceduralTexture2D) {
                var output = ((ProceduralTexture2D) tex).getProcedure().getOutputModules();
                info.diffuseFilename = output[0].inputConnected(0) ? newName() : null;
                info.specularFilename = output[1].inputConnected(0) || output[5].inputConnected(0) ? newName() : null;
                info.hilightFilename = output[1].inputConnected(0) || output[6].inputConnected(0) ? newName() : null;
                info.transparentFilename = output[2].inputConnected(0) || output[4].inputConnected(0) ? newName() : null;
                info.emissiveFilename = output[3].inputConnected(0) ? newName() : null;
            }
        }

        // Determine the range of UV coordinates for this object.
        if (tex instanceof ImageMapTexture) {
            info.minU = info.minV = 0.0;
            info.maxU = info.maxV = 1.0;
        } else if (tex instanceof ProceduralTexture2D) {
            Object3D geometry = obj.getGeometry();
            Mesh mesh = geometry instanceof Mesh ? (Mesh) geometry : geometry.convertToTriangleMesh(0.1);
            Mapping2D map = (Mapping2D) geometry.getTextureMapping();
            if (map instanceof UVMapping && mesh instanceof FacetedMesh && ((UVMapping) map).isPerFaceVertex((FacetedMesh) mesh)) {
                for (var cl: ((UVMapping) map).findFaceTextureCoordinates((FacetedMesh) mesh)) {
                    for (var coord: cl) {
                        info.minU = Math.min(coord.x, info.minU);
                        info.maxU = Math.max(coord.x, info.maxU);
                        info.minV = Math.min(coord.y, info.minV);
                        info.maxV = Math.max(coord.y, info.maxV);
                    }
                }
            } else {

                for (var coord:  map.findTextureCoordinates(mesh)) {
                    info.minU = Math.min(coord.x, info.minU);
                    info.maxU = Math.max(coord.x, info.maxU);
                    info.minV = Math.min(coord.y, info.minV);
                    info.maxV = Math.max(coord.y, info.maxV);
                }
            }
        }
    }

    /**
     * Create a new name for an image file.
     */
    private String newName() {
        return baseFilename + (nextID++) + ".jpg";
    }

    /**
     * Get the TextureImageInfo (which may be null) for a particular texture.
     */
    public TextureImageInfo getTextureInfo(Texture tex) {
        return texturesMap.get(tex);
    }

    /**
     * Get Collection of all TextureImageInfos.
     */
    public Collection<TextureImageInfo> getTextures() {
        return texturesMap.values();
    }

    /**
     * Write out all the images for the various textures.
     */
    public void saveImages() throws IOException, InterruptedException {
        for (TextureImageInfo info : texturesMap.values()) {
            if ((components & DIFFUSE) != 0) {
                writeComponentImage(info, DIFFUSE_COLOR_COMPONENT, info.diffuseFilename);
            }
            if ((components & SPECULAR) != 0) {
                writeComponentImage(info, SPECULAR_COLOR_COMPONENT, info.specularFilename);
            }
            if ((components & HILIGHT) != 0) {
                writeComponentImage(info, HILIGHT_COLOR_COMPONENT, info.hilightFilename);
            }
            if ((components & TRANSPARENT) != 0) {
                writeComponentImage(info, TRANSPARENT_COLOR_COMPONENT, info.transparentFilename);
            }
            if ((components & EMISSIVE) != 0) {
                writeComponentImage(info, EMISSIVE_COLOR_COMPONENT, info.emissiveFilename);
            }
        }
    }

    /**
     * Write an image file to disk representing a component of a texture.
     */
    private void writeComponentImage(TextureImageInfo info, int component, String filename) throws IOException, InterruptedException {
        if (filename == null || !(info.getTexture() instanceof Texture2D)) {
            return;
        }
        Image img = ((Texture2D) info.getTexture()).createComponentImage(info.minU, info.maxU, info.minV, info.maxV, width, height, component, 0.0, info.getParamValues());
        ImageSaver.saveImage(img, new File(dir, filename), ImageSaver.FORMAT_JPEG, quality);
    }
}
