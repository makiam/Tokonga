package artofillusion;

import artofillusion.ui.Translate;
import buoy.widget.BMenu;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Slf4j
public class LayoutMenu extends BMenu {
    @Getter
    private final LayoutWindow layout;

    public LayoutMenu(LayoutWindow layout, String title) {
        super(Translate.text(title));
        this.layout = layout;
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
        layout.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                org.greenrobot.eventbus.EventBus.getDefault().unregister(LayoutMenu.this);
            }
        });
    }

    @Subscribe
    final void stub(org.greenrobot.eventbus.NoSubscriberEvent event) {
        //Greenrobot stub method
    }
}
