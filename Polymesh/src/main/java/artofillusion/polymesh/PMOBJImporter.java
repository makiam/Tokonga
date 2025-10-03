/*
 *  Copyright (C) 2002,2004 by Peter Eastman, Modifications (C) 2005 by François Guillet for PolyMesh adaptation
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
import artofillusion.Camera;
import artofillusion.Scene;

import artofillusion.image.ImageMap;
import artofillusion.image.ImageOrColor;
import artofillusion.image.ImageOrValue;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec2;
import artofillusion.math.Vec3;
import artofillusion.object.DirectionalLight;
import artofillusion.object.ObjectInfo;
import artofillusion.object.SceneCamera;
import artofillusion.texture.ImageMapTexture;
import artofillusion.texture.Texture;
import artofillusion.texture.Texture2D;
import artofillusion.texture.UVMapping;
import artofillusion.texture.UniformTexture;
import artofillusion.ui.Translate;

import buoy.widget.BFrame;
import buoy.widget.BStandardDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * PMOBJImporter imports .OBJ files to Polymeshes.
 *
 * @author François Guillet
 */
@Slf4j
public class PMOBJImporter {

    /**
     * Description of the Method
     *
     * @param parent Description of the Parameter
     */
    public static void importFile(BFrame parent) {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogTitle(Translate.text("Translators:importOBJ"));
        Optional.ofNullable(ArtOfIllusion.getCurrentDirectory()).ifPresent(dir -> jfc.setCurrentDirectory(new File(dir)));

        if(jfc.showOpenDialog(parent.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File f = jfc.getSelectedFile();
        ArtOfIllusion.setCurrentDirectory(jfc.getCurrentDirectory().getAbsolutePath());
        String objName = f.getName();
        if (objName.lastIndexOf('.') > 0) {
            objName = objName.substring(0, objName.lastIndexOf('.'));
        }

        // Create a scene to add objects to.
        Scene theScene = new Scene();
        CoordinateSystem coords = new CoordinateSystem(new Vec3(0.0, 0.0, Camera.DEFAULT_DISTANCE_TO_SCREEN), new Vec3(0.0, 0.0, -1.0), Vec3.vy());
        ObjectInfo info = new ObjectInfo(new SceneCamera(), coords, "Camera 1");

        theScene.addObject(info, null);
        info = new ObjectInfo(new DirectionalLight(new RGBColor(1.0f, 1.0f, 1.0f), 0.8f), coords.duplicate(), "Light 1");

        theScene.addObject(info, null);

        // Open the file and read the contents.
        Map<String, Vector<FaceInfo>> groupTable = new Hashtable<>();
        Map<String, TextureInfo> textureTable = new Hashtable<>();
        List<Vec3> vertex = new Vector<>();
        List<Vec3> normal = new Vector<>();
        List<Vec3> texture = new Vector<>();
        Vector<FaceInfo>[] face = new Vector[]{new Vector<>()};  // The array of Vector<FaceInfo>
        groupTable.put("default", face[0]);
        int lineNo = 0;
        int smoothingGroup = -1;
        String currentTexture = null;
        VertexInfo[] vertIndex = new VertexInfo[3];
        double[] val = new double[3];
        double[] min = new double[]{Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = new double[]{-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        String s;

        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            while ((s = in.readLine()) != null) {
                lineNo++;
                if (s.startsWith("#")) {
                    continue;
                }
                if (s.endsWith("\\")) {
                    String s2;
                    while (s.endsWith("\\") && (s2 = in.readLine()) != null) {
                        s = s.substring(0, s.length() - 1) + s2;
                    }
                }
                String[] fields = breakLine(s);
                if (fields.length == 0) {
                    continue;
                }
                if ("v".equals(fields[0]) && fields.length == 4) {
                    // Read in a vertex.

                    for (int i = 0; i < 3; i++) {
                        try {
                            val[i] = Double.parseDouble(fields[i + 1]);
                            if (val[i] < min[i]) {
                                min[i] = val[i];
                            }
                            if (val[i] > max[i]) {
                                max[i] = val[i];
                            }
                        } catch (NumberFormatException ex) {
                            throw new Exception("Illegal value '" + fields[i + 1] + "' found in line " + lineNo + ".");
                        }
                    }
                    vertex.add(new Vec3(val[0], val[1], val[2]));
                } else if ("vn".equals(fields[0]) && fields.length == 4) {
                    // Read in a vertex normal.

                    for (int i = 0; i < 3; i++) {
                        try {
                            val[i] = Double.parseDouble(fields[i + 1]);
                        } catch (NumberFormatException ex) {
                            throw new Exception("Illegal value '" + fields[i + 1] + "' found in line " + lineNo + ".");
                        }
                    }
                    normal.add(new Vec3(val[0], val[1], val[2]));
                } else if ("vt".equals(fields[0]) && fields.length > 1) {
                    // Read in a texture vertex.

                    for (int i = 0; i < 3; i++) {
                        try {
                            if (i < fields.length - 1) {
                                val[i] = Double.parseDouble(fields[i + 1]);
                            } else {
                                val[i] = 0.0;
                            }
                        } catch (NumberFormatException ex) {
                            throw new Exception("Illegal value '" + fields[i + 1] + "' found in line " + lineNo + ".");
                        }
                    }
                    texture.add(new Vec3(val[0], val[1], val[2]));
                } else if ("f".equals(fields[0])) {
                    vertIndex = new VertexInfo[fields.length - 1];
                    for (int i = 0; i < vertIndex.length; i++) {
                        vertIndex[i] = parseVertexSpec(fields[i + 1], vertex.size(), texture.size(), normal.size(), lineNo);
                    }
                    for (int i = 0; i < face.length; i++) {
                        // Add a face.
                        face[i].add(new FaceInfo(vertIndex, smoothingGroup, currentTexture));
                    }
                } else if ("s".equals(fields[0])) {
                    // Set the smoothing group.

                    if (fields.length == 1 || "off".equalsIgnoreCase(fields[1])) {
                        smoothingGroup = 0;
                        continue;
                    }
                    try {
                        smoothingGroup = Integer.parseInt(fields[1]);
                    } catch (NumberFormatException ex) {
                        throw new Exception("Illegal value '" + fields[1] + "' found in line " + lineNo + ".");
                    }
                } else if ("g".equals(fields[0])) {
                    // Set the current group or groups.

                    face = new Vector[fields.length - 1];
                    for (int i = 0; i < face.length; i++) {
                        face[i] = groupTable.get(fields[i + 1]);
                        if (face[i] == null) {
                            face[i] = new Vector<>();
                            groupTable.put(fields[i + 1], face[i]);
                        }
                    }
                } else if ("usemtl".equals(fields[0]) && fields.length > 1) {
                    // Set the current texture.

                    currentTexture = fields[1];
                } else if ("mtllib".equals(fields[0])) {
                    // Load one or more texture libraries.

                    for (int i = 1; i < fields.length; i++) {
                        parseTextures(fields[i], jfc.getCurrentDirectory(), textureTable);
                    }
                }
            }

            // If necessary, rescale the vertices to make the object an appropriate size.
            double maxSize = Math.max(Math.max(max[0] - min[0], max[1] - min[1]), max[2] - min[2]);
            double scale = Math.pow(10.0, -Math.floor(Math.log(maxSize) / Math.log(10.0)));
            vertex.forEach(item -> item.scale(scale));

            // Create a poly mesh for each group.
            Map<String, Texture> realizedTextures = new Hashtable<>();
            Map<String, ImageMap> imageMaps = new Hashtable<>();

            for (Map.Entry<String, Vector<FaceInfo>> entry: groupTable.entrySet()) {
                var group = entry.getKey();
                var groupFaces = entry.getValue();
                if (groupFaces.isEmpty()) {
                    continue;
                }

                // Find which vertices are used by faces in this group.
                int[] realIndex = new int[vertex.size()];
                for (int i = 0; i < realIndex.length; i++) {
                    realIndex[i] = -1;
                }
                int[][] fc = new int[groupFaces.size()][];
                int numVert = 0;
                for (int i = 0; i < fc.length; i++) {
                    FaceInfo fi = groupFaces.get(i);
                    for (int j = 0; j < fi.vi.length; j++) {
                        if (realIndex[fi.getVertex(j).vert] == -1) {
                            realIndex[fi.getVertex(j).vert] = numVert++;
                        }
                    }
                    fc[i] = new int[fi.vi.length];
                    for (int j = 0; j < fi.vi.length; j++) {
                        fc[i][j] = realIndex[fi.getVertex(j).vert];
                    }

                }

                // Build the list of vertices and center them.
                Vec3[] vert = new Vec3[numVert];
                Vec3 center = new Vec3();
                for (int i = 0; i < realIndex.length; i++) {
                    if (realIndex[i] > -1) {
                        vert[realIndex[i]] = vertex.get(i);
                        center.add(vert[realIndex[i]]);
                    }
                }
                center.scale(1.0 / vert.length);
                for (int i = 0; i < vert.length; i++) {
                    vert[i] = vert[i].minus(center);
                }
                coords = new CoordinateSystem(center, Vec3.vz(), Vec3.vy());
                info = new ObjectInfo(new PolyMesh(vert, fc), coords, ("default".equals(group) ? objName : group));

                // Find the smoothness values for the edges.
                PolyMesh.Wedge[] edges = ((PolyMesh) info.object).getEdges();
                for (int i = 0; i < edges.length; i++) {
                    if (edges[i].face == -1 || edges[edges[i].hedge].face == -1) {
                        continue;
                    }
                    FaceInfo f1 = groupFaces.get(edges[i].face);
                    FaceInfo f2 = groupFaces.get(edges[edges[i].hedge].face);
                    if (f1.smoothingGroup == 0 || f1.smoothingGroup != f2.smoothingGroup) {
                        // They are in different smoothing groups.

                        edges[i].smoothness = 0.0f;
                        continue;
                    }

                    // Find matching vertices and compare their normals.
                    for (int j = 0; j < f1.vi.length; j++) {
                        for (int k = 0; k < f2.vi.length; k++) {
                            if (f1.getVertex(j).vert == f2.getVertex(k).vert) {
                                int n1 = f1.getVertex(j).norm;
                                int n2 = f2.getVertex(k).norm;
                                if (n1 != n2 && (normal.get(n1)).distance(normal.get(n2)) > 1e-10) {
                                    edges[i].smoothness = 0.0f;
                                }
                                break;
                            }
                        }
                    }
                }

                // Set the texture.  For the moment, assume a single texture per group.  In the future, this could possibly
                // be improved to deal correctly with per-face textures.
                String texName = groupFaces.get(0).texture;
                if (texName != null && textureTable.get(texName) != null) {
                    Texture tex = realizedTextures.get(texName);
                    if (tex == null) {
                        tex = createTexture(textureTable.get(texName), theScene, jfc.getCurrentDirectory(), imageMaps, parent);
                        realizedTextures.put(texName, tex);
                    }
                    if (tex instanceof Texture2D) {
                        // Set the UV coordinates.

                        UVMapping map = new UVMapping(info.getObject(), tex);
                        info.setTexture(tex, map);
                        Vec2[] uv = new Vec2[numVert];
                        boolean needPerFace = false;
                        for (int j = 0; j < groupFaces.size() && !needPerFace; j++) {
                            FaceInfo fi = groupFaces.get(j);
                            for (int k = 0; k < fi.vi.length; k++) {
                                VertexInfo vi = fi.getVertex(k);
                                Vec3 texCoords = (vi.tex < texture.size() ? texture.get(vi.tex) : vertex.get(vi.vert));
                                Vec2 tc = new Vec2(texCoords.x, texCoords.y);
                                //per face per vertex texture is not handled in PolyMeshes
                                //if (uv[realIndex[vi.vert]] != null && !uv[realIndex[vi.vert]].equals(tc))
                                //  needPerFace = true;
                                uv[realIndex[vi.vert]] = tc;
                            }
                        }
                        map.setTextureCoordinates(info.object, uv);
                    } else {
                        info.setTexture(tex, tex.getDefaultMapping(info.object));
                    }
                }
                theScene.addObject(info, null);
            }
        } catch (Exception ex) {
            log.atError().setCause(ex).log(Translate.text("errorLoadingFile"));
            new BStandardDialog("Error", new String[]{Translate.text("errorLoadingFile"), ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(parent);
            return;
        }
        ArtOfIllusion.newWindow(theScene);
    }

    /**
     * Separate a line into pieces divided by whitespace.
     */
    private static String[] breakLine(String line) {
        StringTokenizer st = new StringTokenizer(line);
        List<String> tokens = new ArrayList<>();

        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens.toArray(new String[0]);
    }

    /**
     * Parse the specification for a vertex and return the index of the vertex
     * to use.
     */
    private static VertexInfo parseVertexSpec(String spec, int vertex, int texture, int normal, int lineNo) throws Exception {
        VertexInfo info = new VertexInfo();
        StringTokenizer st = new StringTokenizer(spec, "/", true);
        info.tex = info.norm = Integer.MAX_VALUE;
        int i = 0;
        while (st.hasMoreTokens()) {
            String value = st.nextToken();
            if ("/".equals(value)) {
                i++;
                continue;
            }
            try {
                int index = Integer.parseInt(value);
                int total;
                if (i == 0) {
                    total = vertex;
                } else if (i == 1) {
                    total = texture;
                } else {
                    total = normal;
                }
                if (index < 0) {
                    index += total;
                } else {
                    index--;
                }
                if (i == 0) {
                    info.vert = index;
                } else if (i == 1) {
                    info.tex = index;
                } else {
                    info.norm = index;
                }
            } catch (NumberFormatException ex) {
                throw new Exception("Illegal value '" + spec + "' found in line " + lineNo + ".");
            }
        }
        if (info.tex == Integer.MAX_VALUE) {
            info.tex = info.vert;
        }
        if (info.norm == Integer.MAX_VALUE) {
            info.norm = info.vert;
        }
        return info;
    }

    /**
     * Parse the contents of a .mtl file and add TextureInfo object to a hashtable.
     */
    private static void parseTextures(String file, File baseDir, Map<String, TextureInfo> textures) throws Exception {
        File f = new File(baseDir, file);
        if (!f.isFile()) {
            f = new File(file);
        }
        if (!f.isFile()) {
            throw new Exception("Cannot locate material file '" + file + "'.");
        }
        BufferedReader in = Files.newBufferedReader(f.toPath());
        String line;
        TextureInfo currentTexture = null;
        while ((line = in.readLine()) != null) {
            try {
                if (line.startsWith("#")) {
                    continue;
                }
                String[] fields = breakLine(line);
                if (fields.length == 0) {
                    continue;
                }
                if ("newmtl".equals(fields[0])) {
                    // This is the start of a new texture.

                    currentTexture = null;
                    if (fields.length == 1 || textures.get(fields[1]) != null) {
                        continue;
                    }
                    currentTexture = new TextureInfo();
                    currentTexture.name = fields[1];
                    textures.put(fields[1], currentTexture);
                }
                if (currentTexture == null || fields.length < 2) {
                    continue;
                }
                if ("Kd".equals(fields[0])) {
                    currentTexture.diffuse = parseColor(fields);
                } else if ("Ka".equals(fields[0])) {
                    currentTexture.ambient = parseColor(fields);
                } else if ("Ks".equals(fields[0])) {
                    currentTexture.specular = parseColor(fields);
                } else if ("d".equals(fields[0]) || "Tr".equals(fields[0])) {
                    currentTexture.transparency = 1.0 - Double.parseDouble(fields[1]);
                } else if ("Ns".equals(fields[0])) {
                    currentTexture.shininess = Double.parseDouble(fields[1]);
                } else if ("map_Kd".equals(fields[0])) {
                    currentTexture.diffuseMap = fields[1];
                } else if ("map_Ka".equals(fields[0])) {
                    currentTexture.ambientMap = fields[1];
                } else if ("map_Ks".equals(fields[0])) {
                    currentTexture.specularMap = fields[1];
                } else if ("map_d".equals(fields[0])) {
                    currentTexture.transparentMap = fields[1];
                } else if ("map_Bump".equals(fields[0])) {
                    currentTexture.bumpMap = fields[1];
                }
            } catch (Exception ex) {
                in.close();
                throw new Exception("Illegal line '" + line + "' found in file '" + file + "'.");
            }
        }
        in.close();
    }

    /**
     * Create a texture from a TextureInfo and add it to the scene.
     */
    private static Texture createTexture(TextureInfo info, Scene scene, File baseDir, Map<String, ImageMap> imageMaps, BFrame parent) throws Exception {
        info.resolveColors();
        ImageMap diffuseMap = loadMap(info.diffuseMap, scene, baseDir, imageMaps, parent);
        ImageMap specularMap = loadMap(info.specularMap, scene, baseDir, imageMaps, parent);
        ImageMap transparentMap = loadMap(info.transparentMap, scene, baseDir, imageMaps, parent);
        ImageMap bumpMap = loadMap(info.bumpMap, scene, baseDir, imageMaps, parent);
        RGBColor transparentColor = new RGBColor(info.transparency, info.transparency, info.transparency);
        if (diffuseMap == null && specularMap == null && transparentMap == null && bumpMap == null) {
            // Create a uniform texture.

            UniformTexture tex = new UniformTexture();
            tex.diffuseColor = info.diffuse.duplicate();
            tex.specularColor = info.specular.duplicate();
            tex.transparentColor = transparentColor;
            tex.shininess = (float) info.specularity;
            tex.specularity = 0.0f;
            tex.roughness = info.roughness;
            tex.setName(info.name);
            scene.addTexture(tex);
            return tex;
        } else {
            // Create an image mapped texture.

            ImageMapTexture tex = new ImageMapTexture();
            tex.diffuseColor = (diffuseMap == null ? new ImageOrColor(info.diffuse) : new ImageOrColor(info.diffuse, diffuseMap));
            tex.specularColor = (specularMap == null ? new ImageOrColor(info.specular) : new ImageOrColor(info.specular, specularMap));
            tex.transparentColor = (transparentMap == null ? new ImageOrColor(transparentColor) : new ImageOrColor(transparentColor, transparentMap));
            if (bumpMap != null) {
                tex.bump = new ImageOrValue(1.0f, bumpMap, 0);
            }
            tex.shininess = new ImageOrValue((float) info.specularity);
            tex.specularity = new ImageOrValue(0.0f);
            tex.roughness = new ImageOrValue((float) info.roughness);
            tex.tileX = tex.tileY = true;
            tex.mirrorX = tex.mirrorY = false;
            tex.setName(info.name);
            scene.addTexture(tex);
            return tex;
        }
    }

    /**
     * Return the image map corresponding to the specified filename, and add it to the scene.
     */
    private static ImageMap loadMap(String name, Scene scene, File baseDir, Map<String, ImageMap> imageMaps, BFrame parent) throws Exception {
        if (name == null) {
            return null;
        }
        ImageMap map = imageMaps.get(name);
        if (map != null) {
            return map;
        }
        File f = new File(baseDir, name);
        if (!f.isFile()) {
            f = new File(name);
        }
        if (!f.isFile()) {
            throw new Exception("Cannot locate image map file '" + name + "'.");
        }
        try {
            map = ImageMap.loadImage(f);
        } catch (InterruptedException ex) {
            throw new Exception("Unable to load image map file '" + f.getAbsolutePath() + "'.");
        }
        scene.addImage(map);
        imageMaps.put(name, map);
        return map;
    }

    /**
     * Parse the specification for a color.
     *
     * @param fields Description of the Parameter
     * @return Description of the Return Value
     * @exception NumberFormatException Description of the Exception
     */
    private static RGBColor parseColor(String[] fields) throws NumberFormatException {
        if (fields.length < 4) {
            return null;
        }
        return new RGBColor(Double.parseDouble(fields[1]), Double.parseDouble(fields[2]), Double.parseDouble(fields[3]));
    }

    /**
     * Inner class for storing information about a vertex of a face.
     */
    private static class VertexInfo {

        public int vert, norm, tex;
    }

    /**
     * Inner class for storing information about a face.
     *
     */
    private static class FaceInfo {

        public final VertexInfo[] vi;
        public final int smoothingGroup;
        public final String texture;

        public FaceInfo(VertexInfo v1, VertexInfo v2, VertexInfo v3, int smoothingGroup, String texture) {
            this.vi = new VertexInfo[] {v1, v2, v3};
            this.smoothingGroup = smoothingGroup;
            this.texture = texture;
        }

        /**
         * Constructor for the FaceInfo object
         *
         * @param vi Description of the Parameter
         * @param smoothingGroup Description of the Parameter
         * @param texture Description of the Parameter
         */
        public FaceInfo(VertexInfo[] vi, int smoothingGroup, String texture) {
            this.vi = vi;
            this.smoothingGroup = smoothingGroup;
            this.texture = texture;
        }

        /**
         * Gets the vertex attribute of the FaceInfo object
         *
         * @param index Description of the Parameter
         * @return The vertex value
         */
        public VertexInfo getVertex(int index) {
            return vi[index];
        }
    }

    /**
     * Inner class for storing information about a texture in a .mtl file.
     */
    private static class TextureInfo {

        /**
         * Description of the Field
         */
        public String name;
        /**
         * Description of the Field
         */
        public RGBColor ambient, diffuse, specular;
        /**
         * Description of the Field
         */
        public double shininess, transparency, specularity, roughness;
        /**
         * Description of the Field
         */
        public String ambientMap, diffuseMap, specularMap, transparentMap, bumpMap;

        /**
         * This should be called once, after the TextureInfo is created but
         * before it is actually used. It converts from the representation used
         * by .obj files to the one used by Art of Illusion.
         */
        public void resolveColors() {
            if (diffuse == null) {
                diffuse = new RGBColor(0.0, 0.0, 0.0);
            }
            if (ambient == null) {
                ambient = new RGBColor();
            }
            if (specular == null) {
                specular = new RGBColor();
            } else {
                specularity = 1.0;
            }
            diffuse.scale(1.0 - transparency);
            specular.scale(1.0 - transparency);
            roughness = 1.0 - (shininess - 1.0) / 128.0;
            if (roughness > 1.0) {
                roughness = 1.0;
            }
            checkColorRange(ambient);
            checkColorRange(diffuse);
            checkColorRange(specular);
        }

        /**
         * Make sure that the components of a color are all between 0 and 1.
         *
         * @param c Description of the Parameter
         */
        private void checkColorRange(RGBColor c) {
            float r = c.getRed();
            float g = c.getGreen();
            float b = c.getBlue();
            if (r < 0.0f) {
                r = 0.0f;
            }
            if (r > 1.0f) {
                r = 1.0f;
            }
            if (g < 0.0f) {
                g = 0.0f;
            }
            if (g > 1.0f) {
                g = 1.0f;
            }
            if (b < 0.0f) {
                b = 0.0f;
            }
            if (b > 1.0f) {
                b = 1.0f;
            }
            c.setRGB(r, g, b);
        }
    }
}
