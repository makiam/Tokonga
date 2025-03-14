package artofillusion.animation;

import artofillusion.math.CoordinateSystem;
import artofillusion.object.NullObject;
import artofillusion.object.ObjectInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Texture Track Test")
class TextureTrackTest {

    @Test
    public void testCreateTrack() {
        var oi = new ObjectInfo(new NullObject(), new CoordinateSystem(), "Null Object");
        var track = new TextureTrack(oi);
        Assertions.assertEquals(Timecourse.INTERPOLATING, track.getSmoothingMethod());
        Assertions.assertNull(track.param);
        Assertions.assertNotNull(track.theWeight);
        Assertions.assertNotNull(track.getTimecourse());

        Assertions.assertEquals(oi, track.getParent());
        Assertions.assertEquals(1, track.getSubtracks().length);

    }
}
