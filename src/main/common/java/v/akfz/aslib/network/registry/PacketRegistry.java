package v.akfz.aslib.network.registry;

import net.minecraft.resources.ResourceLocation;
import v.akfz.aslib.network.annotation.NetworkPacket;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketDecoder;
import v.akfz.aslib.network.api.PacketEncoder;
import v.akfz.aslib.network.api.PacketHandler;
import v.akfz.aslib.network.codec.PacketAutoCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for managing network packets, their codecs, and handlers.
 * <p>
 * Registration returns a {@link Registration} handle that can be used to
 * remove the entry later. Safe to ignore — for the common "register once at
 * startup" case there's nothing to clean up.
 */
public final class PacketRegistry {

    private final Map<ResourceLocation, PacketEntry<?>> byId = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, PacketEntry<?>> byClass = new ConcurrentHashMap<>();


    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Packet> Registration register(Class<T> type, PacketEncoder<T> encoder,
                                                    PacketDecoder<T> decoder, PacketHandler<T> handler) {
        ResourceLocation rl = this.getResourceLocation(type);
        if (rl == null) {
            throw new IllegalArgumentException("ResourceLocation is missing on class: " + type.getName());
        }
        putEntry(new PacketEntry(rl, type, encoder, decoder, handler));
        return () -> unregister(type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Packet> void register(T dummyInstance, PacketHandler<T> handler) {
        ResourceLocation rl = this.getResourceLocation(dummyInstance.getClass());
        if (rl == null) {
            throw new IllegalArgumentException("ResourceLocation is missing on class: "
                    + dummyInstance.getClass().getName());
        }

        PacketEncoder<T> enc = (PacketEncoder<T>) dummyInstance.encoder();
        PacketDecoder<T> dec = (PacketDecoder<T>) dummyInstance.decoder();

        if (enc == null || dec == null) {
            PacketAutoCodec<T> auto = (PacketAutoCodec<T>) PacketAutoCodec.of(dummyInstance.getClass());
            if (enc == null) enc = (packet, buf) -> auto.encode(buf, packet);
            if (dec == null) dec = auto::decode;
        }

        putEntry(new PacketEntry(rl, (Class<T>) dummyInstance.getClass(), enc, dec, handler));
    }

    public <T extends Packet> Registration register(PacketEntry<T> entry) {
        putEntry(entry);
        return () -> unregister(entry.type());
    }

    public void unregister(Class<? extends Packet> type) {
        PacketEntry<?> removed = this.byClass.remove(type);
        if (removed != null) this.byId.remove(removed.id());
    }

    public void unregister(ResourceLocation id) {
        PacketEntry<?> removed = this.byId.remove(id);
        if (removed != null) this.byClass.remove(removed.type());
    }

    public boolean isPresent(ResourceLocation id) {
        return this.byId.containsKey(id);
    }

    public boolean isPresent(Class<?> type) {
        return this.byClass.containsKey(type);
    }

    public PacketEntry<?> get(ResourceLocation id) {
        return this.byId.get(id);
    }

    public PacketEntry<?> get(Class<?> type) {
        return this.byClass.get(type);
    }

    private <T extends Packet> void putEntry(PacketEntry<T> entry) {
        if (this.byId.containsKey(entry.id())) {
            throw new IllegalStateException("Duplicate packet id: " + entry.id());
        }
        if (this.byClass.containsKey(entry.type())) {
            throw new IllegalStateException("Packet already registered: " + entry.type().getName());
        }
        this.byId.put(entry.id(), entry);
        this.byClass.put(entry.type(), entry);
    }

    private ResourceLocation getResourceLocation(Class<? extends Packet> clazz) {
        if (clazz.isAnnotationPresent(NetworkPacket.class)) {
            NetworkPacket annotation = clazz.getAnnotation(NetworkPacket.class);
            return ResourceLocation.tryParse(annotation.value());
        }
        return null;
    }

    @FunctionalInterface
    public interface Registration extends AutoCloseable {
        void unregister();
        @Override default void close() { unregister(); }
    }
}