package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.ReadBypassEventListener;
import artofillusion.test.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class TrackRestoreTest {
    private static ReadBypassEventListener listener;

    @BeforeAll
    public static void setupClass() {
        listener = new ReadBypassEventListener();
    }

    @BeforeEach
    void resetCounterBefore() {
        listener.reset();
    }

    @Test
    void testRestoreTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new PoseTrack(oi);


        Assertions.assertEquals(0, oi.getTracks().length);

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        var bb = StreamUtil.getUTFNameAsByteArray(track.getClass());

        var fb = StreamUtil.writeObjectToStream((target) -> {
            track.writeToStream(target, null);
        });
        wrap.putInt(1); // Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);

        artofillusion.SceneIOUtil.loadTracksUnbuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, oi.getTracks().length);

    }

    @Test
    @DisplayName("Test Restore Track buffered")
    void testRestoreTrackBuffered() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new PoseTrack(oi);

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, track, null, true);
        });

        wrap.putInt(1); // Tracks counter
        wrap.put(fb, 0, fb.length);

    }


    @Test
    @DisplayName("Test Restore Track when class not found")
    void testRestoreTrackBadClassName() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new PoseTrack(oi);


        Assertions.assertEquals(0, oi.getTracks().length);

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        var bb = StreamUtil.getUTFNameAsByteArray(track.getClass() + "Bad");

        var fb = StreamUtil.writeObjectToStream((target) -> {
            track.writeToStream(target, null);
        });
        wrap.putInt(1); // Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);


        Assertions.assertThrows(IOException.class, () -> {
            artofillusion.SceneIOUtil.loadTracksUnbuffered(StreamUtil.stream(wrap), null, oi);
        });

    }

    @Test
    @DisplayName("Test Restore Track when No expected constructor")
    void testRestoreTrackMissedExpectedTrackConstructor() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new DummyTrack();


        Assertions.assertEquals(0, oi.getTracks().length);

        ByteBuffer wrap = ByteBuffer.allocate(10000);
        var bb = StreamUtil.getUTFNameAsByteArray(track.getClass());

        var fb = StreamUtil.writeObjectToStream((target) -> {
            track.writeToStream(target, null);
        });
        wrap.putInt(1); // Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);


        Assertions.assertThrows(IOException.class, () -> {
            artofillusion.SceneIOUtil.loadTracksUnbuffered(StreamUtil.stream(wrap), null, oi);
        });

    }
}
