package v.akfz.aslib.network.api;

import net.minecraft.network.FriendlyByteBuf;

@FunctionalInterface
public interface PacketEncoder<T extends Packet> {
    void encode(T packet, FriendlyByteBuf buf);
}
