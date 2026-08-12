package v.akfz.aslib.network.codec;

import net.minecraft.network.FriendlyByteBuf;

public interface TypeCodec<T> {
    void encode(FriendlyByteBuf buf, T value);
    T decode(FriendlyByteBuf buf);
}