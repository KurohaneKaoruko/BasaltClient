package mc.basalt.client;

import mc.basalt.client.gui.BasaltClickGUI;
import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.ui.hud.HudManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasaltClient implements ClientModInitializer {
    public static final String MOD_ID = "basalt-client";
    public static final ModMetadata MOD_METADATA;
    public static final String NAME;
    public static final String VERSION;
    public static final Logger LOG;

    static {
        MOD_METADATA = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        NAME = MOD_METADATA.getName();
        LOG = LoggerFactory.getLogger(MOD_ID);
        String ver = MOD_METADATA.getVersion().getFriendlyString();
        String ModVer = ver.split("-")[0];
        // String GameVer = ver.split("-")[1]; // Kept from original but unused in
        // original
        VERSION = ModVer;
    }

    public static BasaltClient INSTANCE;
    public static MinecraftClient MC;
    public static float TIMER = 1.0f;
    public static boolean FREECAM_ENABLED = false;
    public static boolean SAFEWALK_ENABLED = false;
    public static boolean FULLBRIGHT_ENABLED = false;
    public static boolean XRAY_ENABLED = false;
    public static double FREECAM_X, FREECAM_Y, FREECAM_Z;
    public static float FREECAM_YAW, FREECAM_PITCH;
    public static KeyBinding CLICK_GUI_KEY;
    public static KeyBinding HUD_EDITOR_KEY;
    private CommandManager commandManager;

    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
        }

        LOG.info("初始化 {}", NAME);

        MC = MinecraftClient.getInstance();

        // Initialize Modules
        Modules.get().init();

        // Initialize Commands
        commandManager = new CommandManager();

        // Initialize HUD
        HudManager.get().init();

        // Load Config
        ConfigManager.load();

        // Register Keybind (Right Shift)
        CLICK_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.basalt.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.basalt.client"));

        // Register HUD Editor Keybind (H)
        HUD_EDITOR_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.basalt.hudeditor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.basalt.client"));

        // Register Client Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CLICK_GUI_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(BasaltClickGUI.INSTANCE);
                } else if (client.currentScreen instanceof BasaltClickGUI) {
                    client.setScreen(null);
                }
            }

            while (HUD_EDITOR_KEY.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new HudEditorScreen());
                }
            }
        });

        // Save Config on Stop
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ConfigManager.save();
        });
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
