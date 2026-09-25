package v.akfz.aslib.network.codec;

import net.minecraft.network.FriendlyByteBuf;

public interface PacketTypeCodec<T> {
	void encode(FriendlyByteBuf buf, T value);
	T decode(FriendlyByteBuf buf);
}