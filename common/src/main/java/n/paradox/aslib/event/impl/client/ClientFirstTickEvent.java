package n.paradox.aslib.event.impl.client;

import n.paradox.aslib.event.api.Event;

public class ClientFirstTickEvent extends Event {
    public final Runnable event;
    public ClientFirstTickEvent(Runnable event) {
        this.event = event;
    }
}
