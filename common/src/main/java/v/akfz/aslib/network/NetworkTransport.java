package v.akfz.aslib.network;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import v.akfz.aslib.network.api.Packet;
import v.akfz.aslib.network.registry.PacketRegistry;

public class NetworkTransport implements VanillaTransport {
    private final PacketRegistry registry;

    public NetworkTransport(PacketRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void sendToServer(@NotNull Packet packet) {
        this.sendToServer(packet, this.createFriendlyByteBuf());
    }

    @Override
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

    @Override
    public void sendToPlayer(@NotNull ServerPlayer player, @NotNull Packet packet) {
        this.sendToPlayer(player, packet, this.createFriendlyByteBuf());
    }

    @Override
    public void sendToPlayer(@NotNull ServerPlayer player, @NotNull Packet packet, @NotNull FriendlyByteBuf buf) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(packet);
        Preconditions.checkNotNull(buf);
        this.encodePacket(packet, buf);
        ClientboundCustomPayloadPacket customPacket = new ClientboundCustomPayloadPacket(this.getId(packet), buf);
        player.connection.send(customPacket);
    }

    public void sendToAll(@NotNull MinecraftServer server, @NotNull Packet packet) {
        Preconditions.checkNotNull(server);
        Preconditions.checkNotNull(packet);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            this.sendToPlayer(player, packet);
        }
    }

    public void sendToLevel(@NotNull ServerLevel level, @NotNull Packet packet) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(packet);
        for (ServerPlayer player : level.players()) {
            this.sendToPlayer(player, packet);
        }
    }

    public void sendToTracking(@NotNull Entity entity, @NotNull Packet packet) {
        Preconditions.checkNotNull(entity);
        Preconditions.checkNotNull(packet);
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                if (serverLevel.getChunkSource().chunkMap.getPlayers(entity.chunkPosition(), false).contains(player)) {
                    this.sendToPlayer(player, packet);
                }
            }
        }
    }

    public void sendToAround(@NotNull ServerLevel level, @NotNull Vec3 pos, double radius, @NotNull Packet packet) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(pos);
        Preconditions.checkNotNull(packet);
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.x, pos.y, pos.z) <= radiusSqr) {
                this.sendToPlayer(player, packet);
            }
        }
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
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}