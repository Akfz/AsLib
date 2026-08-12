package v.akfz.aslib.network.registry;

import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketDecoder;
import v.akfz.aslib.network.api.PacketEncoder;
import v.akfz.aslib.network.api.PacketHandler;
import net.minecraft.resources.ResourceLocation;

public final class PacketEntry<T extends Packet> {
    private final ResourceLocation id;
    private final Class<T> type;
    private final PacketEncoder<T> encoder;
    private final PacketDecoder<T> decoder;
    private final PacketHandler<T> handler;

    public PacketEntry(ResourceLocation id, Class<T> type, PacketEncoder<T> encoder, PacketDecoder<T> decoder, PacketHandler<T> handler) {
        this.id = id;
        this.type = type;
        this.encoder = encoder;
        this.decoder = decoder;
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public PacketEntry(ResourceLocation id, T dummyInstance, PacketHandler<T> handler) {
        this.id = id;
        this.type = (Class<T>) dummyInstance.getClass();

        if (dummyInstance.decoder() == null || dummyInstance.encoder() == null) {
            System.out.println(id + ", encoder or decoder is null!");
        }
        this.encoder = (PacketEncoder<T>) dummyInstance.encoder();
        this.decoder = (PacketDecoder<T>) dummyInstance.decoder();
        this.handler = handler;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public Class<T> type() {
        return this.type;
    }

    public PacketEncoder<T> encoder() {
        return this.encoder;
    }

    public PacketDecoder<T> decoder() {
        return this.decoder;
    }

    public PacketHandler<T> handler() {
        return this.handler;
    }
}
