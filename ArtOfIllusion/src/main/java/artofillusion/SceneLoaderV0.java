/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import artofillusion.image.MIPMappedImage;
import com.google.common.flogger.FluentLogger;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author MaksK
 */
public class SceneLoaderV0 implements SceneLoader {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    protected Scene scene;
    protected DataInputStream stream;
    
    @Override
    public void load(DataInputStream in, Scene scene) {
        this.scene = scene;
        this.stream = in;
        
    }
    
    protected void loadImages() throws IOException {
        int counter = stream.readInt();
        for (int i = 0; i < counter; i++) {
            scene.addImage(new MIPMappedImage(stream, (short) 0));
        }
    }
    
    protected void loadTextures() throws IOException {
        int counter = stream.readInt();
    }
}
