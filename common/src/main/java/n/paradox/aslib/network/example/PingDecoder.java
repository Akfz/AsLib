package n.paradox.aslib.network.example;

import n.paradox.aslib.network.api.PacketDecoder;
import net.minecraft.network.FriendlyByteBuf;

public final class PingDecoder implements PacketDecoder<PingPacket> {
    public PingPacket decode(FriendlyByteBuf buf) {
        return new PingPacket(buf.readLong());
    }
}
