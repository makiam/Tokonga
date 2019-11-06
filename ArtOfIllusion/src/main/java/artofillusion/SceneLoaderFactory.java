/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;

/**
 *
 * @author MaksK
 */
public class SceneLoaderFactory {

    private SceneLoaderFactory() {
    }

    public static SceneLoader getLoader(DataInputStream in) throws IOException {
        short version = in.readShort();
        if (version < 0 || version > 4) { throw new InvalidObjectException(String.format("Unsupported file version %d or file damaged", version)); }
        if(version == 0) return new SceneLoaderV0();
        if(version == 3) return new SceneLoaderV3();
        return new SceneLoaderV4();
    }
}
