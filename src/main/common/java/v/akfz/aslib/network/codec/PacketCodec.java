package v.akfz.aslib.network.codec;

import v.akfz.aslib.network.api.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import v.akfz.aslib.network.registry.PacketEntry;
import v.akfz.aslib.network.registry.PacketRegistry;

public final class PacketCodec {
    private final PacketRegistry registry;

    public PacketCodec(PacketRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public void encode(Packet packet, FriendlyByteBuf buffer) {
        PacketEntry<?> entry = this.registry.get(packet.getClass());
        if (entry == null) {
            throw new IllegalStateException("Packet not registered: " + packet.getClass());
        } else {
            if (((PacketEntry<Packet>) entry).encoder() == null) {
                System.out.println("ENCODER IS NULL : " + entry.id());
                return;
            }
            ((PacketEntry<Packet>) entry).encoder().encode(packet, buffer);
        }
    }

    public Packet decode(PacketEntry<?> raw, FriendlyByteBuf buffer) {
        if (raw.decoder() == null) {
            System.out.println("DECODER IS NULL : " + raw.id());
            return null;
        }
        return raw.decoder().decode(buffer);
    }

    public PacketEntry<?> getEntry(ResourceLocation resourceLocation) {
        return this.registry.get(resourceLocation);
    }
}
