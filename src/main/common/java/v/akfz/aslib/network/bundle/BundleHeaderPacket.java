package v.akfz.aslib.network.bundle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import v.akfz.aslib.network.annotation.NetworkPacket;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketDecoder;
import v.akfz.aslib.network.api.PacketEncoder;

import java.util.ArrayList;
import java.util.List;

@NetworkPacket("aslib:bundle_header")
public final class BundleHeaderPacket implements Packet {
    private final long bundleId;
    private final List<Entry> entries;

    public BundleHeaderPacket(long bundleId, List<Entry> entries) {
        this.bundleId = bundleId;
        this.entries = entries;
    }

    public long getBundleId() { return bundleId; }
    public List<Entry> getEntries() { return entries; }

    public static void encode(BundleHeaderPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.bundleId);
        buf.writeVarInt(packet.entries.size());
        for (Entry entry : packet.entries) {
            buf.writeResourceLocation(entry.id());
            buf.writeVarInt(entry.length());
        }
    }

    public static BundleHeaderPacket decode(FriendlyByteBuf buf) {
        long bundleId = buf.readLong();
        int size = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int length = buf.readVarInt();
            entries.add(new Entry(id, length));
        }
        return new BundleHeaderPacket(bundleId, entries);
    }

    @Override
    public PacketEncoder<BundleHeaderPacket> encoder() { return BundleHeaderPacket::encode; }
    @Override
    public PacketDecoder<BundleHeaderPacket> decoder() { return BundleHeaderPacket::decode; }

    public record Entry(ResourceLocation id, int length) {}
}