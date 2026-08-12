package v.akfz.aslib.network.api;

import org.jetbrains.annotations.Nullable;

public interface Packet {
    @Nullable default PacketEncoder<? extends Packet> encoder() { return null; }
    @Nullable default PacketDecoder<? extends Packet> decoder() { return null; }
}