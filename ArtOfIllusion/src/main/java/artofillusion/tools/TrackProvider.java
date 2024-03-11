package artofillusion.tools;

import artofillusion.object.ObjectInfo;

import java.util.Collection;
import java.util.List;

public interface TrackProvider {
    String getCategory();

    String getName();

    void create(Collection<ObjectInfo> objects);
}
