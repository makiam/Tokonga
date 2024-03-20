package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.ProceduralPositionTrack;
import artofillusion.animation.ProceduralRotationTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

public class ProceduralRotationTrackProvider implements TrackProvider {
    @Override
    public String getCategory() {
        return Translate.text("menu.rotationTrack");
    }

    @Override
    public String getName() {
        return Translate.text("menu.proceduralTrack");
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        TrackProvider.add(item, new ProceduralRotationTrack(item), undo);
    }
}
