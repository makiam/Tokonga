/* Copyright (C) 2018-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.object;

import artofillusion.MaterialPreviewer;
import artofillusion.Scene;
import artofillusion.TextureParameter;
import artofillusion.WireframeMesh;
import artofillusion.animation.Keyframe;
import artofillusion.material.Material;
import artofillusion.material.MaterialMapping;
import artofillusion.material.MaterialSpec;
import artofillusion.math.BoundingBox;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import artofillusion.test.util.StreamUtil;
import artofillusion.texture.ConstantParameterValue;
import artofillusion.texture.LayeredTexture;
import artofillusion.texture.ParameterValue;
import artofillusion.texture.Texture;
import artofillusion.texture.TextureMapping;
import artofillusion.texture.TextureSpec;
import buoy.widget.BFrame;
import buoy.widget.Widget;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;

import buoy.widget.WindowWidget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author maksim.khramov
 */
@DisplayName("Object 3 D Test")
class Object3DTest {

    @BeforeEach
    void setUp() throws Exception {
        DummyObject.canSetTexture = true;
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Attempt To Create Object With Bad Version 1")
    void testAttemptToCreateObjectWithBadVersion1() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Object version;
            wrap.putShort((short) -1);
            new DummyObject(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Attempt To Create Object With Bad Version 2")
    void testAttemptToCreateObjectWithBadVersion2() {
        assertThrows(InvalidObjectException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Object version;
            wrap.putShort((short) 2);
            new DummyObject(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object No Texturable")
    void testCreateObjectNoTexturable() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(2);
        // Object version;
        wrap.putShort((short) 1);
        DummyObject.canSetTexture = false;
        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object Texturable No Input Data")
    void testCreateObjectTexturableNoInputData() {
        assertThrows(IOException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(2);
            // Object version;
            wrap.putShort((short) 1);
            DummyObject.canSetTexture = true;
            new DummyObject(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object No Material But Layered Texture")
    void testCreateObjectNoMaterialButLayeredTexture() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object version;
        wrap.putShort((short) 1);
        // No material
        wrap.putInt(-1);
        // Layered texture
        wrap.putInt(-1);
        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);
        Assertions.assertNotNull(dob);
        Assertions.assertNull(dob.getMaterial());
        Assertions.assertNotNull(dob.getTexture());
        Assertions.assertTrue(dob.getTexture() instanceof LayeredTexture);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object Texture Only")
    void testCreateObjectTextureOnly() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object version;
        wrap.putShort((short) 1);
        // No material
        wrap.putInt(-1);
        // Default scene UniformTexture
        wrap.putInt(0);
        String className = DummyTextureMapping.class.getTypeName();
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);
        Assertions.assertEquals(scene.getDefaultTexture(), dob.getTexture());
        Assertions.assertTrue(dob.getTextureMapping() instanceof DummyTextureMapping);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object But Material Missed")
    void testCreateObjectButMaterialMissed() {
        assertThrows(IOException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object version;
            wrap.putShort((short) 1);
            // Take 0'th material from scene but it is missed
            wrap.putInt(0);
            String className = "dummy.dummy.UnknownMaterialClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            // Layered texture
            wrap.putInt(-1);
            DummyObject.canSetTexture = true;
            new DummyObject(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object With Material")
    void testCreateObjectWithMaterial() throws IOException {
        Scene scene = new Scene();
        scene.addMaterial(new DummyMaterial());
        ByteBuffer wrap = ByteBuffer.allocate(200);
        // Object version;
        wrap.putShort((short) 1);
        // Take 0'th material from scene
        wrap.putInt(0);
        String className = DummyMaterialMapping.class.getTypeName();
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        // Layered texture
        wrap.putInt(-1);
        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);
        Assertions.assertEquals(scene.getMaterial(0), dob.getMaterial());
        Assertions.assertTrue(dob.getMaterialMapping() instanceof DummyMaterialMapping);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @DisplayName("Test Create Object But Texture Missed")
    void testCreateObjectButTextureMissed() {
        assertThrows(IOException.class, () -> {
            Scene scene = new Scene();
            ByteBuffer wrap = ByteBuffer.allocate(200);
            // Object version;
            wrap.putShort((short) 1);
            // No material
            wrap.putInt(-1);
            // Take 0'th texture from scene but texture class is missed
            wrap.putInt(0);
            String className = "dummy.dummy.UnknownTextureClass";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            DummyObject.canSetTexture = true;
            new DummyObject(StreamUtil.stream(wrap), scene);
        });
    }

    @Test
    @DisplayName("Test Get Object Average Value For Empty Parameter Values")
    void testGetObjectAverageValueForEmptyParameterValues() {
        DummyObject dob = new DummyObject();
        Assertions.assertArrayEquals(new double[0], dob.getAverageParameterValues(), 0);
    }

    @Test
    @DisplayName("Test Set And Get Object Average Value For Empty Parameter Values")
    void testSetAndGetObjectAverageValueForEmptyParameterValues() {
        DummyObject dob = new DummyObject();
        ParameterValue[] pv = new ParameterValue[1];
        pv[0] = new ConstantParameterValue(100);
        dob.setParameterValues(pv);
        Assertions.assertArrayEquals(new double[]{100}, dob.getAverageParameterValues(), 0);
    }

    @Test
    @DisplayName("Test Read Parameter Value")
    void testReadParameterValue() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        String className = ConstantParameterValue.class.getTypeName();
        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        // Value to pass to ConstantParameterValue constructor
        wrap.putDouble(100);
        ParameterValue pv = DummyObject.readParameterValue(StreamUtil.stream(wrap));
        Assertions.assertNotNull(pv);
        Assertions.assertTrue(pv instanceof ConstantParameterValue);
        Assertions.assertEquals(100d, pv.getAverageValue(), 0);
    }

    @Test
    @DisplayName("Test Read Parameter Value From Unknown Class")
    void testReadParameterValueFromUnknownClass() {
        assertThrows(IOException.class, () -> {
            ByteBuffer wrap = ByteBuffer.allocate(200);
            String className = "dummy.dummy.Unknown";
            wrap.putShort(Integer.valueOf(className.length()).shortValue());
            wrap.put(className.getBytes());
            DummyObject.readParameterValue(StreamUtil.stream(wrap));
        });
    }

    @Test
    @DisplayName("Test Copy Texture And Material From Empty One")
    void testCopyTextureAndMaterialFromEmptyOne() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();
        target.copyTextureAndMaterial(source);
        Assertions.assertNull(target.getTextureMapping());
        Assertions.assertNull(target.getMaterial());
        Assertions.assertNull(target.getMaterialMapping());
        Assertions.assertNull(target.getParameters());
    }

    @Test
    @DisplayName("Test Copy Texture And Material With Existed Texture No Mapping")
    void testCopyTextureAndMaterialWithExistedTextureNoMapping() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();
        Texture mock = new MockTexture();
        source.setTexture(mock, null);
        target.copyTextureAndMaterial(source);
        Assertions.assertNull(target.getTextureMapping());
        Assertions.assertNull(target.getTexture());
        Assertions.assertNull(target.getMaterial());
        Assertions.assertNull(target.getMaterialMapping());
        Assertions.assertNotNull(target.getParameters());
        Assertions.assertEquals(0, target.getParameters().length);
    }

    @Test
    @DisplayName("Test Copy Texture And Material With Existed Texture And Mapping")
    void testCopyTextureAndMaterialWithExistedTextureAndMapping() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();
        Texture mock = new MockTexture();
        source.setTexture(mock, new DummyTextureMapping(null, source, mock));
        target.copyTextureAndMaterial(source);
        Assertions.assertNotNull(target.getTextureMapping());
        Assertions.assertNotNull(target.getTexture());
        Assertions.assertNull(target.getMaterial());
        Assertions.assertNull(target.getMaterialMapping());
        Assertions.assertNotNull(target.getParameters());
        Assertions.assertEquals(0, target.getParameters().length);
    }

    @Test
    void copyTextureAndMaterialWithExistedTextureMappingAndParameters() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();
        Texture mock = new MockTexture();
        source.setTexture(mock, new DummyTextureMapping(null, source, mock));
        target.copyTextureAndMaterial(source);
        Assertions.assertNotNull(target.getTextureMapping());
        Assertions.assertNotNull(target.getTexture());
        Assertions.assertNull(target.getMaterial());
        Assertions.assertNull(target.getMaterialMapping());
        Assertions.assertNotNull(target.getParameters());
        Assertions.assertEquals(0, target.getParameters().length);
    }

    @Test
    @DisplayName("Test Get Parameter Value From Missed One")
    void testGetParameterValueFromMissedOne() {
        assertThrows(NullPointerException.class, () -> {
            DummyObject source = new DummyObject();
            source.getParameterValue(new TextureParameter(source, "Dummy", 0, 0, 0));
        });
    }

    @Test
    @DisplayName("Test Get Parameter Value From Empty")
    void testGetParameterValueFromEmpty() {
        DummyObject source = new DummyObject();
        source.setParameters(new TextureParameter[0]);
        Assertions.assertNull(source.getParameterValue(new TextureParameter(source, "Dummy", 0, 0, 0)));
    }

    @Test
    @DisplayName("Test Get Parameter Value From Unset Values")
    void testGetParameterValueFromUnsetValues() {
        assertThrows(NullPointerException.class, () -> {
            DummyObject source = new DummyObject();
            TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
            source.setParameters(new TextureParameter[]{tp});
            Assertions.assertNull(source.getParameterValue(tp));
        });
    }

    @Test
    @DisplayName("Test Get Parameter Value From Empty Values")
    void testGetParameterValueFromEmptyValues() {
        DummyObject source = new DummyObject();
        TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
        source.setParameters(new TextureParameter[]{tp});
        source.setParameterValues(new ParameterValue[1]);
        Assertions.assertNull(source.getParameterValue(tp));
    }

    @Test
    @DisplayName("Test Get Parameter Value From Set Values")
    void testGetParameterValueFromSetValues() {
        DummyObject source = new DummyObject();
        TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
        ParameterValue pv = new ConstantParameterValue(100);
        source.setParameters(new TextureParameter[]{tp});
        source.setParameterValues(new ParameterValue[]{pv});
        Assertions.assertNotNull(source.getParameterValue(tp));
        Assertions.assertEquals(pv, source.getParameterValue(tp));
    }

    @DisplayName("Mock Texture")
    static class MockTexture extends Texture {

        @Override
        public boolean hasComponent(int component) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void getAverageSpec(TextureSpec spec, double time, double[] param) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextureMapping getDefaultMapping(Object3D object) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Texture duplicate() {
            return new MockTexture();
        }

        @Override
        public void edit(WindowWidget fr, Scene sc) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @DisplayName("Dummy Texture Mapping")
    static class DummyTextureMapping extends TextureMapping {

        private Object3D object;

        private Texture texture;

        public DummyTextureMapping(DataInputStream in, Object3D obj, Texture texture) {
            this.texture = texture;
            this.object = obj;
        }

        @Override
        public void writeToFile(DataOutputStream out) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Texture getTexture() {
            return texture;
        }

        @Override
        public Object3D getObject() {
            return object;
        }

        @Override
        public void getTextureSpec(Vec3 pos, TextureSpec spec, double angle, double size, double t, double[] param) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void getTransparency(Vec3 pos, RGBColor trans, double angle, double size, double t, double[] param) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDisplacement(Vec3 pos, double size, double t, double[] param) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TextureMapping duplicate() {
            return new DummyTextureMapping(null, object, texture);
        }

        @Override
        public TextureMapping duplicate(Object3D obj, Texture tex) {
            return new DummyTextureMapping(null, obj, tex);
        }

        @Override
        public void copy(TextureMapping map) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @DisplayName("Dummy Material Mapping")
    static class DummyMaterialMapping extends MaterialMapping {

        public DummyMaterialMapping(Object3D target, Material material) {
            super(target, material);
        }

        public DummyMaterialMapping(DataInputStream in, Object3D target, Material material) {
            super(target, material);
        }

        @Override
        public void writeToFile(DataOutputStream out) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getStepSize() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void getMaterialSpec(Vec3 pos, MaterialSpec spec, double size, double t) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MaterialMapping duplicate() {
            return new DummyMaterialMapping(this.getObject(), this.getMaterial());
        }

        @Override
        public MaterialMapping duplicate(Object3D obj, Material mat) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copy(MaterialMapping map) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @DisplayName("Dummy Material")
    static class DummyMaterial extends Material {

        @Override
        public boolean isScattering() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean castsShadows() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MaterialMapping getDefaultMapping(Object3D obj) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Material duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void edit(WindowWidget fr, Scene sc) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @DisplayName("Dummy Object")
    private static class DummyObject extends Object3D {

        public static boolean canSetTexture = true;

        public DummyObject() {
        }

        public DummyObject(DataInputStream in, Scene theScene) throws IOException {
            super(in, theScene);
        }

        @Override
        public Object3D duplicate() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void copyObject(Object3D obj) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BoundingBox getBounds() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSize(double xsize, double ysize, double zsize) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public WireframeMesh getWireframeMesh() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Keyframe getPoseKeyframe() {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void applyPoseKeyframe(Keyframe k) {
            // To change body of generated methods, choose Tools | Templates.
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean canSetTexture() {
            return canSetTexture;
        }
    }
}
