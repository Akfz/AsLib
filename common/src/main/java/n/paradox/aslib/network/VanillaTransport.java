package n.paradox.aslib.network;

import n.paradox.aslib.network.api.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public interface VanillaTransport {
    default void sendToServer(Packet packet) {
    }

    default void sendToServer(Packet packet, FriendlyByteBuf buf) {
    }

    default void sendToPlayer(ServerPlayer player, Packet packet) {
    }

    default void sendToPlayer(ServerPlayer player, Packet packet, FriendlyByteBuf buf) {
    }
}
