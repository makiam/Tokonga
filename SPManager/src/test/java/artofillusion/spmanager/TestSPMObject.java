/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package artofillusion.spmanager;

import artofillusion.ArtOfIllusion;
import org.junit.jupiter.api.Test;


import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 *
 * @author MaksK
 */
public class TestSPMObject {
    @Test
    public void testObject() throws URISyntaxException {
        var path = Paths.get(TestSPMObject.class.getResource("/artofillusion/Tools.jar").toURI());


        var so = new SPMObjectInfo(path.toFile().toString());
        //System.out.println(so);
    }
}
