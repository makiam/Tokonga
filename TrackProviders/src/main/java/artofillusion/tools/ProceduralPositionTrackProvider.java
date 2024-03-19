package artofillusion.tools;

import artofillusion.UndoRecord;
import artofillusion.animation.ProceduralPositionTrack;
import artofillusion.object.ObjectInfo;
import artofillusion.ui.Translate;

public class ProceduralPositionTrackProvider implements TrackProvider {
    @Override
    public String getCategory() {
        return Translate.text("menu.positionTrack");
    }

    @Override
    public String getName() {
        return Translate.text("menu.proceduralTrack");
    }

    @Override
    public void forEach(ObjectInfo item, UndoRecord undo) {
        TrackProvider.add(item, new ProceduralPositionTrack(item), undo);
    }
}
