package v.akfz.aslib.event.impl;

import v.akfz.aslib.event.api.Event;

public class TickUpdater extends Event {
    public final boolean client;

    public TickUpdater(boolean client) {
        this.client = client;
    }
}
