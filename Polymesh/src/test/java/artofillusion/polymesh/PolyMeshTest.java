package artofillusion.polymesh;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


class PolyMeshTest {

    @Test
    void testInstantiate() throws IOException {
        ByteBuffer wrap = ByteBuffer.allocate(200);
        var dis = new DataInputStream(stream(wrap));
        var pm = new PolyMesh(dis);
    }

    public static DataInputStream stream(ByteBuffer wrap) {
        return new DataInputStream(new ByteArrayInputStream(wrap.array()));
    }
}