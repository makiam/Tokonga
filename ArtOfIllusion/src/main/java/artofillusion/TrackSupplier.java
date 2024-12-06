package artofillusion;

import artofillusion.animation.Track;
import artofillusion.object.ObjectInfo;

import java.util.function.Function;

@FunctionalInterface
public interface TrackSupplier extends Function<ObjectInfo, Track> {

}
