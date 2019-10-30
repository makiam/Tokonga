/* Copyright (C) 2003 by Peter Eastman
 *  Changes copyright 2019 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.translators;

import artofillusion.image.*;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.texture.*;
import java.awt.Image;
import java.io.*;
import java.util.*;

/**
 * This class can be used by various other exporters. It collects information about the textures
 * used by a set of objects, and writes out image files for the 2D textures.
 */
public class TextureImageExporter {

    private Map<Texture, TextureImageInfo> textureTable;
    private File dir;
    private String baseFilename;
    private int quality, components, width, height;
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
     * @param components specifies which components to write images for (a sum of the flags given
     * above)
     * @param width the width to use for images
     * @param height the height to use for images
     */
    public TextureImageExporter(File dir, String baseFilename, int quality, int components, int width, int height) {
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
        Texture tex = obj.getObject().getTexture();
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
                artofillusion.procedural.Module output[] = ((ProceduralTexture2D) tex).getProcedure().getOutputModules();
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
            Mesh mesh = (obj.getObject() instanceof Mesh ? (Mesh) obj.getObject() : obj.getObject().convertToTriangleMesh(0.1));
            Mapping2D map = (Mapping2D) obj.getObject().getTextureMapping();
            if (map instanceof UVMapping && mesh instanceof FacetedMesh && ((UVMapping) map).isPerFaceVertex((FacetedMesh) mesh)) {
                Vec2 coordinates[][] = ((UVMapping) map).findFaceTextureCoordinates((FacetedMesh) mesh);
                for (Vec2[] coordinatesLine : coordinates) {
                    for (Vec2 coordinate : coordinatesLine) {
                        if (coordinate.x < info.minu) {
                            info.minu = coordinate.x;
                        }
                        if (coordinate.x > info.maxu) {
                            info.maxu = coordinate.x;
                        }
                        if (coordinate.y < info.minv) {
                            info.minv = coordinate.y;
                        }
                        if (coordinate.y > info.maxv) {
                            info.maxv = coordinate.y;
                        }
                    }
                }
            } else {
                Vec2 coordinates[] = map.findTextureCoordinates(mesh);
                for (Vec2 coordinate : coordinates) {
                    if (coordinate.x < info.minu) {
                        info.minu = coordinate.x;
                    }
                    if (coordinate.x > info.maxu) {
                        info.maxu = coordinate.x;
                    }
                    if (coordinate.y < info.minv) {
                        info.minv = coordinate.y;
                    }
                    if (coordinate.y > info.maxv) {
                        info.maxv = coordinate.y;
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
        return tex == null ? null : textureTable.get(tex);
    }

    /**
     * @return Collection of TextureImageInfo's
     */
    public Collection<TextureImageInfo> getTextures() {
        return textureTable.values();
    }


    /**
     * Write out all of the images for the various textures.
     */
    public void saveImages() throws IOException, InterruptedException {
        for(TextureImageInfo info: textureTable.values()) {
            if ((components & DIFFUSE) != 0) {
                writeComponentImage(info, Texture2D.DIFFUSE_COLOR_COMPONENT, info.diffuseFilename);
            }
            if ((components & SPECULAR) != 0) {
                writeComponentImage(info, Texture2D.SPECULAR_COLOR_COMPONENT, info.specularFilename);
            }
            if ((components & HILIGHT) != 0) {
                writeComponentImage(info, Texture2D.HILIGHT_COLOR_COMPONENT, info.hilightFilename);
            }
            if ((components & TRANSPARENT) != 0) {
                writeComponentImage(info, Texture2D.TRANSPARENT_COLOR_COMPONENT, info.transparentFilename);
            }
            if ((components & EMISSIVE) != 0) {
                writeComponentImage(info, Texture2D.EMISSIVE_COLOR_COMPONENT, info.emissiveFilename);
            }
        }
    }

    /**
     * Write an image file to disk representing a component of a texture.
     */
    private void writeComponentImage(TextureImageInfo info, int component, String filename) throws IOException, InterruptedException {
        if (filename == null || !(info.texture instanceof Texture2D)) {
            return;
        }
        Image img = ((Texture2D) info.texture).createComponentImage(info.minu, info.maxu, info.minv, info.maxv, width, height, component, 0.0, info.paramValue);
        ImageSaver.saveImage(img, new File(dir, filename), ImageSaver.FORMAT_JPEG, quality);
    }
}
