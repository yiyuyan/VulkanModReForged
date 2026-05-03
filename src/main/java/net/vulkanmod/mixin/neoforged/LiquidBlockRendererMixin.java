package net.vulkanmod.mixin.neoforged;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.vulkanmod.render.chunk.util.neoforged.FluidSpriteCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {
    @Inject(method = "setupSprites",at = @At("TAIL"))
    public void setup(CallbackInfo ci){
        FluidSpriteCache.reload();
    }
}
