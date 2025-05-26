package artofillusion.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StreamUtilTest {

    @Test
    void testCompareBAonEmptyString() {
        String origin = "";

        var tmp = origin.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(tmp.length + 2);
        bb.putShort((short) tmp.length);
        bb.put(tmp);

        var ba2 = StreamUtil.getUTFNameAsByteArray(origin);

        Assertions.assertArrayEquals(bb.array(), ba2);
    }

    @Test
    void testCompareBAonString() {
        String origin = "Hello World";

        var tmp = origin.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(tmp.length + 2);
        bb.putShort((short) tmp.length);
        bb.put(tmp);

        var ba2 = StreamUtil.getUTFNameAsByteArray(origin);

        Assertions.assertArrayEquals(bb.array(), ba2);
    }

    @Test
    void testCompareBAonString2() {
        String origin = "Likörflasche PM";

        var tmp = origin.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(tmp.length + 2);
        bb.putShort((short) tmp.length);
        bb.put(tmp);

        var ba2 = StreamUtil.getUTFNameAsByteArray(origin);

        Assertions.assertArrayEquals(bb.array(), ba2);
    }

}
