package artofillusion.test.util;

import artofillusion.animation.Track;
import artofillusion.object.ObjectInfo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrackTestUtil {

    public static Track[] getTracks(ObjectInfo test) {
        return test.getTracks();
    }

    public static Track getTrack(ObjectInfo test, int index) {
        return TrackTestUtil.getTracks(test)[index];
    }

    public static int getLength(ObjectInfo test) {
        return TrackTestUtil.getTracks(test).length;
    }
}
