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
    public void testWriteDefaultToStreamUnbuffered() throws IOException {
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

    @Test
    public void testWriteOneToStreamBuffered() throws IOException {
        String origin = "Likörflasche PM";
        var oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), origin);
        var track0 = new PositionTrack(oi);
        track0.setEnabled(false);

        var classBuffer = StreamUtil.getUTFNameAsByteArray(track0.getClass());

        var trackBuffer = StreamUtil.writeObjectToStream((target) -> {
            track0.writeToStream(target, null);
        });

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        wrap.putInt(1); // Tracks counter
        wrap.put(classBuffer, 0, classBuffer.length);

        wrap.putInt(trackBuffer.length);
        wrap.put(trackBuffer, 0, trackBuffer.length);


        artofillusion.SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);

        var restored = oi.getTracks()[0];
        Assertions.assertEquals("Position", restored.getName());
        Assertions.assertEquals(false, restored.isEnabled());


    }

    @Test
    public void testWriteTwoToStreamBuffered() throws IOException {
        String origin = "Likörflasche PM";
        var oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), origin);
        var track0 = new PositionTrack(oi);
        track0.setEnabled(false);
        track0.setName("Likörflasche PM " + track0.getName());
        var classBuffer = StreamUtil.getUTFNameAsByteArray(track0.getClass());

        var trackBuffer = StreamUtil.writeObjectToStream((target) -> {
            track0.writeToStream(target, null);
        });

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        wrap.putInt(2); // Tracks counter
        wrap.put(classBuffer, 0, classBuffer.length);

        wrap.putInt(trackBuffer.length);
        wrap.put(trackBuffer, 0, trackBuffer.length);


        var track1 = new RotationTrack(oi);
        track1.setName("Likörflasche PM " + track1.getName());
        track0.setEnabled(true);

        classBuffer = StreamUtil.getUTFNameAsByteArray(track1.getClass());
        trackBuffer = StreamUtil.writeObjectToStream((target) -> {
            track1.writeToStream(target, null);
        });

        wrap.put(classBuffer, 0, classBuffer.length);

        wrap.putInt(trackBuffer.length);
        wrap.put(trackBuffer, 0, trackBuffer.length);


        artofillusion.SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);

        Assertions.assertEquals(2, oi.getTracks().length);
        var restored = oi.getTracks()[1];
        Assertions.assertEquals("Likörflasche PM " + "Rotation", restored.getName());
        Assertions.assertEquals(true, restored.isEnabled());


    }
}
