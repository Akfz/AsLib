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

public final class BundleBuilder {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

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

    public BundleBuilder add(Packet packet) {
        this.packets.add(new PacketHolder<>(packet, null, null));
        return this;
    }

    public <T extends Packet> BundleBuilder add(T packet, PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
        this.packets.add(new PacketHolder<>(packet, encoder, decoder));
        return this;
    }

    @SuppressWarnings("unchecked")
    public void send(BiConsumer<Packet, Packet> sender) {
        if (packets.size() < 2) {
            throw new IllegalStateException("The bundle must contain at least 2 packets!");
        }

        long bundleId = ID_GENERATOR.incrementAndGet();
        List<BundleHeaderPacket.Entry> entries = new ArrayList<>();
        FriendlyByteBuf dataBuf = new FriendlyByteBuf(Unpooled.buffer());

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
                        throw new IllegalStateException("The package is not registered: " + packet.getClass());
                    }

                    entries.add(new BundleHeaderPacket.Entry(entry.id(), length));
                    dataBuf.writeBytes(singleBuf);
                } finally {
                    singleBuf.release();
                }
            }

            byte[] rawData = new byte[dataBuf.readableBytes()];
            dataBuf.readBytes(rawData);

            BundleHeaderPacket header = new BundleHeaderPacket(bundleId, entries);
            BundlePayloadPacket payload = new BundlePayloadPacket(bundleId, rawData);

            sender.accept(header, payload);
        } finally {
            dataBuf.release();
        }
    }

    private record PacketHolder<T extends Packet>(
            T packet,
            PacketEncoder<T> encoder,
            PacketDecoder<T> decoder
    ) {}
}