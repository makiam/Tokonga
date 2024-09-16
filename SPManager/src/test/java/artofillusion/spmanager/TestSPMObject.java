/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package artofillusion.spmanager;

import artofillusion.ArtOfIllusion;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

/**
 *
 * @author MaksK
 */
public class TestSPMObject {
    @Test
    public void testObject() {

        var so = new SPMObjectInfo(Paths.get(ArtOfIllusion.PLUGIN_DIRECTORY, "Tools.jar").toString());
        System.out.println(so);
    }
}
