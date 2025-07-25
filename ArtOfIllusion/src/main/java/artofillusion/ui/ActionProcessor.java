/* Copyright (C) 2001-2004 by Peter Eastman
   Changes copyright (C) 2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import javax.swing.*;

/**
 * This class processes "discardable" events. This is typically used for
 * mouse-moved or mouse-dragged events, where significant time is needed to process
 * each event, and they might be delivered faster than they can be processed.
 * <p>
 * Callback objects corresponding to particular events are passed to addEvent() as they
 * occur. They are called one at a time. If several new events are delivered before
 * the last one has been finished, all but the most recent one are discarded.
 * <p>
 * This class is not thread safe. addEvent() and stopProcessing() should only ever be
 * invoked from the event dispatch thread. In turn, all processing of events is
 * guaranteed to be done on the event dispatch thread.
 */
public class ActionProcessor {

    private Runnable nextEvent;
    private boolean isCanceled;

    /**
     * Add an event to the queue.
     */
    public synchronized void addEvent(Runnable event) {
        nextEvent = event;
        SwingUtilities.invokeLater(() -> {
            if (nextEvent != null && !isCanceled) {
                nextEvent.run();
            }
            nextEvent = null;
        });
    }

    /**
     * Halt processing, and discard any further events that are added to the queue.
     */
    public void stopProcessing() {
        isCanceled = true;
    }

    /**
     * Determine whether this stopProcessing() has been invoked.
     */
    public boolean hasBeenStopped() {
        return isCanceled;
    }

    /**
     * Determine whether there is an event waiting to be processed.
     */
    public boolean hasEvent() {
        return (nextEvent != null);
    }
}
