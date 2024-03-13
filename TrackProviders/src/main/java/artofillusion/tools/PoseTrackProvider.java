package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

import java.util.Collection;
import java.util.function.Predicate;

public class PoseTrackProvider implements TrackProvider {
    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public String getName() {
        return Translate.text("menu.poseTrack");
    }

    @Override
    public void create(Collection<ObjectInfo> objects, UndoRecord undo) {
        Map<Boolean, List<ObjectInfo>> pon = objects.stream().collect(Collectors.partitioningBy(PoseTrackProvider::isPosable));


        pon.get(true).forEach(obj -> obj.addTrack(new PoseTrack(obj), 0));
        pon.get(false).forEach(obj -> obj.addTrack(new PoseTrack(obj), 0));

    }

    /**
     * Check scene ObjectInfo posable.
     *
     * @param  info	object to check
     * @return  true if the object is posable
     */
    private static boolean isPosable(ObjectInfo info) {
        return info.getObject().getPosableObject() != null;
    }

}
