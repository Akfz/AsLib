package v.akfz.aslib.event.impl;

import v.akfz.aslib.event.api.Event;

/**
 * So so so.. What we have here..
 *
 * Guide about events {@link v.akfz.aslib.event.api.Event}
 */
public class TickUpdater extends Event {
    public final boolean client;

    public TickUpdater(boolean client) {
        this.client = client;
    }
}
