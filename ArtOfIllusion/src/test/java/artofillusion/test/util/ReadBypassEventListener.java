package artofillusion.test.util;

import artofillusion.BypassEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

@Slf4j
public class ReadBypassEventListener {

    @Getter
    private int counter;
    @Getter private BypassEvent last;

    public ReadBypassEventListener() {
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBypassEvent(BypassEvent event) {
        log.info("Event: {}", event);
        last = event;
        counter++;
    }

    public void reset() {
        counter = 0;
    }
}