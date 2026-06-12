package n.paradox.aslib.network.example;

import n.paradox.aslib.network.api.PacketEncoder;
import net.minecraft.network.FriendlyByteBuf;

public final class PingEncoder implements PacketEncoder<PingPacket> {
    public void encode(PingPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.getTime());
    }
}
