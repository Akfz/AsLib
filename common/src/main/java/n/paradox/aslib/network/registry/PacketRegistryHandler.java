package n.paradox.aslib.network.registry;

import n.paradox.aslib.network.api.Packet;
import net.minecraft.server.level.ServerPlayer;

public class PacketRegistryHandler {
    private final PacketRegistry registry;

    public PacketRegistryHandler(PacketRegistry registry) {
        this.registry = registry;
    }

    public void handle(Packet packet) {
        PacketEntry<Packet> entry = this.registry.get(packet.getClass());
        if (entry == null) {
            throw new IllegalStateException("Packet not registered: " + String.valueOf(packet.getClass()));
        } else {
            entry.handler().handle(packet);
        }
    }

    public void handle(Packet packet, ServerPlayer sender) {
        PacketEntry<Packet> entry = this.registry.get(packet.getClass());
        if (entry == null) {
            throw new IllegalStateException("Packet not registered: " + String.valueOf(packet.getClass()));
        } else {
            entry.handler().handle(packet, sender);
        }
    }
}
