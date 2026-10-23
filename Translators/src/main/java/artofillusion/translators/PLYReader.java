/*  Copyright (C) 2026 by Maksim Khramov
    Changes copyright (C) 2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.translators;

import artofillusion.math.Vec3;
import artofillusion.object.TriangleMesh;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reader for Stanford PLY files.
 *
 * <p>Supports both ASCII and binary data in either byte order. Vertex coordinates are located by
 * property name, so files may carry additional per-vertex properties (normals, colors, texture
 * coordinates) and may list them before x/y/z. Faces are fan triangulated: a face with N vertices
 * produces N&minus;2 triangles, so non-triangular polygons are imported instead of rejected. PLY has
 * no hole encoding, therefore a face that encloses a hole is filled by the fan.
 */
@Slf4j
public final class PLYReader {

    private static final String VERTEX_ELEMENT = "vertex";
    private static final String FACE_ELEMENT = "face";
    private static final String FORMAT_ASCII = "ascii";
    private static final String FORMAT_BINARY_LE = "binary_little_endian";
    private static final String FORMAT_BINARY_BE = "binary_big_endian";

    /** A property declared by an element. */
    private static final class Property {
        private final String name;
        private final String type;      // scalar type, or the element type of a list
        private final String countType; // null for scalar properties
        private final int size;         // size in bytes of one value of 'type'

        private Property(String name, String type, String countType) {
            this.name = name;
            this.type = type;
            this.countType = countType;
            this.size = sizeOf(type);
        }

        private boolean isList() {
            return countType != null;
        }
    }

    /** An element (vertex, face, edge, ...) declared by the header. */
    private static final class Element {
        private final String name;
        private final int count;
        private final List<Property> properties = new ArrayList<>();

        private Element(String name, int count) {
            this.name = name;
            this.count = count;
        }

        private boolean hasListProperty() {
            for (Property property : properties) {
                if (property.isList()) {
                    return true;
                }
            }
            return false;
        }
    }

    /** The parsed header of a PLY file. */
    private static final class Header {
        private String format;
        private final Map<String, Element> elements = new LinkedHashMap<>();
    }

    private PLYReader() {
    }

    /**
     * Reads a PLY file.
     *
     * @param filePath path to the .ply file
     * @return a {@link TriangleMesh} holding the vertices and the fan triangulated faces
     * @throws IOException if the file cannot be read or does not hold a supported PLY structure
     */
    public static TriangleMesh read(String filePath) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            Header header = parseHeader(raf);
            Element vertexElement = header.elements.get(VERTEX_ELEMENT);
            Element faceElement = header.elements.get(FACE_ELEMENT);
            if (vertexElement == null) {
                throw new IOException("PLY file has no 'vertex' element.");
            }
            if (faceElement == null) {
                throw new IOException("PLY file has no 'face' element.");
            }

            int indexX = indexOfScalar(vertexElement, "x");
            int indexY = indexOfScalar(vertexElement, "y");
            int indexZ = indexOfScalar(vertexElement, "z");
            if (indexX < 0 || indexY < 0 || indexZ < 0) {
                throw new IOException("Vertex element is missing the x, y or z property.");
            }

            int indexIndices = indexOfFaceIndices(faceElement);
            if (indexIndices < 0) {
                throw new IOException("Face element is missing the 'vertex_indices' list property.");
            }

            return switch (header.format) {
                case FORMAT_ASCII -> readAscii(raf, header, indexX, indexY, indexZ, indexIndices);
                case FORMAT_BINARY_LE -> readBinary(raf, header, indexX, indexY, indexZ, indexIndices, ByteOrder.LITTLE_ENDIAN);
                case FORMAT_BINARY_BE -> readBinary(raf, header, indexX, indexY, indexZ, indexIndices, ByteOrder.BIG_ENDIAN);
                default -> throw new IOException("Unsupported PLY format: '" + header.format + "'.");
            };
        }
    }

    private static int indexOfScalar(Element element, String name) {
        for (int i = 0; i < element.properties.size(); i++) {
            Property property = element.properties.get(i);
            if (!property.isList() && property.name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOfFaceIndices(Element faceElement) {
        int fallback = -1;
        int lastResort = -1;
        for (int i = 0; i < faceElement.properties.size(); i++) {
            Property property = faceElement.properties.get(i);
            if (!property.isList()) {
                continue;
            }
            if (property.name.equals("vertex_indices")) {
                return i;
            }
            if (property.name.equals("indices") && fallback < 0) {
                fallback = i;
            }
            if (property.name.equals("vertex_index") && lastResort < 0) {
                lastResort = i;
            }
        }
        return fallback >= 0 ? fallback : lastResort;
    }

    // ------------------------------------------------------------------------
    // Header parsing
    // ------------------------------------------------------------------------

    private static Header parseHeader(RandomAccessFile raf) throws IOException {
        String line = raf.readLine();
        if (line == null || !line.trim().equals("ply")) {
            throw new IOException("Not a valid PLY file (missing 'ply' header).");
        }

        Header header = new Header();
        Element currentElement = null;
        while ((line = raf.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("end_header")) {
                break;
            }
            String[] tokens = split(line);
            switch (tokens[0]) {
                case "format" -> {
                    if (tokens.length < 3) {
                        throw new IOException("Invalid PLY format line: '" + line + "'.");
                    }
                    header.format = tokens[1];
                    requireSupportedVersion(tokens[2]);
                }
                case "element" -> {
                    if (tokens.length < 3) {
                        throw new IOException("Invalid PLY element line: '" + line + "'.");
                    }
                    if (header.elements.containsKey(tokens[1])) {
                        throw new IOException("PLY header declares the '" + tokens[1] + "' element twice.");
                    }
                    currentElement = new Element(tokens[1], parseElementCount(tokens[2], tokens[1]));
                    header.elements.put(tokens[1], currentElement);
                }
                case "property" -> {
                    if (currentElement == null) {
                        throw new IOException("PLY property declared before any element: '" + line + "'.");
                    }
                    if (tokens.length >= 2 && tokens[1].equals("list")) {
                        if (tokens.length < 5) {
                            throw new IOException("Invalid PLY list property: '" + line + "'.");
                        }
                        currentElement.properties.add(listProperty(tokens[4], tokens[2], tokens[3]));
                    } else {
                        if (tokens.length < 3) {
                            throw new IOException("Invalid PLY property: '" + line + "'.");
                        }
                        currentElement.properties.add(scalarProperty(tokens[2], tokens[1]));
                    }
                }
                case "comment", "obj_info" -> {
                    // Free form text, not part of the structure.
                }
                default -> log.debug("Ignoring unknown PLY header directive: {}", line);
            }
        }
        if (header.format == null) {
            throw new IOException("PLY header has no 'format' line.");
        }
        return header;
    }

    private static void requireSupportedVersion(String version) throws IOException {
        try {
            float value = Float.parseFloat(version);
            if (value != 1.0f) {
                log.debug("Reading PLY version {} as if it were 1.0", version);
            }
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid PLY version: '" + version + "'.", ex);
        }
    }

    private static int parseElementCount(String count, String element) throws IOException {
        try {
            long value = Long.parseLong(count);
            if (value < 0 || value > Integer.MAX_VALUE) {
                throw new IOException("Invalid record count " + count + " for PLY element '" + element + "'.");
            }
            return (int) value;
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid record count '" + count + "' for PLY element '" + element + "'.", ex);
        }
    }

    private static Property scalarProperty(String name, String type) throws IOException {
        requireSupportedType(type, "property '" + name + "'");
        return new Property(name, type, null);
    }

    private static Property listProperty(String name, String countType, String elementType) throws IOException {
        requireIntegerType(countType, "list count of property '" + name + "'");
        requireIntegerType(elementType, "list elements of property '" + name + "'");
        return new Property(name, elementType, countType);
    }

    private static void requireSupportedType(String type, String what) throws IOException {
        if (sizeOf(type) < 0) {
            throw new IOException("Unsupported PLY type '" + type + "' declared for " + what + ".");
        }
    }

    /**
     * List lengths and list entries are read as {@code int}, so only the 8, 16 and 32 bit integer
     * types are accepted. Rejecting the rest at header-parse time keeps the failure inside the
     * {@code IOException} contract instead of surfacing as an unchecked exception mid-read.
     */
    private static void requireIntegerType(String type, String what) throws IOException {
        requireSupportedType(type, what);
        switch (type) {
            case "char", "uchar", "int8", "uint8", "short", "ushort", "int16", "uint16",
                 "int", "uint", "int32", "uint32" -> {
                // supported by readIntValue()
            }
            default -> throw new IOException("Unsupported PLY type '" + type + "' declared for " + what
                    + ": list lengths and indices must be 8, 16 or 32 bit integers.");
        }
    }

    /** Size in bytes of one scalar value, or -1 when the type is unknown. */
    private static int sizeOf(String type) {
        return switch (type) {
            case "char", "uchar", "int8", "uint8" -> 1;
            case "short", "ushort", "int16", "uint16" -> 2;
            case "int", "uint", "int32", "uint32", "float", "float32" -> 4;
            case "double", "float64", "long", "int64", "ulong", "uint64" -> 8;
            default -> -1;
        };
    }

    /**
     * Rejects headers whose declared record counts cannot possibly be backed by the payload, so a
     * crafted {@code element vertex 2000000000} line cannot trigger a multi-gigabyte allocation
     * (which would raise {@link OutOfMemoryError}, an {@code Error} that no importer can catch).
     */
    private static void validateDeclaredCounts(Header header, long payloadBytes, boolean binary) throws IOException {
        long required = 0;
        for (Element element : header.elements.values()) {
            required += (long) element.count * minimumRecordSize(element, binary);
            if (required > payloadBytes) {
                throw new IOException("PLY header declares " + element.count + " records for the '" + element.name
                        + "' element, which cannot fit in the remaining " + payloadBytes + " bytes of data.");
            }
        }
    }

    /** Smallest possible size of one record: one character in ASCII, the declared widths in binary. */
    private static int minimumRecordSize(Element element, boolean binary) {
        if (!binary) {
            return 1;
        }
        int size = 0;
        for (Property property : element.properties) {
            size += property.isList() ? sizeOf(property.countType) : property.size;
        }
        return Math.max(size, 1);
    }

    // ------------------------------------------------------------------------
    // ASCII reading
    // ------------------------------------------------------------------------

    private static TriangleMesh readAscii(RandomAccessFile raf, Header header,
                                          int indexX, int indexY, int indexZ, int indexIndices) throws IOException {
        Element vertexElement = header.elements.get(VERTEX_ELEMENT);
        Element faceElement = header.elements.get(FACE_ELEMENT);
        validateDeclaredCounts(header, raf.length() - raf.getFilePointer(), false);
        Vec3[] vertices = new Vec3[vertexElement.count];
        List<int[]> polygons = new ArrayList<>(faceElement.count);

        for (Element element : header.elements.values()) {
            for (int i = 0; i < element.count; i++) {
                String line = readDataLine(raf);
                if (line == null) {
                    throw new IOException("Unexpected end of file: element '" + element.name + "' declares " + element.count
                            + " records but only " + i + " were found.");
                }
                if (element == vertexElement) {
                    vertices[i] = readAsciiVertex(line, element, indexX, indexY, indexZ);
                } else if (element == faceElement) {
                    polygons.add(readAsciiFace(line, element, indexIndices));
                }
            }
        }
        return buildMesh(vertices, polygons);
    }

    /** Reads the next non-blank data line, or null at end of file. */
    private static String readDataLine(RandomAccessFile raf) throws IOException {
        String line;
        while ((line = raf.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                return line;
            }
        }
        return null;
    }

    private static Vec3 readAsciiVertex(String line, Element element, int indexX, int indexY, int indexZ) throws IOException {
        String[] tokens = split(line);
        double[] coordinates = new double[3];
        int token = 0;
        for (int p = 0; p < element.properties.size(); p++) {
            Property property = element.properties.get(p);
            if (property.isList()) {
                int count = asciiInt(tokens, token, element.name);
                if (count < 0 || count > tokens.length - token - 1) {
                    throw new IOException("Truncated '" + element.name + "' record: list property '"
                            + property.name + "' declares " + count + " values but only "
                            + Math.max(tokens.length - token - 1, 0) + " remain.");
                }
                token += 1 + count;
                continue;
            }
            if (p == indexX) {
                coordinates[0] = asciiDouble(tokens, token, element.name);
            } else if (p == indexY) {
                coordinates[1] = asciiDouble(tokens, token, element.name);
            } else if (p == indexZ) {
                coordinates[2] = asciiDouble(tokens, token, element.name);
            } else if (token >= tokens.length) {
                throw truncatedRecord(element);
            }
            token++;
        }
        return new Vec3(coordinates[0], coordinates[1], coordinates[2]);
    }

    private static int[] readAsciiFace(String line, Element element, int indexIndices) throws IOException {
        String[] tokens = split(line);
        int[] indices = null;
        int token = 0;
        for (int p = 0; p < element.properties.size(); p++) {
            Property property = element.properties.get(p);
            if (!property.isList()) {
                asciiToken(tokens, token, element.name);
                token++;
                continue;
            }
            int count = asciiInt(tokens, token, element.name);
            if (count < 0) {
                throw new IOException("Negative list length " + count + " in '" + element.name + "' record.");
            }
            token++;
            if (count > tokens.length - token) {
                throw new IOException("Truncated '" + element.name + "' record: list property '" + property.name
                        + "' declares " + count + " values but only " + (tokens.length - token) + " remain.");
            }
            if (p == indexIndices) {
                indices = new int[count];
                for (int i = 0; i < count; i++) {
                    indices[i] = asciiInt(tokens, token + i, element.name);
                }
            }
            token += count;
        }
        if (indices == null) {
            throw new IOException("Face record has no vertex indices.");
        }
        return indices;
    }

    private static String[] split(String line) {
        return line.trim().split("\\s+");
    }

    private static String asciiToken(String[] tokens, int index, String element) throws IOException {
        if (index >= tokens.length) {
            throw new IOException("Truncated '" + element + "' record: expected at least " + (index + 1)
                    + " values, found " + tokens.length + ".");
        }
        return tokens[index];
    }

    private static double asciiDouble(String[] tokens, int index, String element) throws IOException {
        String token = asciiToken(tokens, index, element);
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid number '" + token + "' in '" + element + "' record.", ex);
        }
    }

    private static int asciiInt(String[] tokens, int index, String element) throws IOException {
        String token = asciiToken(tokens, index, element);
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid integer '" + token + "' in '" + element + "' record.", ex);
        }
    }

    private static IOException truncatedRecord(Element element) {
        return new IOException("Truncated '" + element.name + "' record: the header declares more properties than the line holds.");
    }

    // ------------------------------------------------------------------------
    // Binary reading
    // ------------------------------------------------------------------------

    private static TriangleMesh readBinary(RandomAccessFile raf, Header header,
                                           int indexX, int indexY, int indexZ, int indexIndices,
                                           ByteOrder order) throws IOException {
        long dataStart = raf.getFilePointer();
        long dataLength = raf.length() - dataStart;
        if (dataLength > Integer.MAX_VALUE) {
            throw new IOException("Binary PLY data is too large: " + dataLength + " bytes.");
        }
        validateDeclaredCounts(header, dataLength, true);
        byte[] data = new byte[(int) dataLength];
        raf.readFully(data);
        ByteBuffer buffer = ByteBuffer.wrap(data).order(order);

        Element vertexElement = header.elements.get(VERTEX_ELEMENT);
        Element faceElement = header.elements.get(FACE_ELEMENT);
        Vec3[] vertices = new Vec3[vertexElement.count];
        List<int[]> polygons = new ArrayList<>(faceElement.count);

        try {
            for (Element element : header.elements.values()) {
                if (element == vertexElement) {
                    readBinaryVertices(buffer, element, vertices, indexX, indexY, indexZ);
                } else if (element == faceElement) {
                    readBinaryFaces(buffer, element, polygons, indexIndices);
                } else {
                    skipBinaryElement(buffer, element);
                }
            }
        } catch (IndexOutOfBoundsException | BufferUnderflowException ex) {
            throw new IOException("Unexpected end of file while reading binary PLY data.", ex);
        }
        return buildMesh(vertices, polygons);
    }

    /**
     * Reads the vertex element. When every property is a scalar the coordinates are fetched at
     * absolute offsets and the whole block is skipped in one step; otherwise the records are
     * walked property by property so a list property cannot desynchronize the buffer.
     */
    private static void readBinaryVertices(ByteBuffer buffer, Element element, Vec3[] vertices,
                                           int indexX, int indexY, int indexZ) throws IOException {
        if (element.hasListProperty()) {
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = readBinaryVertex(buffer, element, indexX, indexY, indexZ);
            }
            return;
        }

        int[] offsets = new int[element.properties.size()];
        int stride = 0;
        for (int p = 0; p < element.properties.size(); p++) {
            offsets[p] = stride;
            stride += element.properties.get(p).size;
        }

        int base = buffer.position();
        Property propertyX = element.properties.get(indexX);
        Property propertyY = element.properties.get(indexY);
        Property propertyZ = element.properties.get(indexZ);
        long required = (long) vertices.length * stride;
        if (buffer.position() + required > buffer.limit()) {
            throw new IOException("Unexpected end of file: " + vertices.length + " vertex records of " + stride
                    + " bytes do not fit in the remaining " + buffer.remaining() + " bytes.");
        }
        for (int i = 0; i < vertices.length; i++) {
            int record = base + i * stride;
            vertices[i] = new Vec3(readScalarAt(buffer, record + offsets[indexX], propertyX.type),
                    readScalarAt(buffer, record + offsets[indexY], propertyY.type),
                    readScalarAt(buffer, record + offsets[indexZ], propertyZ.type));
        }
        buffer.position((int) (base + required));
    }

    private static Vec3 readBinaryVertex(ByteBuffer buffer, Element element,
                                         int indexX, int indexY, int indexZ) throws IOException {
        double[] coordinates = new double[3];
        for (int p = 0; p < element.properties.size(); p++) {
            Property property = element.properties.get(p);
            if (property.isList()) {
                skipBinaryList(buffer, property);
            } else if (p == indexX || p == indexY || p == indexZ) {
                double value = readScalarValue(buffer, property.type);
                if (p == indexX) {
                    coordinates[0] = value;
                } else if (p == indexY) {
                    coordinates[1] = value;
                } else {
                    coordinates[2] = value;
                }
            } else {
                advance(buffer, property.size);
            }
        }
        return new Vec3(coordinates[0], coordinates[1], coordinates[2]);
    }

    private static void readBinaryFaces(ByteBuffer buffer, Element element, List<int[]> polygons,
                                        int indexIndices) throws IOException {
        for (int i = 0; i < element.count; i++) {
            int[] indices = null;
            for (int p = 0; p < element.properties.size(); p++) {
                Property property = element.properties.get(p);
                if (!property.isList()) {
                    advance(buffer, property.size);
                    continue;
                }
                int count = readListLength(buffer, property);
                if (p == indexIndices) {
                    indices = new int[count];
                    for (int k = 0; k < count; k++) {
                        indices[k] = readIntValue(buffer, property.type);
                    }
                } else {
                    advance(buffer, (long) count * property.size);
                }
            }
            if (indices != null) {
                polygons.add(indices);
            }
        }
    }

    private static int readListLength(ByteBuffer buffer, Property property) throws IOException {
        int count = readIntValue(buffer, property.countType);
        if (count < 0 || (long) count * property.size > buffer.remaining()) {
            throw new IOException("Invalid length " + count + " for list property '" + property.name + "'.");
        }
        return count;
    }

    private static void skipBinaryList(ByteBuffer buffer, Property property) throws IOException {
        int count = readListLength(buffer, property);
        advance(buffer, (long) count * property.size);
    }

    /** Skips an element this reader does not use (edge, material, ...) to keep the buffer aligned. */
    private static void skipBinaryElement(ByteBuffer buffer, Element element) throws IOException {
        if (!element.hasListProperty()) {
            int recordSize = 0;
            for (Property property : element.properties) {
                recordSize += property.size;
            }
            advance(buffer, (long) element.count * recordSize);
            return;
        }
        for (int i = 0; i < element.count; i++) {
            for (Property property : element.properties) {
                if (property.isList()) {
                    skipBinaryList(buffer, property);
                } else {
                    advance(buffer, property.size);
                }
            }
        }
    }

    private static void advance(ByteBuffer buffer, long bytes) throws IOException {
        long position = buffer.position() + bytes;
        if (bytes < 0 || position > buffer.limit()) {
            throw new IOException("Unexpected end of file while reading binary PLY data.");
        }
        buffer.position((int) position);
    }

    /** Reads a scalar value at an absolute buffer index, without moving the buffer. */
    private static double readScalarAt(ByteBuffer buffer, int index, String type) {
        return switch (type) {
            case "char", "int8" -> buffer.get(index);
            case "uchar", "uint8" -> buffer.get(index) & 0xFF;
            case "short", "int16" -> buffer.getShort(index);
            case "ushort", "uint16" -> buffer.getShort(index) & 0xFFFF;
            case "int", "int32" -> buffer.getInt(index);
            case "uint", "uint32" -> buffer.getInt(index) & 0xFFFFFFFFL;
            case "long", "int64" -> buffer.getLong(index);
            case "ulong", "uint64" -> buffer.getLong(index);
            case "float", "float32" -> buffer.getFloat(index);
            case "double", "float64" -> buffer.getDouble(index);
            default -> throw new IllegalArgumentException("Unsupported scalar type: " + type);
        };
    }

    private static double readScalarValue(ByteBuffer buffer, String type) throws IOException {
        double value = readScalarAt(buffer, buffer.position(), type);
        advance(buffer, sizeOf(type));
        return value;
    }

    /**
     * Reads a list length or index, honoring the declared integer width. The result is a signed
     * {@code int}, so a {@code uint}/{@code uint32} value at or above 2^31 comes back negative and is
     * rejected by the caller's range check — deliberate, since Java arrays cannot hold that many
     * elements anyway.
     */
    private static int readIntValue(ByteBuffer buffer, String type) throws IOException {
        int index = buffer.position();
        int value = switch (type) {
            case "char", "int8" -> buffer.get(index);
            case "uchar", "uint8" -> buffer.get(index) & 0xFF;
            case "short", "int16" -> buffer.getShort(index);
            case "ushort", "uint16" -> buffer.getShort(index) & 0xFFFF;
            case "int", "int32", "uint", "uint32" -> buffer.getInt(index);
            default -> throw new IllegalArgumentException("Unsupported integer type '" + type + "' for a list length or index.");
        };
        advance(buffer, sizeOf(type));
        return value;
    }

    // ------------------------------------------------------------------------
    // Mesh assembly
    // ------------------------------------------------------------------------

    /** Fan triangulates every polygon and validates the indices before handing them to TriangleMesh. */
    private static TriangleMesh buildMesh(Vec3[] vertices, List<int[]> polygons) throws IOException {
        List<int[]> triangles = new ArrayList<>(polygons.size());
        int triangulated = 0;
        for (int f = 0; f < polygons.size(); f++) {
            int[] polygon = polygons.get(f);
            if (polygon.length < 3) {
                log.debug("Skipping degenerate face {} with {} vertices", f + 1, polygon.length);
                continue;
            }
            for (int index : polygon) {
                if (index < 0 || index >= vertices.length) {
                    throw new IOException("Face " + (f + 1) + " references vertex " + index
                            + ", but the file declares " + vertices.length + " vertices.");
                }
            }
            if (polygon.length > 3) {
                triangulated++;
            }
            for (int i = 1; i < polygon.length - 1; i++) {
                if (polygon[0] == polygon[i] || polygon[i] == polygon[i + 1] || polygon[0] == polygon[i + 1]) {
                    log.debug("Skipping degenerate triangle ({}, {}, {}) of face {}",
                            polygon[0], polygon[i], polygon[i + 1], f + 1);
                    continue;
                }
                triangles.add(new int[] {polygon[0], polygon[i], polygon[i + 1]});
            }
        }
        if (triangulated > 0) {
            log.debug("Fan triangulated {} polygon(s) into {} triangles; concave faces may be filled",
                    triangulated, triangles.size());
        }
        return new TriangleMesh(vertices, triangles.toArray(new int[0][]));
    }
}
