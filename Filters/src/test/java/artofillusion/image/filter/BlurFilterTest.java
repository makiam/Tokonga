package artofillusion.image.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
