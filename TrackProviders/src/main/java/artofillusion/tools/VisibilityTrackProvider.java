package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.VisibilityTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

import java.util.Collection;

public class VisibilityTrackProvider implements TrackProvider {
    @Override
    public String getName() {
        return Translate.text("menu.visibilityTrack");
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public void create(Collection<ObjectInfo> objects, UndoRecord undo) {
        for (ObjectInfo item : objects) {
            item.addTrack(new VisibilityTrack(item), 0);
        }
    }
}
