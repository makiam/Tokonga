/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.image.filter;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@DisplayName("Read saturation filter data")
public class SaturationFilterReadTest {

    @Test
    void testReadFromStream() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(20);
        wrap.putDouble(Math.PI);
        SaturationFilter sf = new SaturationFilter();
        sf.initFromStream(SaturationFilterReadTest.stream(wrap), null);

        float pv = ((Double)sf.getPropertyValue(0)).floatValue();
        Assertions.assertEquals(Double.valueOf(Math.PI).floatValue(), pv);



    }
    /*
    TODO: Reuse StreamUtil from AOI core?
     */
    public static DataInputStream stream(ByteBuffer wrap) {
        return new DataInputStream(new ByteArrayInputStream(wrap.array()));
    }
}
