package v.akfz.aslib.network.example;

import v.akfz.aslib.network.annotation.NetworkPacket;
import v.akfz.aslib.network.api.AbstractPacket;

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
