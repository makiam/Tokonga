/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.util.logging.Level;

/**
 *
 * @author MaksK
 */
public class SceneLoaderV3 extends SceneLoaderV0 {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    @Override
    protected void loadImages() throws IOException {
        try {
            Class<?> clazz = ArtOfIllusion.getClass(stream.readUTF());
        } catch (ClassNotFoundException ex) {
            logger.at(Level.SEVERE).withCause(ex).log("Error loading image: %s", ex.getMessage());
        }
    }


    
}
