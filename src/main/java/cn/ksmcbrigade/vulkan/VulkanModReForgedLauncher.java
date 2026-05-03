package cn.ksmcbrigade.vulkan;

import net.minecraftforge.fml.common.Mod;
import net.vulkanmod.Initializer;

@Mod(VulkanModReForgedLauncher.MOD_ID)
public final class VulkanModReForgedLauncher {
    public static final String MOD_ID = "vulkanmod";

    public VulkanModReForgedLauncher() {
        new Initializer().onInitializeClient();
    }
}
