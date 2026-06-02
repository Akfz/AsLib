package n.paradox.aslib.event.impl.client;

import n.paradox.aslib.event.api.Event;
import net.minecraft.client.Minecraft;

public class ClientTickEvent extends Event {
    private final Minecraft minecraft;
    public ClientTickEvent(Minecraft m) {
        this.minecraft = m;
    }
    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
