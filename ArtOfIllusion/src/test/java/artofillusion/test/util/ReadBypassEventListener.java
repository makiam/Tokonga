package artofillusion.test.util;

import artofillusion.BypassEvent;
import lombok.Getter;
import org.greenrobot.eventbus.Subscribe;

public class ReadBypassEventListener {

    @Getter
    private int counter;
    @Getter private BypassEvent last;

    public ReadBypassEventListener() {
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBypassEvent(BypassEvent event) {
        last = event;
        counter++;
    }

    public void reset() {
        counter = 0;
    }
}