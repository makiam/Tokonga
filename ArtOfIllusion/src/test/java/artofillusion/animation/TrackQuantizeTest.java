package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TrackQuantizeTest {

    @ParameterizedTest
    @MethodSource("getTracks")
    public void testTrackQuantized(Track<?> test) {
        Assertions.assertTrue(test.isQuantized());
        var dup = test.duplicate(test.getParent());
        Assertions.assertTrue(dup.isQuantized());
    }

    static Stream<Track<?>> getTracks() {
        List<Track<?>> tracks = new ArrayList<>();

        ObjectInfo oi = new ObjectInfo(new Cube(1, 1, 1), new CoordinateSystem(), "Cube");
        tracks.add(new PositionTrack(oi));
        tracks.add(new ConstraintTrack(oi));
        tracks.add(new PoseTrack(oi));
        tracks.add(new WeightTrack(new DummyTrack()));
        return tracks.stream();
    }


}
