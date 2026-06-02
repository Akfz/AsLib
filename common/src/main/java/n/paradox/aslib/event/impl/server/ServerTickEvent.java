package n.paradox.aslib.event.impl.server;

import n.paradox.aslib.event.api.Event;
import net.minecraft.server.dedicated.DedicatedServer;

public class ServerTickEvent extends Event {
    private final DedicatedServer minecraft;
    public ServerTickEvent(DedicatedServer m) {
        this.minecraft = m;
    }
    public DedicatedServer getMinecraft() {
        return this.minecraft;
    }
}
