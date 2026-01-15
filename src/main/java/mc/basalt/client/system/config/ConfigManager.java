package mc.basalt.client.system.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getGameDir().toFile(), "basalt/config.json");

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

        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) return;

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("modules")) {
                JsonObject modulesJson = json.getAsJsonObject("modules");
                for (Module module : Modules.get().getAll()) {
                    if (modulesJson.has(module.getName())) {
                        JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());
                        
                        // Load Enabled State
                        if (moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean()) {
                            module.setEnabled(true);
                        } else {
                            module.setEnabled(false);
                        }
                        
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
                                    if (setting instanceof BooleanSetting) {
                                        ((BooleanSetting) setting).set(value.getAsBoolean());
                                    } else if (setting instanceof DoubleSetting) {
                                        ((DoubleSetting) setting).set(value.getAsDouble());
                                    } else if (setting instanceof ModeSetting) {
                                        ((ModeSetting) setting).set(value.getAsString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
