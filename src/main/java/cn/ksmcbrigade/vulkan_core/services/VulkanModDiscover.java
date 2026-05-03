package cn.ksmcbrigade.vulkan_core.services;

import cn.ksmcbrigade.vulkan_core.VKCUnsafeUtils;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/5/3
 */
public class VulkanModDiscover extends AbstractJarFileModLocator {

    @Override
    public Stream<Path> scanCandidates() {

        List<Path> pathList = new ArrayList<>();

        String os = System.getProperty("os.name");

        System.out.println("VulkanMod Library Discover loading...");
        System.out.println("OS: "+os);

        os = os.toLowerCase();
        if(os.contains("windows")) os = "windows";
        else if(os.contains("mac")) os = "macos";
        else os = "linux";

        File dir = new File("vulkan-libs");
        File[] files = dir.listFiles();
        if(files==null || files.length<13){
            File file = new File(RandomStringUtils.randomNumeric(8)+"-vulkan-tmp.zip");
            try {
                FileUtils.writeByteArrayToFile(file, IOUtils.toByteArray(Objects.requireNonNull(VulkanModDiscover.class.getResourceAsStream("/vulkan-libs.zip"))));
                try(ZipFile zipFile = new ZipFile(file)){
                    Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
                    while (entryEnumeration.hasMoreElements()){
                        ZipEntry entry = entryEnumeration.nextElement();
                        File jarFile = new File(entry.getName());
                        if(jarFile.isDirectory() || !jarFile.getName().toLowerCase().endsWith(".jar")) continue;
                        FileUtils.writeByteArrayToFile(jarFile,IOUtils.toByteArray(zipFile.getInputStream(entry)));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                file.delete();
            }
        }
        files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".jar"));
        if(files!=null){
            for (File file : files) {
                String name = file.getName();
                if(name.toLowerCase().contains("natives") && !name.contains(os)) continue;
                pathList.add(file.toPath());
            }
        }

        VKCUnsafeUtils.coexistenceCoreAndMod();

        return pathList.stream();
    }

    @Override
    public String name() {
        return "vkc_mod_provider";
    }

    @Override
    public void initArguments(Map<String, ?> map) {

    }
}
