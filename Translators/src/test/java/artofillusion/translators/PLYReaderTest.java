/* Copyright (C) 2026 by Maksim Khramov
 *  Changes copyright (C) 2026 by Alexander Vorobyev

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PLYReaderTest {

    @Test
    void airplane() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
            .getResource("artofillusion/translators/airplane.ply");
        Assertions.assertNotNull(resource, "airplane.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(2452, airplane.getFaceCount());
        Assertions.assertEquals(1335, airplane.getVertices().length);
    }

    @Test
    void airplane_binary() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/airplane_binary.ply");
        Assertions.assertNotNull(resource, "airplane_binary.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(2452, airplane.getFaceCount());
        Assertions.assertEquals(1335, airplane.getVertices().length);
    }

    @Test
    void platonic_cube() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_ascii.ply");
        Assertions.assertNotNull(resource, "cube_ascii.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(12, airplane.getFaceCount());
        Assertions.assertEquals(8, airplane.getVertices().length);
    }

    @Test
    void platonic_cube_binary() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_binary.ply");
        Assertions.assertNotNull(resource, "cube_binary.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(12, airplane.getFaceCount());
        Assertions.assertEquals(8, airplane.getVertices().length);
    }

    @Test
    void platonic_cube_color_prepend() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_color_ascii.ply");
        Assertions.assertNotNull(resource, "cube_color_ascii.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(12, airplane.getFaceCount());
        Assertions.assertEquals(8, airplane.getVertices().length);
    }

    @Test
    void platonic_dodecahedron() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/dodecahedron_ascii.ply");
        Assertions.assertNotNull(resource, "dodecahedron_ascii.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(36, airplane.getFaceCount());
        Assertions.assertEquals(20, airplane.getVertices().length);
    }

    @Test
    void polygon_WithHole() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/face-with-hole.ply");
        Assertions.assertNotNull(resource, "face-with-hole.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(6, airplane.getFaceCount());
        Assertions.assertEquals(8, airplane.getVertices().length);
    }
}
