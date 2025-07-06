package com.skyblock21.mixin;

import com.skyblock21.events.ChunkEvents;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {

    @Shadow
    @Final
    private ClientWorld world;

    @Inject(method = "loadChunkFromPacket", at = @At("RETURN"))
    private void onChunkLoad(int x, int z, PacketByteBuf buf, Map<Heightmap.Type, long[]> heightmaps, Consumer<ChunkData.BlockEntityVisitor> consumer, CallbackInfoReturnable<WorldChunk> cir) {
        WorldChunk chunk = cir.getReturnValue();
        if (chunk != null) {
            ChunkEvents.CHUNK_SPAWNED.invoker().onChunkSpawned(this.world, chunk);
        }
    }

    @Inject(method = "unload", at = @At("HEAD"))
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci) {
        WorldChunk chunk = this.world.getChunk(pos.x, pos.z);
        if (chunk != null) {
            ChunkEvents.CHUNK_REMOVED.invoker().onChunkRemoved(this.world, chunk);
        }
    }
}
