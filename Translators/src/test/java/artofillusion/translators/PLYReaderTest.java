/*  Copyright (C) 2026 by Maksim Khramov
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        var cube = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(cube, "PLYReader should return non-null object");
        Assertions.assertEquals(12, cube.getFaceCount());
        Assertions.assertEquals(8, cube.getVertices().length);
    }

    @Test
    void platonic_cube_binary() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_binary.ply");
        Assertions.assertNotNull(resource, "cube_binary.ply not found in classpath");
        var cube = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(cube, "PLYReader should return non-null object");
        Assertions.assertEquals(12, cube.getFaceCount());
        Assertions.assertEquals(8, cube.getVertices().length);
    }

    @Test
    void platonic_cube_color_prepend() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_color_ascii.ply");
        Assertions.assertNotNull(resource, "cube_color_ascii.ply not found in classpath");
        var cube = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(cube, "PLYReader should return non-null object");
        Assertions.assertEquals(12, cube.getFaceCount());
        Assertions.assertEquals(8, cube.getVertices().length);
    }

    @Test
    void platonic_dodecahedron() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/dodecahedron_ascii.ply");
        Assertions.assertNotNull(resource, "dodecahedron_ascii.ply not found in classpath");
        var dodecahedron = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(dodecahedron, "PLYReader should return non-null object");
        Assertions.assertEquals(36, dodecahedron.getFaceCount());
        Assertions.assertEquals(20, dodecahedron.getVertices().length);
    }

    @Test
    void polygon_WithHole() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/face-with-hole.ply");
        Assertions.assertNotNull(resource, "face-with-hole.ply not found in classpath");
        var polygonWithHole = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(polygonWithHole, "PLYReader should return non-null object");
        Assertions.assertEquals(6, polygonWithHole.getFaceCount());
        Assertions.assertEquals(8, polygonWithHole.getVertices().length);
    }

    @Test
    void icosahedron() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/icosahedron.ply");
        Assertions.assertNotNull(resource, "icosahedron.ply not found in classpath");
        var icosahedron = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(icosahedron, "PLYReader should return non-null object");
        Assertions.assertEquals(20, icosahedron.getFaceCount());
        Assertions.assertEquals(12, icosahedron.getVertices().length);
    }

    @Test
    void cube_binary_big_endian() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/cube_binary_big_endian.ply");
        Assertions.assertNotNull(resource, "cube_binary_big_endian.ply not found in classpath");
        var cube = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(cube, "PLYReader should return non-null object");
        Assertions.assertEquals(12, cube.getFaceCount());
        Assertions.assertEquals(8, cube.getVertices().length);
        // counts survive a wrong byte order, coordinates do not: pin the decoded values
        Assertions.assertEquals(1.0, cube.getVertices()[7].r.x, 0.0);
        Assertions.assertEquals(1.0, cube.getVertices()[7].r.y, 0.0);
        Assertions.assertEquals(1.0, cube.getVertices()[7].r.z, 0.0);
        Assertions.assertEquals(0.0, cube.getVertices()[0].r.x, 0.0);
        Assertions.assertEquals(0.0, cube.getVertices()[0].r.y, 0.0);
        Assertions.assertEquals(0.0, cube.getVertices()[0].r.z, 0.0);
        Assertions.assertEquals(1.0, cube.getVertices()[3].r.y, 0.0);
        Assertions.assertEquals(0.0, cube.getVertices()[3].r.z, 0.0);
    }

    @Test
    void colored_faces() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/tet.ply");
        Assertions.assertNotNull(resource, "tet.ply not found in classpath");
        var tet = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(tet, "PLYReader should return non-null object");
        Assertions.assertEquals(4, tet.getFaceCount());
        Assertions.assertEquals(4, tet.getVertices().length);
    }

    @Test
    @DisplayName("PLYReader should read a 32-bit list declaration")
    void plane_with_uint32_list_declaration() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/airplane_2.ply");
        Assertions.assertNotNull(resource, "airplane_2.ply not found in classpath");
        var airplane = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(airplane, "PLYReader should return non-null object");
        Assertions.assertEquals(2452, airplane.getFaceCount());
        Assertions.assertEquals(1335, airplane.getVertices().length);
    }

    @Test
    @DisplayName("PLYReader should skip unused elements and honour a 32-bit list count")
    void quad_binary_extras() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/quad_binary_extras.ply");
        Assertions.assertNotNull(resource, "quad_binary_extras.ply not found in classpath");
        var quad = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(quad, "PLYReader should return non-null object");
        Assertions.assertEquals(2, quad.getFaceCount());
        Assertions.assertEquals(4, quad.getVertices().length);
        // double positions must keep full precision, not be truncated to float
        Assertions.assertEquals(0.1, quad.getVertices()[2].r.y, 1.0e-12);
    }

    @Test
    @DisplayName("PLYReader should read 64-bit positions and a list property in the vertex element")
    void tet_binary_mixed() throws IOException, URISyntaxException {
        var resource = getClass().getClassLoader()
                .getResource("artofillusion/translators/tet_binary_mixed.ply");
        Assertions.assertNotNull(resource, "tet_binary_mixed.ply not found in classpath");
        var tet = PLYReader.read(Paths.get(resource.toURI()).toString());
        Assertions.assertNotNull(tet, "PLYReader should return non-null object");
        Assertions.assertEquals(4, tet.getFaceCount());
        Assertions.assertEquals(4, tet.getVertices().length);
        Assertions.assertEquals(1.0, tet.getVertices()[3].r.x, 1.0e-12);
        Assertions.assertEquals(1.0, tet.getVertices()[3].r.y, 1.0e-12);
        Assertions.assertEquals(0.0, tet.getVertices()[3].r.z, 1.0e-12);
    }

    // ------------------------------------------------------------------------
    // Negative paths: every deliberate rejection should surface as IOException
    // ------------------------------------------------------------------------

    @TempDir
    Path tempDir;

    private String writeAscii(String name, String... headerAndData) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, String.join("\n", headerAndData).getBytes(StandardCharsets.US_ASCII));
        return file.toString();
    }

    @Test
    @DisplayName("PLYReader should reject a header that declares more records than the payload can hold")
    void rejectsOversizedDeclaredCount() throws IOException {
        String file = writeAscii("huge.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 2000000000",
                "property float x",
                "property float y",
                "property float z",
                "element face 0",
                "property list uchar int vertex_indices",
                "end_header");
        Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
    }

    @Test
    @DisplayName("PLYReader should reject a non-integer list count type at header parse time")
    void rejectsNonIntegerListType() throws IOException {
        String file = writeAscii("floatlist.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 3",
                "property float x",
                "property float y",
                "property float z",
                "element face 1",
                "property list float int vertex_indices",
                "end_header",
                "0 0 0", "1 0 0", "0 1 0",
                "3 0 1 2");
        IOException error = Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
        Assertions.assertTrue(error.getMessage().contains("must be 8, 16 or 32 bit integers"), error.getMessage());
    }

    @Test
    @DisplayName("PLYReader should reject an unsupported property type")
    void rejectsUnknownPropertyType() throws IOException {
        String file = writeAscii("float16.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 1",
                "property float16 x",
                "property float16 y",
                "property float16 z",
                "element face 0",
                "property list uchar int vertex_indices",
                "end_header",
                "0 0 0");
        Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
    }

    @Test
    @DisplayName("PLYReader should reject a duplicated element declaration")
    void rejectsDuplicateElement() throws IOException {
        String file = writeAscii("duplicate.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 1",
                "property float x",
                "property float y",
                "property float z",
                "element vertex 1",
                "property float x",
                "element face 0",
                "property list uchar int vertex_indices",
                "end_header",
                "0 0 0");
        IOException error = Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
        Assertions.assertTrue(error.getMessage().contains("twice"), error.getMessage());
    }

    @Test
    @DisplayName("PLYReader should report a missing face element")
    void rejectsMissingFaceElement() throws IOException {
        String file = writeAscii("points.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 1",
                "property float x",
                "property float y",
                "property float z",
                "end_header",
                "0 0 0");
        IOException error = Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
        Assertions.assertTrue(error.getMessage().contains("face"), error.getMessage());
    }

    @Test
    @DisplayName("PLYReader should report a face index outside the vertex range")
    void rejectsOutOfRangeFaceIndex() throws IOException {
        String file = writeAscii("badindex.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 3",
                "property float x",
                "property float y",
                "property float z",
                "element face 1",
                "property list uchar int vertex_indices",
                "end_header",
                "0 0 0", "1 0 0", "0 1 0",
                "3 0 1 5");
        IOException error = Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
        Assertions.assertTrue(error.getMessage().contains("references vertex 5"), error.getMessage());
    }

    @Test
    @DisplayName("PLYReader should report a truncated ASCII record")
    void rejectsTruncatedAsciiRecord() throws IOException {
        String file = writeAscii("short.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 1",
                "property float x",
                "property float y",
                "property float z",
                "element face 1",
                "property list uchar int vertex_indices",
                "end_header",
                "0 0",
                "0");
        Assertions.assertThrows(IOException.class, () -> PLYReader.read(file));
    }

    @Test
    @DisplayName("PLYReader should report truncated binary vertex data")
    void rejectsTruncatedBinaryPayload() throws IOException {
        Path file = tempDir.resolve("truncated.ply");
        String header = """
                ply
                format binary_little_endian 1.0
                element vertex 2
                property float x
                property float y
                property float z
                element face 0
                property list uchar int vertex_indices
                end_header
                """;
        Files.write(file, header.getBytes(StandardCharsets.US_ASCII));
        Assertions.assertThrows(IOException.class, () -> PLYReader.read(file.toString()));
    }

    @Test
    @DisplayName("PLYReader should report a negative binary list length")
    void rejectsNegativeListLength() throws IOException {
        Path file = tempDir.resolve("negativelist.ply");
        String header = "ply\n"
                + "format binary_little_endian 1.0\n"
                + "element vertex 1\n"
                + "property float x\n"
                + "property float y\n"
                + "property float z\n"
                + "element face 1\n"
                + "property list char int vertex_indices\n"
                + "end_header\n";
        ByteBuffer payload = ByteBuffer.allocate(12 + 1).order(ByteOrder.LITTLE_ENDIAN);
        payload.putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).put((byte) -1);
        byte[] bytes = new byte[header.length() + payload.position()];
        System.arraycopy(header.getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, header.length());
        System.arraycopy(payload.array(), 0, bytes, header.length(), payload.position());
        Files.write(file, bytes);
        Assertions.assertThrows(IOException.class, () -> PLYReader.read(file.toString()));
    }

    @Test
    @DisplayName("PLYReader should accept the singular vertex_index spelling")
    void acceptsVertexIndexAlias() throws IOException {
        String file = writeAscii("singular.ply",
                "ply",
                "format ascii 1.0",
                "element vertex 3",
                "property float x",
                "property float y",
                "property float z",
                "element face 1",
                "property list uchar int vertex_index",
                "end_header",
                "0 0 0", "1 0 0", "0 1 0",
                "3 0 1 2");
        var mesh = PLYReader.read(file);
        Assertions.assertEquals(1, mesh.getFaceCount());
    }
}
