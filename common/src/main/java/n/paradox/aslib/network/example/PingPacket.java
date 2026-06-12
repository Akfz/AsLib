package n.paradox.aslib.network.example;

import n.paradox.aslib.network.annotation.NetworkPacket;
import n.paradox.aslib.network.api.AbstractPacket;

@NetworkPacket("aslib:ping")
public final class PingPacket extends AbstractPacket {
    private final long time;

    public PingPacket(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }
}
