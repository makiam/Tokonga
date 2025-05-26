package artofillusion.animation;

import artofillusion.SceneIOUtil;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class PositionTrackTest {

    @Test
    public void testWriteDefaultToStream() throws IOException {
        String origin = "Likörflasche PM";
        var oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), origin);
        var track = new PositionTrack(oi);
        track.setEnabled(false);

        var bb = StreamUtil.getUTFNameAsByteArray(track.getClass());

        var fb = StreamUtil.writeObjectToStream((target) -> {
            track.writeToStream(target, null);
        });

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        wrap.putInt(1); // Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);

        artofillusion.SceneIOUtil.loadTracksUnbuffered(StreamUtil.stream(wrap), null, oi);

        var restored = oi.getTracks()[0];
        Assertions.assertEquals("Position", restored.getName());
        Assertions.assertEquals(false, restored.isEnabled());


    }
}
