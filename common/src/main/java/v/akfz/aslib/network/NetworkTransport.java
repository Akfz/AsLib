package v.akfz.aslib.network;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.registry.PacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class NetworkTransport implements VanillaTransport {
    private final PacketRegistry registry;

    public NetworkTransport(PacketRegistry registry) {
        this.registry = registry;
    }

    public void sendToServer(@NotNull Packet packet) {
        this.sendToServer(packet, this.createFriendlyByteBuf());
    }

    public void sendToServer(@NotNull Packet packet, @NotNull FriendlyByteBuf buf) {
        Preconditions.checkNotNull(packet);
        Preconditions.checkNotNull(buf);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.getConnection() != null) {
            this.encodePacket(packet, buf);
            ServerboundCustomPayloadPacket customPacket = new ServerboundCustomPayloadPacket(this.getId(packet), buf);
            mc.player.connection.send(customPacket);
        }
    }

    public void sendToPlayer(@NotNull ServerPlayer player, @NotNull Packet packet) {
        this.sendToPlayer(player, packet, this.createFriendlyByteBuf());
    }

    public void sendToPlayer(@NotNull ServerPlayer player, @NotNull Packet packet, @NotNull FriendlyByteBuf buf) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(packet);
        Preconditions.checkNotNull(buf);
        this.encodePacket(packet, buf);
        ClientboundCustomPayloadPacket customPacket = new ClientboundCustomPayloadPacket(this.getId(packet), buf);
        player.connection.send(customPacket);
    }

    private void encodePacket(@NotNull Packet packet, @NotNull FriendlyByteBuf buf) {
        try {
            AsLibNetworking.CODEC.encode(packet, buf);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ResourceLocation getId(Packet packet) {
        return this.registry.get(packet.getClass()).id();
    }

    private FriendlyByteBuf createFriendlyByteBuf() {
        return new FriendlyByteBuf(new FriendlyByteBuf(Unpooled.buffer()));
    }
}