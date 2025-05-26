package artofillusion.animation;

import artofillusion.LayoutWindow;
import artofillusion.Scene;
import artofillusion.SceneIOUtil;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.ReadBypassEventListener;
import artofillusion.test.util.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, oi.getTracks().length);
        Track re = oi.getTracks()[0];

    }


    @Test
    @DisplayName("Test Restore Track buffered")
    void testRestoreTwoTracksBuffered() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new PoseTrack(oi);

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, track, null, true);
        });

        wrap.putInt(2); // Tracks counter
        wrap.put(fb, 0, fb.length);
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(2, oi.getTracks().length);
        Track re = oi.getTracks()[0];
        Assertions.assertEquals(oi, re.getParent());
    }

    @Test
    @DisplayName("Test Restore Track without appropriate constructor buffered")
    void testRestoreBadConstructorTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new DummyTrack();

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, track, null, true);
        });

        wrap.putInt(1); // Tracks counter
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, listener.getCounter());

    }



    @Test
    @DisplayName("Test Restore Track cannot be found")
    void testRestoreNotFoundTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new BadConstructorTrack();

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, track, null, true);
        });

        wrap.putInt(1); // Tracks counter
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, listener.getCounter());
    }


    @Test
    @DisplayName("Test Restore Track More")
    void testRestoreGoodTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new GoodTrack(oi);

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, track, null, true);
        });

        wrap.putInt(1); // Tracks counter
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(0, listener.getCounter());
    }

    @Test
    public void testRestoreMissedTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track track = new GoodTrack(oi);

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var bb = StreamUtil.getUTFNameAsByteArray(track.getClass() + "IAmMissed");
        var fb = StreamUtil.writeObjectToStream((target) -> {
            track.writeToStream(target, null);
        });

        wrap.putInt(1); //Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, listener.getCounter());
    }

    @Test
    public void testRestoreGoodAndMissedTrack() throws IOException {
        Object3D obj = new Cube(1, 1, 1);
        ObjectInfo oi = new ObjectInfo(obj, new CoordinateSystem(), "Cube");
        Track one = new GoodTrack(oi);

        ByteBuffer wrap = ByteBuffer.allocate(10000);

        var bb = StreamUtil.getUTFNameAsByteArray(one.getClass() + "IAmMissed");
        var fb = StreamUtil.writeObjectToStream((target) -> {
            one.writeToStream(target, null);
        });

        wrap.putInt(2); //Tracks counter
        wrap.put(bb, 0, bb.length);
        wrap.putInt(fb.length);
        wrap.put(fb, 0, fb.length);

        Track two = new GoodTrack(oi);
        fb = StreamUtil.writeObjectToStream((target) -> {
            artofillusion.SceneIOUtil.writeTrack(target, two, null, true);
        });
        wrap.put(fb, 0, fb.length);

        SceneIOUtil.loadTracksBuffered(StreamUtil.stream(wrap), null, oi);
        Assertions.assertEquals(1, listener.getCounter());
        Assertions.assertEquals(1, oi.getTracks().length);
        Assertions.assertInstanceOf(GoodTrack.class, oi.getTracks()[0]);
    }



    public static class GoodTrack extends Track {
        public GoodTrack(ObjectInfo info) {

        }

        @Override
        public void edit(LayoutWindow win) {

        }

        @Override
        public void apply(double time) {

        }

        @Override
        public Track<?> duplicate(Object parent) {
            return null;
        }

        @Override
        public double[] getKeyTimes() {
            return new double[0];
        }

        @Override
        public int moveKeyframe(int which, double time) {
            return 0;
        }

        @Override
        public void deleteKeyframe(int which) {

        }

        @Override
        public boolean isNullTrack() {
            return false;
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene scene) throws IOException {

        }

        @Override
        public void initFromStream(DataInputStream in, Scene scene) throws IOException {

        }

        @Override
        public void copy(Track source) {

        }
    }

    public static class BadConstructorTrack extends Track {

        @Override
        public void edit(LayoutWindow win) {

        }

        @Override
        public void apply(double time) {

        }

        @Override
        public Track<?> duplicate(Object parent) {
            return null;
        }

        @Override
        public double[] getKeyTimes() {
            return new double[0];
        }

        @Override
        public int moveKeyframe(int which, double time) {
            return 0;
        }

        @Override
        public void deleteKeyframe(int which) {

        }

        @Override
        public boolean isNullTrack() {
            return false;
        }

        @Override
        public void writeToStream(DataOutputStream out, Scene scene) throws IOException {

        }

        @Override
        public void initFromStream(DataInputStream in, Scene scene) throws IOException {

        }

        @Override
        public void copy(Track source) {

        }
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
