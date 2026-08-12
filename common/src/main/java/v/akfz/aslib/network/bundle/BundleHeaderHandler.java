package v.akfz.aslib.network.bundle;

import net.minecraft.server.level.ServerPlayer;
import v.akfz.aslib.network.api.PacketHandler;

public final class BundleHeaderHandler implements PacketHandler<BundleHeaderPacket> {
    @Override
    public void handle(BundleHeaderPacket packet) {
        BundleManager.registerHeader(packet);
    }

    @Override
    public void handle(BundleHeaderPacket packet, ServerPlayer player) {
        BundleManager.registerHeader(packet);
    }
}