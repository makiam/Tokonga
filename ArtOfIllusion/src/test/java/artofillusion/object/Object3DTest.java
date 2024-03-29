/* Copyright (C) 2018-2023 by Maksim Khramov

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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class Object3DTest {

    @Before
    public void setUp() throws Exception {
        DummyObject.canSetTexture = true;
    }

    @Test(expected = InvalidObjectException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testAttemptToCreateObjectWithBadVersion1() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(2);
        wrap.putShort((short) -1); // Object version;

        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test(expected = InvalidObjectException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testAttemptToCreateObjectWithBadVersion2() throws IOException {
        Scene scene = new Scene();

        ByteBuffer wrap = ByteBuffer.allocate(2);
        wrap.putShort((short) 2); // Object version;

        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectNoTexturable() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(2);
        wrap.putShort((short) 1); // Object version;

        DummyObject.canSetTexture = false;
        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test(expected = IOException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectTexturableNoInputData() throws IOException {
        Scene scene = new Scene();

        ByteBuffer wrap = ByteBuffer.allocate(2);
        wrap.putShort((short) 1); // Object version;

        DummyObject.canSetTexture = true;
        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectNoMaterialButLayeredTexture() throws IOException {
        Scene scene = new Scene();

        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Object version;
        wrap.putInt(-1);  // No material
        wrap.putInt(-1);  // Layered texture

        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);
        Assert.assertNotNull(dob);

        Assert.assertNull(dob.getMaterial());
        Assert.assertNotNull(dob.getTexture());
        Assert.assertTrue(dob.getTexture() instanceof LayeredTexture);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectTextureOnly() throws IOException {
        Scene scene = new Scene();

        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Object version;
        wrap.putInt(-1);  // No material
        wrap.putInt(0);  // Default scene UniformTexture

        String className = DummyTextureMapping.class.getTypeName();

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());

        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);

        Assert.assertEquals(scene.getDefaultTexture(), dob.getTexture());
        Assert.assertTrue(dob.getTextureMapping() instanceof DummyTextureMapping);

    }

    @Test(expected = IOException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectButMaterialMissed() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Object version;
        wrap.putInt(0);  // Take 0'th material from scene but it is missed

        String className = "dummy.dummy.UnknownMaterialClass";

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());

        wrap.putInt(-1);  // Layered texture

        DummyObject.canSetTexture = true;
        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectWithMaterial() throws IOException {
        Scene scene = new Scene();
        scene.addMaterial(new DummyMaterial());

        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Object version;
        wrap.putInt(0);  // Take 0'th material from scene

        String className = DummyMaterialMapping.class.getTypeName();

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());

        wrap.putInt(-1);  // Layered texture

        DummyObject.canSetTexture = true;
        DummyObject dob = new DummyObject(StreamUtil.stream(wrap), scene);

        Assert.assertEquals(scene.getMaterial(0), dob.getMaterial());
        Assert.assertTrue(dob.getMaterialMapping() instanceof DummyMaterialMapping);

    }

    @Test(expected = IOException.class)
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testCreateObjectButTextureMissed() throws IOException {
        Scene scene = new Scene();
        ByteBuffer wrap = ByteBuffer.allocate(200);
        wrap.putShort((short) 1); // Object version;
        wrap.putInt(-1);  // No material
        wrap.putInt(0);  // Take 0'th texture from scene but texture class is missed

        String className = "dummy.dummy.UnknownTextureClass";

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());

        DummyObject.canSetTexture = true;
        new DummyObject(StreamUtil.stream(wrap), scene);
    }

    @Test
    public void testGetObjectAverageValueForEmptyParameterValues() {
        DummyObject dob = new DummyObject();
        Assert.assertArrayEquals(new double[0], dob.getAverageParameterValues(), 0);
    }

    @Test
    public void testSetAndGetObjectAverageValueForEmptyParameterValues() {
        DummyObject dob = new DummyObject();
        ParameterValue[] pv = new ParameterValue[1];
        pv[0] = new ConstantParameterValue(100);

        dob.setParameterValues(pv);
        Assert.assertArrayEquals(new double[]{100}, dob.getAverageParameterValues(), 0);
    }

    @Test
    public void testReadParameterValue() throws IOException {

        ByteBuffer wrap = ByteBuffer.allocate(200);

        String className = ConstantParameterValue.class.getTypeName();

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());
        wrap.putDouble(100); // Value to pass to ConstantParameterValue constructor

        ParameterValue pv = DummyObject.readParameterValue(StreamUtil.stream(wrap));
        Assert.assertNotNull(pv);
        Assert.assertTrue(pv instanceof ConstantParameterValue);
        Assert.assertEquals(100d, pv.getAverageValue(), 0);

    }

    @Test(expected = IOException.class)
    public void testReadParameterValueFromUnknownClass() throws IOException {

        ByteBuffer wrap = ByteBuffer.allocate(200);

        String className = "dummy.dummy.Unknown";

        wrap.putShort(Integer.valueOf(className.length()).shortValue());
        wrap.put(className.getBytes());

        DummyObject.readParameterValue(StreamUtil.stream(wrap));

    }

    @Test
    public void testCopyTextureAndMaterialFromEmptyOne() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();

        target.copyTextureAndMaterial(source);

        Assert.assertNull(target.getTextureMapping());
        Assert.assertNull(target.getMaterial());
        Assert.assertNull(target.getMaterialMapping());
        Assert.assertNull(target.getParameters());
    }

    @Test
    public void testCopyTextureAndMaterialWithExistedTextureNoMapping() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();

        Texture mock = new MockTexture();
        source.setTexture(mock, null);

        target.copyTextureAndMaterial(source);

        Assert.assertNull(target.getTextureMapping());
        Assert.assertNull(target.getTexture());

        Assert.assertNull(target.getMaterial());
        Assert.assertNull(target.getMaterialMapping());

        Assert.assertNotNull(target.getParameters());
        Assert.assertEquals(0, target.getParameters().length);

    }

    @Test
    public void testCopyTextureAndMaterialWithExistedTextureAndMapping() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();

        Texture mock = new MockTexture();
        source.setTexture(mock, new DummyTextureMapping(null, source, mock));

        target.copyTextureAndMaterial(source);

        Assert.assertNotNull(target.getTextureMapping());
        Assert.assertNotNull(target.getTexture());

        Assert.assertNull(target.getMaterial());
        Assert.assertNull(target.getMaterialMapping());

        Assert.assertNotNull(target.getParameters());
        Assert.assertEquals(0, target.getParameters().length);

    }

    @Test
    public void copyTextureAndMaterialWithExistedTextureMappingAndParameters() {
        DummyObject source = new DummyObject();
        DummyObject target = new DummyObject();

        Texture mock = new MockTexture();
        source.setTexture(mock, new DummyTextureMapping(null, source, mock));

        target.copyTextureAndMaterial(source);

        Assert.assertNotNull(target.getTextureMapping());
        Assert.assertNotNull(target.getTexture());

        Assert.assertNull(target.getMaterial());
        Assert.assertNull(target.getMaterialMapping());

        Assert.assertNotNull(target.getParameters());
        Assert.assertEquals(0, target.getParameters().length);

    }

    @Test(expected = NullPointerException.class)
    public void testGetParameterValueFromMissedOne() {
        DummyObject source = new DummyObject();
        source.getParameterValue(new TextureParameter(source, "Dummy", 0, 0, 0));
    }

    @Test
    public void testGetParameterValueFromEmpty() {
        DummyObject source = new DummyObject();
        source.setParameters(new TextureParameter[0]);
        Assert.assertNull(source.getParameterValue(new TextureParameter(source, "Dummy", 0, 0, 0)));
    }

    @Test(expected = NullPointerException.class)
    public void testGetParameterValueFromUnsetValues() {
        DummyObject source = new DummyObject();
        TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
        source.setParameters(new TextureParameter[]{tp});

        Assert.assertNull(source.getParameterValue(tp));

    }

    @Test
    public void testGetParameterValueFromEmptyValues() {
        DummyObject source = new DummyObject();
        TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
        source.setParameters(new TextureParameter[]{tp});
        source.setParameterValues(new ParameterValue[1]);

        Assert.assertNull(source.getParameterValue(tp));

    }

    @Test
    public void testGetParameterValueFromSetValues() {
        DummyObject source = new DummyObject();
        TextureParameter tp = new TextureParameter(source, "Dummy", 0, 0, 0);
        ParameterValue pv = new ConstantParameterValue(100);

        source.setParameters(new TextureParameter[]{tp});
        source.setParameterValues(new ParameterValue[]{pv});

        Assert.assertNotNull(source.getParameterValue(tp));
        Assert.assertEquals(pv, source.getParameterValue(tp));

    }

    public static class MockTexture extends Texture {

        @Override
        public boolean hasComponent(int component) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void getAverageSpec(TextureSpec spec, double time, double[] param) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public TextureMapping getDefaultMapping(Object3D object) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Texture duplicate() {
            return new MockTexture();
        }

        @Override
        public void edit(BFrame fr, Scene sc) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class DummyTextureMapping extends TextureMapping {

        private Object3D object;
        private Texture texture;

        public DummyTextureMapping(DataInputStream in, Object3D obj, Texture texture) {
            this.texture = texture;
            this.object = obj;
        }

        @Override
        public void writeToFile(DataOutputStream out) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void getTransparency(Vec3 pos, RGBColor trans, double angle, double size, double t, double[] param) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double getDisplacement(Vec3 pos, double size, double t, double[] param) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class DummyMaterialMapping extends MaterialMapping {

        public DummyMaterialMapping(Object3D target, Material material) {
            super(target, material);
        }

        public DummyMaterialMapping(DataInputStream in, Object3D target, Material material) {
            super(target, material);
        }

        @Override
        public void writeToFile(DataOutputStream out) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double getStepSize() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void getMaterialSpec(Vec3 pos, MaterialSpec spec, double size, double t) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public MaterialMapping duplicate() {
            return new DummyMaterialMapping(this.getObject(), this.getMaterial());
        }

        @Override
        public MaterialMapping duplicate(Object3D obj, Material mat) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void copy(MaterialMapping map) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Widget getEditingPanel(Object3D obj, MaterialPreviewer preview) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    public static class DummyMaterial extends Material {

        @Override
        public boolean isScattering() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean castsShadows() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public MaterialMapping getDefaultMapping(Object3D obj) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Material duplicate() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void edit(BFrame fr, Scene sc) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private static class DummyObject extends Object3D {

        public static boolean canSetTexture = true;

        public DummyObject() {
        }

        public DummyObject(DataInputStream in, Scene theScene) throws IOException {
            super(in, theScene);
        }

        @Override
        public Object3D duplicate() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void copyObject(Object3D obj) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public BoundingBox getBounds() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setSize(double xsize, double ysize, double zsize) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public WireframeMesh getWireframeMesh() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Keyframe getPoseKeyframe() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void applyPoseKeyframe(Keyframe k) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean canSetTexture() {
            return canSetTexture;
        }

    }
}
