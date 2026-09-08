package mc.basalt.client.platform;

import net.minecraft.client.KeyMapping;

import java.nio.file.Path;

/**
 * 加载器抽象层：Fabric / NeoForge 各自实现。
 * common 代码只通过此接口接触加载器专属功能。
 */
public interface Platform {
    String getLoaderName();

    String getModVersion();

    Path getGameDir();

    void registerKeyMapping(KeyMapping mapping);
}
