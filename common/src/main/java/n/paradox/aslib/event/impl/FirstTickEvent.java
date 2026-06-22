package n.paradox.aslib.event.impl;

import n.paradox.aslib.event.api.Event;

import java.util.HashSet;
import java.util.Set;

public class FirstTickEvent extends Event {
    private static final Set<Runnable> onStartTickClient = new HashSet<>();
    public static void registerStartTickClient(Runnable event) {
        onStartTickClient.add(event);
    }
    private static final Set<Runnable> onStartTickServer = new HashSet<>();
    public static void registerStartTickServer(Runnable event) {
        onStartTickServer.add(event);
    }

    public static Set<Runnable> getOnStartTickClient() {
        return new HashSet<>(onStartTickClient);
    }
    public static Set<Runnable> getOnStartTickServer() {
        return new HashSet<>(onStartTickServer);
    }
}
