package v.akfz.aslib.network.api;

import net.minecraft.server.level.ServerPlayer;

public interface PacketHandler<T extends Packet> {
    default void handle(T packet) {
    }

    default void handle(T packet, ServerPlayer player) {
    }
}
