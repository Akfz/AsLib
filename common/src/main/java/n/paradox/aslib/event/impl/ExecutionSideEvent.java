package n.paradox.aslib.event.impl;

import n.paradox.aslib.event.api.Event;

import java.util.HashSet;
import java.util.Set;

public class ExecutionSideEvent extends Event {
    private static final Set<Runnable> onStartClient = new HashSet<>();
    public static void registerStartClient(Runnable event) {
        onStartClient.add(event);
    }

    private static final Set<Runnable> onStartServer = new HashSet<>();
    public static void registerStartServer(Runnable event) {
        onStartServer.add(event);
    }
    public static Set<Runnable> getOnStartClient() {
        return new HashSet<>(onStartClient);
    }
    public static Set<Runnable> getOnStartServer() {
        return new HashSet<>(onStartServer);
    }

    public ExecutionSideEvent() {
    }
}
