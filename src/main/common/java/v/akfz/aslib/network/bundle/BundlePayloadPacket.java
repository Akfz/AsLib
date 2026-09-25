package v.akfz.aslib.network.bundle;

import net.minecraft.network.FriendlyByteBuf;
import v.akfz.aslib.network.annotation.NetworkPacket;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.api.PacketDecoder;
import v.akfz.aslib.network.api.PacketEncoder;

@NetworkPacket("aslib:bundle_payload")
public final class BundlePayloadPacket implements Packet {
    private final long bundleId;
    private final byte[] data;

    public BundlePayloadPacket(long bundleId, byte[] data) {
        this.bundleId = bundleId;
        this.data = data;
    }

    public long getBundleId() { return bundleId; }
    public byte[] getData() { return data; }

    public static void encode(BundlePayloadPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.bundleId);
        buf.writeByteArray(packet.data);
    }

    public static BundlePayloadPacket decode(FriendlyByteBuf buf) {
        long bundleId = buf.readLong();
        byte[] data = buf.readByteArray();
        return new BundlePayloadPacket(bundleId, data);
    }

    @Override
    public PacketEncoder<BundlePayloadPacket> encoder() { return BundlePayloadPacket::encode; }
    @Override
    public PacketDecoder<BundlePayloadPacket> decoder() { return BundlePayloadPacket::decode; }
}