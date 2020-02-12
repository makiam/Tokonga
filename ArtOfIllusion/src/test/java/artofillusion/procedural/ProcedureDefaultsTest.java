/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.procedural;

import artofillusion.animation.ProceduralPositionTrack;
import artofillusion.animation.ProceduralRotationTrack;
import artofillusion.animation.distortion.CustomDistortionTrack;
import artofillusion.material.ProceduralMaterial3D;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import artofillusion.object.ProceduralDirectionalLight;
import artofillusion.object.ProceduralPointLight;
import artofillusion.object.ProceduralPointLight.LightProcedureOwner;
import artofillusion.texture.ProceduralTexture2D;
import artofillusion.texture.ProceduralTexture3D;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author maksim.khramov
 */
public class ProcedureDefaultsTest {
    
    private static ObjectInfo info;
    
    @BeforeClass
    public static void setUpClass() {
        info = new ObjectInfo(new Cube(1,1,1), new CoordinateSystem(), "Test");
    }
    
    @Test
    public void testProceduralRotationTrackAllowParameters() {
        ProceduralRotationTrack test = new ProceduralRotationTrack(info);
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralRotationTrackAllowViewAngle() {
        ProceduralRotationTrack test = new ProceduralRotationTrack(info);
        Assert.assertFalse("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testProceduralRotationTrackCanEditName() {
        ProceduralRotationTrack test = new ProceduralRotationTrack(info);
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }
  
          
    @Test
    public void testProceduralPositionTrackAllowParameters() {
        ProceduralPositionTrack test = new ProceduralPositionTrack(info);
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralPositionTrackAllowViewAngle() {
        ProceduralPositionTrack test = new ProceduralPositionTrack(info);
        Assert.assertFalse("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testProceduralPositionTrackCanEditName() {
        ProceduralPositionTrack test = new ProceduralPositionTrack(info);
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }
    
    
    @Test
    public void testCustomDistortionTrackAllowParameters() {
        CustomDistortionTrack test = new CustomDistortionTrack(info);
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testCustomDistortionTrackAllowViewAngle() {
        CustomDistortionTrack test = new CustomDistortionTrack(info);
        Assert.assertTrue("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testCustomDistortionTrackCanEditName() {
        CustomDistortionTrack test = new CustomDistortionTrack(info);
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }
    
    @Test
    public void testProceduralMaterial3DAllowParameters() {
        ProceduralMaterial3D test = new ProceduralMaterial3D();
        Assert.assertFalse("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralMaterial3DAllowViewAngle() {
        ProceduralMaterial3D test = new ProceduralMaterial3D();
        Assert.assertFalse("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testProceduralMaterial3DCanEditName() {
        ProceduralMaterial3D test = new ProceduralMaterial3D();
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }
    
    
    @Test
    public void testProceduralTexture2DAllowParameters() {
        ProceduralTexture2D test = new ProceduralTexture2D();
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralTexture2DAllowViewAngle() {
        ProceduralTexture2D test = new ProceduralTexture2D();
        Assert.assertTrue("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testProceduralTexture2DCanEditName() {
        ProceduralTexture2D test = new ProceduralTexture2D();
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }

    @Test
    public void testProceduralTexture3DAllowParameters() {
        ProceduralTexture3D test = new ProceduralTexture3D();
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralTexture3DAllowViewAngle() {
        ProceduralTexture3D test = new ProceduralTexture3D();
        Assert.assertTrue("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }
    
    @Test
    public void testProceduralTexture3DCanEditName() {
        ProceduralTexture3D test = new ProceduralTexture3D();
        Assert.assertTrue("Unexpected result of canEditName...", test.canEditName());
    }

    @Test
    public void testProceduralPointLightLightOwnerAllowParameters() {
        ProceduralPointLight ppl = new ProceduralPointLight(0);        
        ProceduralPointLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralPointLightLightOwnerAllowViewAngle() {
        ProceduralPointLight ppl = new ProceduralPointLight(0);        
        ProceduralPointLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertFalse("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }

    @Test
    public void testProceduralPointLightLightOwnerCanEditName() {
        ProceduralPointLight ppl = new ProceduralPointLight(0);        
        ProceduralPointLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertFalse("Unexpected result of canEditName...", test.canEditName());
    }
    
    @Test
    public void testProceduralDirectionalLightLightOwnerAllowParameters() {
        ProceduralDirectionalLight ppl = new ProceduralDirectionalLight(0);        
        ProceduralDirectionalLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertTrue("Unexpected result of allowParameters...", test.allowParameters());
    }
    
    @Test
    public void testProceduralDirectionalLightLightOwnerAllowViewAngle() {
        ProceduralDirectionalLight ppl = new ProceduralDirectionalLight(0);        
        ProceduralDirectionalLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertFalse("Unexpected result of allowViewAngle...", test.allowViewAngle());
    }

    @Test
    public void testProceduralDirectionalLightLightOwnerCanEditName() {
        ProceduralDirectionalLight ppl = new ProceduralDirectionalLight(0);        
        ProceduralDirectionalLight.LightProcedureOwner test = ppl.new LightProcedureOwner(info, null);
        Assert.assertFalse("Unexpected result of canEditName...", test.canEditName());
    }
}
