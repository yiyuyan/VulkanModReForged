package cn.ksmcbrigade.vulkan_core.services;

import cn.ksmcbrigade.vulkan_core.VKCUnsafeUtils;
import cn.ksmcbrigade.vulkan_core.mixintransmog.DummyMixinTransformationService;
import cn.ksmcbrigade.vulkan_core.mixintransmog.GeneratedMixinClassesSecureJar;
import cn.ksmcbrigade.vulkan_core.mixintransmog.InstrumentationHack;
import cn.ksmcbrigade.vulkan_core.mixintransmog.MixinModlauncherRemapper;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.TransformationServiceDecorator;
import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.MixinLaunchPluginLegacy;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static cn.ksmcbrigade.vulkan_core.mixintransmog.Constants.LOG;
import static cn.ksmcbrigade.vulkan_core.mixintransmog.MixinTransformationService.replaceMixinLaunchPlugin;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2026/5/3
 */
public class VulkanTransformationService implements ITransformationService {

    public static GeneratedMixinClassesSecureJar jar = new GeneratedMixinClassesSecureJar();

    public VulkanTransformationService(){
        LOG.info("Mixin Transmogrifier is definitely up to no good...");
        try {
            InstrumentationHack.inject();
        } catch (Throwable t) {
            LOG.error("Error replacing mixin module source", t);
            throw new RuntimeException(t);
        }
        replaceMixinLaunchPlugin();
        LOG.info("crimes against java were committed");
    }

    @Override
    public @NotNull String name() {
        return "vulkan_transformer_loader";
    }

    @Override
    public void initialize(IEnvironment environment) {
        try {
            LOG.debug("initialize called");
            LOG.info("hash: {}", MixinModlauncherRemapper.class.hashCode());

            var mixinBootstrapStartMethod = MixinBootstrap.class.getDeclaredMethod("start");
            mixinBootstrapStartMethod.setAccessible(true);

            Optional<ILaunchPluginService> plugin = environment.findLaunchPlugin("mixin");
            if (plugin.isEmpty()) {
                throw new Error("Mixin Launch Plugin Service could not be located");
            }
            ILaunchPluginService launchPlugin = plugin.get();
            if (!(launchPlugin instanceof MixinLaunchPluginLegacy)) {
                throw new Error("Mixin Launch Plugin Service is present but not compatible");
            }

            var mixinPluginInitMethod = MixinLaunchPluginLegacy.class.getDeclaredMethod("init", IEnvironment.class, List.class);
            mixinPluginInitMethod.setAccessible(true);

            // The actual init invocations
            mixinBootstrapStartMethod.invoke(null);
            mixinPluginInitMethod.invoke(launchPlugin, environment, List.of());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {
        System.out.println("VulkanTransformerLoader Loaded.");

        try {
            FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL,false);
            File file = new File("config/fml.toml");
            FMLConfig.updateConfig(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL,false);
            if(!file.exists()){
                FileUtils.writeStringToFile(file, """
                        #Early window height
                        earlyWindowHeight = 480
                        #Enable NeoForge global version checking
                        versionCheck = true
                        #Should we control the window. Disabling this disables new GL features and can be bad for mods that rely on them.
                        earlyWindowControl = false
                        #Early window framebuffer scale
                        earlyWindowFBScale = 1
                        #Disables File Watcher. Used to automatically update config if its file has been modified.
                        disableConfigWatcher = false
                        #Early window provider
                        earlyWindowProvider = "fmlearlywindow"
                        #Early window width
                        earlyWindowWidth = 854
                        #Early window starts maximized
                        earlyWindowMaximized = false
                        #Default config path for servers
                        defaultConfigPath = "defaultconfigs"
                        #Disables Optimized DFU client-side - already disabled on servers
                        disableOptimizedDFU = true
                        #Skip specific GL versions, may help with buggy graphics card drivers
                        earlyWindowSkipGLVersions = []
                        #Max threads for early initialization parallelism,  -1 is based on processor count
                        maxThreads = -1
                        #Squir?
                        earlyWindowSquir = false
                                            
                        """);
            }
            else{
                StringBuilder builder = new StringBuilder();
                for (String string : FileUtils.readFileToString(file).split("\n")) {
                    if(string.startsWith("earlyWindowControl")){
                        builder.append(string.replace("true","false"));
                    }
                    else{
                        builder.append(string);
                    }
                    builder.append("\n");
                }
                FileUtils.writeStringToFile(file,builder.toString());
            }
        } catch (IOException e) {
            System.out.println("[VulkanCore] Can't close the early window control.");
        }

        LOG.debug("onLoad called");
        LOG.debug(String.join(", ", otherServices));

        try {
            Field handlerField = Launcher.class.getDeclaredField("transformationServicesHandler");
            handlerField.setAccessible(true);
            Object handler = handlerField.get(Launcher.INSTANCE);
            Field serviceLookupField = handler.getClass().getDeclaredField("serviceLookup");
            serviceLookupField.setAccessible(true);
            Map<String, TransformationServiceDecorator> serviceLookup = (Map) serviceLookupField.get(handler);
            Constructor<TransformationServiceDecorator> ctr = TransformationServiceDecorator.class.getDeclaredConstructor(ITransformationService.class);
            ctr.setAccessible(true);
            TransformationServiceDecorator decorator = ctr.newInstance(new DummyMixinTransformationService());
            Method onLoad = TransformationServiceDecorator.class.getDeclaredMethod("onLoad", IEnvironment.class, Set.class);
            onLoad.setAccessible(true);
            onLoad.invoke(decorator, env, otherServices);
            // Silently replace service, avoiding a ConcurrentModificationException
            serviceLookup.put("mixin", decorator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public List<Resource> beginScanning(IEnvironment environment) {
        // Add mixin remapper after the naming service has been initialized
        if (!FMLEnvironment.production) {
            MixinEnvironment.getDefaultEnvironment().getRemappers().add(new MixinModlauncherRemapper());
        }
        return List.of(new Resource(IModuleLayerManager.Layer.GAME, List.of(jar)));
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
