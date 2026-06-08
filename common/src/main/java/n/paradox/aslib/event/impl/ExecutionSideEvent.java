package n.paradox.aslib.event.impl;

import n.paradox.aslib.event.api.Event;

public class ExecutionSideEvent extends Event {
    public final Runnable client;
    public final Runnable server;
    public ExecutionSideEvent(Runnable onClient, Runnable onServer) {
        this.client = onClient;
        this.server = onServer;
    }
}
