package v.akfz.aslib.network.example;

import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.api.PacketHandler;
import net.minecraft.server.level.ServerPlayer;

public final class PingHandler implements PacketHandler<PingPacket> {
    public void handle(PingPacket packet) {
        System.out.println("Client Ping: " + packet.getTime());
    }

    public void handle(PingPacket packet, ServerPlayer player) {
        System.out.println("Server Ping: " + packet.getTime());
        AsLibNetworking.SENDER.sendToPlayer(player, new PingPacket(System.currentTimeMillis()));
    }
}
