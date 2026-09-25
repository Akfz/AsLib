package v.akfz.aslib.network.api;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface PacketDecoder<T extends Packet> {
    T decode(FriendlyByteBuf buf);
}
