package n.paradox.aslib.network.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import n.paradox.aslib.network.annotation.NetworkPacket;
import n.paradox.aslib.network.api.Packet;
import n.paradox.aslib.network.api.PacketDecoder;
import n.paradox.aslib.network.api.PacketEncoder;
import n.paradox.aslib.network.api.PacketHandler;
import net.minecraft.resources.ResourceLocation;

public final class PacketRegistry {
    private final Map<ResourceLocation, PacketEntry<?>> byId = new ConcurrentHashMap<>();
    private final Map<Class<? extends Packet>, PacketEntry<?>> byClass = new ConcurrentHashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Packet> void register(Class<T> type, PacketEncoder<T> encoder, PacketDecoder<T> decoder, PacketHandler<T> handler) {
        ResourceLocation resourceLocation = this.getResourceLocation(type);
        if (resourceLocation == null) {
            throw new IllegalArgumentException("ResourceLocation is null");
        } else {
            this.register(new PacketEntry(resourceLocation, type, encoder, decoder, handler));
        }
    }

    public <T extends Packet> void register(PacketEntry<T> entry) {
        if (this.byId.containsKey(entry.id())) {
            throw new IllegalStateException("Duplicate packet id: " + entry.id());
        } else if (this.byClass.containsKey(entry.type())) {
            throw new IllegalStateException("Packet already registered: " + entry.type().getName());
        } else {
            this.byId.put(entry.id(), entry);
            this.byClass.put(entry.type(), entry);
        }
    }

    private ResourceLocation getResourceLocation(Class<? extends Packet> clazz) {
        if (clazz.isAnnotationPresent(NetworkPacket.class)) {
            NetworkPacket annotation = (NetworkPacket)clazz.getAnnotation(NetworkPacket.class);
            String value = annotation.value();
            return ResourceLocation.tryParse(value);
        } else {
            return null;
        }
    }

    public boolean isPresent(ResourceLocation id) {
        return this.byId.get(id) != null;
    }

    public boolean isPresent(Class<?> type) {
        return this.byClass.get(type) != null;
    }

    public PacketEntry<?> get(ResourceLocation id) {
        return this.byId.get(id);
    }

    public PacketEntry<?> get(Class<?> type) {
        return this.byClass.get(type);
    }
}
