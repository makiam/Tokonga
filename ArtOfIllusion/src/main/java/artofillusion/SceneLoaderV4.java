/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import java.io.DataInputStream;

/**
 *
 * @author MaksK
 */
public class SceneLoaderV4 extends SceneLoaderV3 {

    @Override
    public void load(DataInputStream in, Scene scene) {
        super.load(in, scene);
        loadMetaData();
    }
    
    
    protected void loadMetaData() {
        
    }
}
