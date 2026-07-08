package v.akfz.aslib.network.registry;

import v.akfz.aslib.network.api.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class PacketCodec {
    private final PacketRegistry registry;

    public PacketCodec(PacketRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public void encode(Packet packet, FriendlyByteBuf buffer) {
        PacketEntry<?> entry = this.registry.get(packet.getClass());
        if (entry == null) {
            throw new IllegalStateException("Packet not registered: " + String.valueOf(packet.getClass()));
        } else {
            ((PacketEntry<Packet>) entry).encoder().encode(packet, buffer);
        }
    }

    public Packet decode(PacketEntry<?> raw, FriendlyByteBuf buffer) {
        return raw.decoder().decode(buffer);
    }

    public PacketEntry<?> getEntry(ResourceLocation resourceLocation) {
        PacketEntry<?> raw = this.registry.get(resourceLocation);
        return raw == null ? null : raw;
    }
}
