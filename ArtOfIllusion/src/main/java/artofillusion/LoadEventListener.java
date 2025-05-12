/* Copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public final class LoadEventListener {
    private List<BypassEvent> events = new ArrayList<>();

    LoadEventListener() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onBypassEvent(BypassEvent event) {
        events.add(event);
    }

    public List<String> getEventMessages() {
        return events.stream().map(event -> event.getMessage()).collect(Collectors.toList());
    }
}
