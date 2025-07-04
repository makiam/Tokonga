/* Copyright (C) 2003 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

 This program is free software; you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation; either version 2 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY 
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.image.ImageSaver;
import artofillusion.math.Vec2;
import artofillusion.object.Mesh;
import artofillusion.object.ObjectInfo;
import artofillusion.object.TriangleMesh;

import artofillusion.texture.ImageMapTexture;
import artofillusion.texture.Mapping2D;
import artofillusion.texture.ProceduralTexture2D;
import artofillusion.texture.Texture;
import artofillusion.texture.Texture2D;
import artofillusion.texture.UVMapping;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import static artofillusion.texture.Texture.*;
import static artofillusion.texture.Texture.EMISSIVE_COLOR_COMPONENT;
import static artofillusion.texture.Texture.TRANSPARENT_COLOR_COMPONENT;

/**
 * This class can be used by various other exporters. It collects information about the
 * textures used by a set of objects, and writes out image files for the 2D textures.
 */
public class TextureImageExporter {

    private final Map<Texture, TextureImageInfo> textureTable;

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
    public TextureImageExporter(File dir, String baseFilename, int quality,
            int components, int width, int height) {
        textureTable = new Hashtable<>();

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
        Texture tex = obj.object.getTexture();
        if (tex == null) {
            return;
        }
        TextureImageInfo info = textureTable.get(tex);
        if (info == null) {
            // We haven't encountered this texture before, so create a new TextureImageInfo for it.

            info = new TextureImageInfo(tex, obj.getObject().getAverageParameterValues());
            textureTable.put(tex, info);
            if (tex instanceof ImageMapTexture) {
                // Go through the image maps, and see which ones are being used.

                ImageMapTexture imt = (ImageMapTexture) tex;
                info.diffuseFilename = (imt.diffuseColor.getImage() != null ? newName() : null);
                info.specularFilename = (imt.specularColor.getImage() != null || imt.specularity.getImage() != null ? newName() : null);
                info.hilightFilename = (imt.specularColor.getImage() != null || imt.shininess.getImage() != null ? newName() : null);
                info.transparentFilename = (imt.transparentColor.getImage() != null || imt.transparency.getImage() != null ? newName() : null);
                info.emissiveFilename = (imt.emissiveColor.getImage() != null ? newName() : null);
            } else if (tex instanceof ProceduralTexture2D) {
                var output = ((ProceduralTexture2D) tex).getProcedure().getOutputModules();
                info.diffuseFilename = (output[0].inputConnected(0) ? newName() : null);
                info.specularFilename = (output[1].inputConnected(0) || output[5].inputConnected(0) ? newName() : null);
                info.hilightFilename = (output[1].inputConnected(0) || output[6].inputConnected(0) ? newName() : null);
                info.transparentFilename = (output[2].inputConnected(0) || output[4].inputConnected(0) ? newName() : null);
                info.emissiveFilename = (output[3].inputConnected(0) ? newName() : null);
            }
        }

        // Determine the range of UV coordinates for this object.
        if (tex instanceof ImageMapTexture) {
            info.minu = info.minv = 0.0;
            info.maxu = info.maxv = 1.0;
        } else if (tex instanceof ProceduralTexture2D) {
            Mesh mesh = (obj.object instanceof Mesh ? (Mesh) obj.object
                    : obj.object.convertToTriangleMesh(0.1));
            Mapping2D map = (Mapping2D) obj.object.getTextureMapping();
            if (map instanceof UVMapping && mesh instanceof TriangleMesh
                    && ((UVMapping) map).isPerFaceVertex((TriangleMesh) mesh)) {
                Vec2[][] coords = ((UVMapping) map).findFaceTextureCoordinates((TriangleMesh) mesh);
                for (int i = 0; i < coords.length; i++) {
                    for (int j = 0; j < coords[i].length; j++) {
                        if (coords[i][j].x < info.minu) {
                            info.minu = coords[i][j].x;
                        }
                        if (coords[i][j].x > info.maxu) {
                            info.maxu = coords[i][j].x;
                        }
                        if (coords[i][j].y < info.minv) {
                            info.minv = coords[i][j].y;
                        }
                        if (coords[i][j].y > info.maxv) {
                            info.maxv = coords[i][j].y;
                        }
                    }
                }
            } else {
                Vec2[] coords = map.findTextureCoordinates(mesh);
                for (int i = 0; i < coords.length; i++) {
                    if (coords[i].x < info.minu) {
                        info.minu = coords[i].x;
                    }
                    if (coords[i].x > info.maxu) {
                        info.maxu = coords[i].x;
                    }
                    if (coords[i].y < info.minv) {
                        info.minv = coords[i].y;
                    }
                    if (coords[i].y > info.maxv) {
                        info.maxv = coords[i].y;
                    }
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
        return textureTable.get(tex);
    }

    /**
     * Get an Enumeration of all TextureImageInfos.
     */
    public Collection<TextureImageInfo> getTextures() {
        return textureTable.values();
    }

    /**
     * Write out all of the images for the various textures.
     */
    public void saveImages() throws IOException, InterruptedException {
        for (TextureImageInfo info : textureTable.values()) {
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
     * Write an image file to disk representating a component of a texture.
     */
    private void writeComponentImage(TextureImageInfo info, int component, String filename) throws IOException, InterruptedException {
        if (filename == null || !(info.texture instanceof Texture2D)) {
            return;
        }
        Image img = ((Texture2D) info.texture).createComponentImage(info.minu,
                info.maxu, info.minv, info.maxv, width, height, component, 0.0,
                info.paramValue);
        ImageSaver.saveImage(img, new File(dir, filename), ImageSaver.FORMAT_JPEG, quality);
    }
}
