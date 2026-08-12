package v.akfz.aslib.mixin;

import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.api.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerGamePacketListenerImpl.class})
public class SrvGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;
    @Inject(
            method = {"handleCustomPayload"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void aslib$onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        ResourceLocation id = packet.getIdentifier();
        if (AsLibNetworking.REGISTRY.isPresent(id)) {
            FriendlyByteBuf buf = packet.getData();
            Packet decodedPacket = AsLibNetworking.REGISTRY.get(id).decoder().decode(buf);
            ServerPlayer sender = this.player;

            try {
                AsLibNetworking.HANDLER.handle(decodedPacket, sender);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ci.cancel();
        }
    }
}
