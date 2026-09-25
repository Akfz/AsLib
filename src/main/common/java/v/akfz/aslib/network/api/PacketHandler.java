package v.akfz.aslib.network.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * Handler interface for executing logic when a packet is received.
 *
 * @param <T> The packet type.
 */
public interface PacketHandler<T extends Packet> {
    /**
     * Executed on the client side when a server packet is received.
     *
     * @param packet Received packet instance.
     */
    default void handle(T packet) {}

    /**
     * Executed on the server side when a client packet is received.
     *
     * @param packet Received packet instance.
     * @param player Client player who sent the packet.
     */
    default void handle(T packet, ServerPlayer player) {}
}