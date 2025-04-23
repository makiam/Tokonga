/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */


package artofillusion.image.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


class BlurFilterTest {

    @Test
    void writeToStreamPi() throws IOException {
        BlurFilter filter = new BlurFilter();
        filter.setPropertyValue(0, Double.valueOf(Math.PI));
        var bos = new ByteArrayOutputStream();
        filter.writeToStream(new DataOutputStream(bos), null);
        Assertions.assertEquals("[64, 9, 33, -5, 84, 68, 45, 24]", Arrays.toString(bos.toByteArray()));
        Assertions.assertEquals(Math.PI, ByteBuffer.wrap(bos.toByteArray()).getDouble());
    }

    @Test
    void writeToStreamDefault() throws IOException {
        BlurFilter filter = new BlurFilter();
        var bos = new ByteArrayOutputStream();
        filter.writeToStream(new DataOutputStream(bos), null);
        Assertions.assertEquals(0.05, ByteBuffer.wrap(bos.toByteArray()).getDouble());
    }

    @Test
    void initFromStream() {
    }
}
