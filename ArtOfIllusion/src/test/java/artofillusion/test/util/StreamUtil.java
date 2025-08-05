/* Copyright (C) 2020 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.test.util;

import artofillusion.SceneIO;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.ByteBuffer;

/**
 *
 * @author maksim.khramov
 */
public class StreamUtil {

    public static DataInputStream stream(ByteBuffer wrap) {
        return new DataInputStream(new ByteArrayInputStream(wrap.array()));
    }

    @SneakyThrows
    public static byte[] getUTFNameAsByteArray(String name) {
        ByteArrayOutputStream nameStream = new ByteArrayOutputStream();
        new DataOutputStream(nameStream).writeUTF(name);
        return nameStream.toByteArray();
    }

    public static byte[] getUTFNameAsByteArray(Class<?> clazz) {
        return getUTFNameAsByteArray(clazz.getName());
    }

    public static byte[] writeObjectToStream(SceneIO.DataWriteProvider target) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        target.write(new DataOutputStream(bos));
        return bos.toByteArray();
    }


}
