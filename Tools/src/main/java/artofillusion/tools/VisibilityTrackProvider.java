package artofillusion.tools;

import artofillusion.animation.VisibilityTrack;
import artofillusion.object.ObjectInfo;

import java.util.Collection;

public class VisibilityTrackProvider implements TrackProvider {
    @Override
    public String getName() {
        return "Visibility";
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public void create(Collection<ObjectInfo> objects) {
        for (ObjectInfo item : objects) {
            item.addTrack(new VisibilityTrack(item), 0);
        }
    }
}
