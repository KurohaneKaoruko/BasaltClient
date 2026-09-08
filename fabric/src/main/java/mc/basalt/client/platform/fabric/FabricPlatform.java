package mc.basalt.client.platform.fabric;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;

import java.nio.file.Path;

public class FabricPlatform implements ClientModInitializer, Platform {
    @Override
    public void onInitializeClient() {
        BasaltClient.init(this);
    }

    @Override
    public String getLoaderName() {
        return "Fabric";
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(BasaltClient.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        KeyBindingHelper.registerKeyBinding(mapping);
    }
}
