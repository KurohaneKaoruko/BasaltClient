package mc.basalt.client.platform.neoforge;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.platform.Platform;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.nio.file.Path;

@Mod(NeoForgePlatform.MOD_ID)
public class NeoForgePlatform implements Platform {
    public static final String MOD_ID = "basaltclient";

    public NeoForgePlatform(IEventBus modEventBus) {
        // 仅物理客户端初始化（专用服务器不加载客户端类）
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        modEventBus.addListener(this::onRegisterKeyMappings);
        BasaltClient.init(this);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        if (BasaltClient.CLICK_GUI_KEY != null) {
            event.register(BasaltClient.CLICK_GUI_KEY);
        }
        if (BasaltClient.HUD_EDITOR_KEY != null) {
            event.register(BasaltClient.HUD_EDITOR_KEY);
        }
    }

    @Override
    public String getLoaderName() {
        return "NeoForge";
    }

    @Override
    public String getModVersion() {
        return BasaltClient.class.getPackage().getImplementationVersion() != null
                ? BasaltClient.class.getPackage().getImplementationVersion()
                : "dev";
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        // 注册统一在 RegisterKeyMappingsEvent 中完成
    }
}
