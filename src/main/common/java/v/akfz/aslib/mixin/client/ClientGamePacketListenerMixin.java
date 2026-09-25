package v.akfz.aslib.mixin.client;

import v.akfz.aslib.network.AsLibNetworking;
import v.akfz.aslib.network.api.Packet;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientGamePacketListenerMixin {
    @Inject(
            method = {"handleCustomPayload"},
            at = {@At("HEAD")},
            cancellable = true
    )
    public void aslib$onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        ResourceLocation id = packet.getIdentifier();
        if (AsLibNetworking.REGISTRY.isPresent(id)) {
            FriendlyByteBuf buf = packet.getData();
            Packet decodedPacket = AsLibNetworking.REGISTRY.get(id).decoder().decode(buf);

            try {
                AsLibNetworking.HANDLER.handle(decodedPacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ci.cancel();
        }
    }
}
