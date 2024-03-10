package artofillusion.tools;

import artofillusion.LayoutWindow;
import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TracksMenu extends BMenu {
    private LayoutWindow layout;
    public TracksMenu(LayoutWindow layout) {
        super(Translate.text("menu.addTrack"));
        this.layout = layout;
    }
}
