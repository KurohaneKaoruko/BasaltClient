package mc.basalt.client.system.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.hud.HudManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 通过 Platform 抽象获取游戏目录（Fabric / NeoForge 通用）。 */
    private static File configFile() {
        return new File(BasaltClient.PLATFORM.getGameDir().toFile(), "basalt/config.json");
    }

    public static void save() {
        JsonObject json = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for (Module module : Modules.get().getAll()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", module.getKey());
            
            JsonObject settingsJson = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    settingsJson.addProperty(setting.getName(), ((BooleanSetting) setting).get());
                } else if (setting instanceof DoubleSetting) {
                    settingsJson.addProperty(setting.getName(), ((DoubleSetting) setting).get());
                } else if (setting instanceof ModeSetting) {
                    settingsJson.addProperty(setting.getName(), ((ModeSetting) setting).get());
                }
            }
            moduleJson.add("settings", settingsJson);
            
            modulesJson.add(module.getName(), moduleJson);
        }

        json.add("modules", modulesJson);

        // HUD 布局（锚点 + 偏移，分辨率无关）
        JsonObject hudJson = new JsonObject();
        for (HudElement element : HudManager.get().getElements()) {
            JsonObject elementJson = new JsonObject();
            elementJson.addProperty("anchor", element.getAnchor().name());
            elementJson.addProperty("offsetX", element.getOffsetX());
            elementJson.addProperty("offsetY", element.getOffsetY());
            elementJson.addProperty("enabled", element.isEnabled());
            hudJson.add(element.getName(), elementJson);
        }
        json.add("hud", hudJson);

        try {
            if (!configFile().getParentFile().exists()) {
                configFile().getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile())) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!configFile().exists()) return;

        try (FileReader reader = new FileReader(configFile())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("modules")) {
                JsonObject modulesJson = json.getAsJsonObject("modules");
                for (Module module : Modules.get().getAll()) {
                    if (modulesJson.has(module.getName())) {
                        JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());
                        
                        boolean shouldEnable = moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean();
                        module.setEnabled(shouldEnable);
                        
                        // Load Keybind
                        if (moduleJson.has("key")) {
                            module.setKey(moduleJson.get("key").getAsInt());
                        }
                        
                        // Load Settings
                        if (moduleJson.has("settings")) {
                            JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                            for (Setting<?> setting : module.getSettings()) {
                                if (settingsJson.has(setting.getName())) {
                                    JsonElement value = settingsJson.get(setting.getName());
                                    try {
                                        if (setting instanceof BooleanSetting) {
                                            ((BooleanSetting) setting).set(value.getAsBoolean());
                                        } else if (setting instanceof DoubleSetting) {
                                            ((DoubleSetting) setting).set(value.getAsDouble());
                                        } else if (setting instanceof ModeSetting) {
                                            ((ModeSetting) setting).set(value.getAsString());
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HUD 布局（锚点 + 偏移）
            if (json.has("hud")) {
                JsonObject hudJson = json.getAsJsonObject("hud");
                for (HudElement element : HudManager.get().getElements()) {
                    if (!hudJson.has(element.getName())) {
                        continue;
                    }
                    try {
                        JsonObject elementJson = hudJson.getAsJsonObject(element.getName());
                        if (elementJson.has("anchor")) {
                            element.setAnchor(HudElement.Anchor.valueOf(elementJson.get("anchor").getAsString()));
                        }
                        if (elementJson.has("offsetX") && elementJson.has("offsetY")) {
                            element.setOffsetX(elementJson.get("offsetX").getAsInt());
                            element.setOffsetY(elementJson.get("offsetY").getAsInt());
                        }
                        if (elementJson.has("enabled")) {
                            element.setEnabled(elementJson.get("enabled").getAsBoolean());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
