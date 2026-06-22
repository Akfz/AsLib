package n.paradox.aslib.event.impl.server;

import n.paradox.aslib.event.api.Event;

public class ServerFirstTickEvent extends Event {
    public final Runnable event;
    public ServerFirstTickEvent(Runnable event) {
        this.event = event;
    }
}
