package v.akfz.aslib.network.api;

import org.jetbrains.annotations.Nullable;

/**
 * Interface representing a network packet.
 * Implementations should be annotated with {@link v.akfz.aslib.network.annotation.NetworkPacket}.
 */
public interface Packet {
    /**
     * @return The encoder responsible for serializing this packet into byte buffer.
     */
    @Nullable default PacketEncoder<? extends Packet> encoder() { return null; }

    /**
     * @return The decoder responsible for deserializing this packet from byte buffer.
     */
    @Nullable default PacketDecoder<? extends Packet> decoder() { return null; }
}