package v.akfz.aslib.network.api;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public interface NetworkManager {
    void sendToServer(Packet packet);

    void sendToPlayer(ServerPlayer player, Packet packet);
}

