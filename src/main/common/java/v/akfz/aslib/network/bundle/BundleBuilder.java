package v.akfz.aslib.network.bundle;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketDecoder;
import v.akfz.aslib.network.api.PacketEncoder;
import v.akfz.aslib.network.api.PacketHandler;
import v.akfz.aslib.network.codec.PacketCodec;
import v.akfz.aslib.network.registry.PacketEntry;
import v.akfz.aslib.network.registry.PacketRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Helper class for bundling multiple network packets into a single payload stream.
 * Automatically splits payload chunks if total size exceeds 30 KB.
 */
public final class BundleBuilder {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    private static final int MAX_PAYLOAD_SIZE = 30000;

    private final PacketRegistry registry;
    private final PacketCodec codec;
    private final List<PacketHolder<?>> packets = new ArrayList<>();

    public BundleBuilder(PacketRegistry registry, PacketCodec codec) {
        this.registry = registry;
        this.codec = codec;
    }

    public BundleBuilder() {
        this.registry = AsLibNetworking.REGISTRY;
        this.codec = AsLibNetworking.CODEC;
    }

    /**
     * Adds a packet to the bundle.
     */
    public BundleBuilder add(Packet packet) {
        this.packets.add(new PacketHolder<>(packet, null, null));
        return this;
    }

    /**
     * Adds a packet with inline custom encoder and decoder.
     */
    public <T extends Packet> BundleBuilder add(T packet, PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
        this.packets.add(new PacketHolder<>(packet, encoder, decoder));
        return this;
    }

    /**
     * Serializes and transmits all queued packets via the provided sender callback.
     *
     * @param sender Callback that accepts the generated header and payload packets.
     */
    @SuppressWarnings("unchecked")
    public void send(BiConsumer<Packet, Packet> sender) {
        if (packets.isEmpty()) {
            return;
        }

        List<BundleHeaderPacket.Entry> currentEntries = new ArrayList<>();
        FriendlyByteBuf currentDataBuf = new FriendlyByteBuf(Unpooled.buffer());

        try {
            for (PacketHolder<?> holder : packets) {
                Packet packet = holder.packet();
                FriendlyByteBuf singleBuf = new FriendlyByteBuf(Unpooled.buffer());
                try {
                    if (holder.encoder() != null && holder.decoder() != null) {
                        Class<Packet> type = (Class<Packet>) packet.getClass();
                        if (!registry.isPresent(type)) {
                            registry.register(
                                    type,
                                    (PacketEncoder<Packet>) holder.encoder(),
                                    (PacketDecoder<Packet>) holder.decoder(),
                                    new PacketHandler<Packet>() {}
                            );
                        }
                    }

                    if (holder.encoder() != null) {
                        ((PacketEncoder<Packet>) holder.encoder()).encode(packet, singleBuf);
                    } else {
                        codec.encode(packet, singleBuf);
                    }

                    int length = singleBuf.readableBytes();
                    PacketEntry<?> entry = registry.get(packet.getClass());
                    if (entry == null) {
                        throw new IllegalStateException("Packet is not registered: " + packet.getClass());
                    }

                    if (currentDataBuf.readableBytes() > 0 && currentDataBuf.readableBytes() + length > MAX_PAYLOAD_SIZE) {
                        flushBundle(currentEntries, currentDataBuf, sender);
                        currentEntries = new ArrayList<>();
                        currentDataBuf = new FriendlyByteBuf(Unpooled.buffer());
                    }

                    currentEntries.add(new BundleHeaderPacket.Entry(entry.id(), length));
                    currentDataBuf.writeBytes(singleBuf);
                } finally {
                    singleBuf.release();
                }
            }

            if (currentDataBuf.readableBytes() > 0) {
                flushBundle(currentEntries, currentDataBuf, sender);
            }
        } finally {
            if (currentDataBuf.refCnt() > 0) {
                currentDataBuf.release();
            }
        }
    }

    private void flushBundle(List<BundleHeaderPacket.Entry> entries, FriendlyByteBuf dataBuf, BiConsumer<Packet, Packet> sender) {
        long bundleId = ID_GENERATOR.incrementAndGet();
        byte[] rawData = new byte[dataBuf.readableBytes()];
        dataBuf.readBytes(rawData);

        BundleHeaderPacket header = new BundleHeaderPacket(bundleId, entries);
        BundlePayloadPacket payload = new BundlePayloadPacket(bundleId, rawData);

        sender.accept(header, payload);
    }

    private record PacketHolder<T extends Packet>(
            T packet,
            PacketEncoder<T> encoder,
            PacketDecoder<T> decoder
    ) {}
}