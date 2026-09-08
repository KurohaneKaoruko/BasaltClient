package mc.basalt.client;

import com.mojang.blaze3d.platform.InputConstants;
import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.gui.BasaltClickGUI;
import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.platform.Platform;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.hud.HudManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端引导入口（加载器无关）。
 * 由各加载器的 Platform 适配器调用 {@link #init(Platform)}。
 */
public final class BasaltClient {
    public static final String MOD_ID = "basalt-client";
    public static final String NAME = "玄武岩CLIENT";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static BasaltClient INSTANCE;
    public static Minecraft MC;
    public static Platform PLATFORM;

    public static float TIMER = 1.0f;
    public static boolean FREECAM_ENABLED = false;
    public static boolean SAFEWALK_ENABLED = false;
    public static boolean FULLBRIGHT_ENABLED = false;
    public static boolean XRAY_ENABLED = false;
    public static double FREECAM_X, FREECAM_Y, FREECAM_Z;
    public static float FREECAM_YAW, FREECAM_PITCH;

    public static KeyMapping CLICK_GUI_KEY;
    public static KeyMapping HUD_EDITOR_KEY;

    private CommandManager commandManager;

    private BasaltClient() {
    }

    public static void init(Platform platform) {
        if (INSTANCE != null) {
            return;
        }

        PLATFORM = platform;
        INSTANCE = new BasaltClient();
        MC = Minecraft.getInstance();

        LOG.info("初始化 {} ({})", NAME, platform.getLoaderName());

        Modules.get().init();
        INSTANCE.commandManager = CommandManager.get();
        HudManager.get().init();
        ConfigManager.load();

        CLICK_GUI_KEY = new KeyMapping(
                "key.basalt.clickgui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.basalt.client");
        HUD_EDITOR_KEY = new KeyMapping(
                "key.basalt.hudeditor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.basalt.client");
        platform.registerKeyMapping(CLICK_GUI_KEY);
        platform.registerKeyMapping(HUD_EDITOR_KEY);

        // 按键轮询走自建事件（MixinMinecraft 注入的 tick 末尾）
        EventBus.subscribe(INSTANCE);
    }

    public static CommandManager commands() {
        return INSTANCE.commandManager;
    }

    public static String version() {
        return PLATFORM != null ? PLATFORM.getModVersion() : "dev";
    }

    /** GUI 显示用版本号：不带 MC 版本后缀（如 0.3.1+1.21.4 → 0.3.1）。 */
    public static String displayVersion() {
        String v = version();
        int plus = v.indexOf('+');
        return plus > 0 ? v.substring(0, plus) : v;
    }

    @EventHandler
    public void onTickEnd(TickEvent.Post event) {
        if (MC == null) {
            return;
        }

        while (CLICK_GUI_KEY.consumeClick()) {
            if (MC.screen == null) {
                MC.setScreen(BasaltClickGUI.INSTANCE);
            } else if (MC.screen instanceof BasaltClickGUI) {
                MC.setScreen(null);
            }
        }

        while (HUD_EDITOR_KEY.consumeClick()) {
            if (MC.screen == null) {
                MC.setScreen(new HudEditorScreen());
            }
        }
    }
}
