package artofillusion.test.util;

import artofillusion.BypassEvent;
import lombok.Getter;
import org.greenrobot.eventbus.Subscribe;

public class ReadBypassEventListener {

    @Getter
    private int counter;

    public ReadBypassEventListener() {
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBypassEvent(BypassEvent event) {
        counter++;
    }

    public void reset() {
        counter = 0;
    }
}