package com.skyblock21.mixin;

import com.skyblock21.events.BlockEvents;
import com.skyblock21.events.PacketEvents;
import net.minecraft.block.BlockState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "handlePacket", at = @At(value = "HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        boolean result = PacketEvents.RECEIVED.invoker().onPacketReceived(packet);

        if (!result) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V", at = @At(value = "HEAD"), cancellable = true)
    private void sendPacketNew(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        boolean result = PacketEvents.SENT.invoker().onPacketSent(packet);

        if (!result) {
            ci.cancel();
        }
    }
}
