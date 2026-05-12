/* Copyright (C) 2025-2026 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public final class LoadEventListener implements Closeable {
    private final List<BypassEvent> events = new ArrayList<>();

    LoadEventListener() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBypassEvent(BypassEvent event) {
        events.add(event);
    }

    public List<String> getEventMessages() {
        return events.stream().map(BypassEvent::message).toList();
    }

    @Override
    public void close() throws IOException {
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this);
    }
}
