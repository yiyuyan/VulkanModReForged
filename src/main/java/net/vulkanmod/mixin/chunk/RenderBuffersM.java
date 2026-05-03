package net.vulkanmod.mixin.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.RenderBuffers;
//import net.minecraft.client.renderer.SectionBufferBuilderPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderBuffers.class)
public class RenderBuffersM {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;<init>(I)V"))
    private void red2(BufferBuilder instance, int i) {

    }
}
