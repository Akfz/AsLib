package v.akfz.aslib.network.bundle;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketHandler;
import v.akfz.aslib.network.codec.PacketCodec;
import v.akfz.aslib.network.registry.PacketEntry;
import v.akfz.aslib.network.registry.PacketRegistry;
import v.akfz.aslib.network.registry.PacketRegistryHandler;

public final class BundlePayloadHandler implements PacketHandler<BundlePayloadPacket> {
    private final PacketRegistry registry;
    private final PacketCodec codec;
    private final PacketRegistryHandler registryHandler;

    public BundlePayloadHandler(PacketRegistry registry, PacketCodec codec, PacketRegistryHandler registryHandler) {
        this.registry = registry;
        this.codec = codec;
        this.registryHandler = registryHandler;
    }

    @Override
    public void handle(BundlePayloadPacket packet) {
        handleBundle(packet, null);
    }

    @Override
    public void handle(BundlePayloadPacket packet, ServerPlayer player) {
        handleBundle(packet, player);
    }

    private void handleBundle(BundlePayloadPacket payload, ServerPlayer player) {
        BundleHeaderPacket header = BundleManager.getAndRemoveHeader(payload.getBundleId());
        if (header == null) {
            System.out.println("Data packet received without a schema header.: " + payload.getBundleId());
            return;
        }

        FriendlyByteBuf payloadBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.getData()));
        try {
            for (BundleHeaderPacket.Entry entry : header.getEntries()) {
                PacketEntry<?> packetEntry = registry.get(entry.id());
                if (packetEntry == null) {
                    System.out.println("Unknown packet ID in the binding: " + entry.id());
                    payloadBuf.skipBytes(entry.length());
                    continue;
                }

                FriendlyByteBuf slicedBuf = new FriendlyByteBuf(payloadBuf.readSlice(entry.length()));
                try {
                    Packet decodedPacket = codec.decode(packetEntry, slicedBuf);
                    if (decodedPacket != null) {
                        if (player != null) {
                            registryHandler.handle(decodedPacket, player);
                        } else {
                            registryHandler.handle(decodedPacket);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error unpacking package from the bundle: " + entry.id());
                    e.printStackTrace();
                }
            }
        } finally {
            payloadBuf.release();
        }
    }
}