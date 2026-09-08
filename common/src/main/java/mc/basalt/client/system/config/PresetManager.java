package mc.basalt.client.system.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 配置预设：将当前模块开关与参数保存为命名预设，随时切换。
 * 存储于 <游戏目录>/basalt/presets/<名称>.json
 */
public final class PresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static String activePreset = null;

    private PresetManager() {
    }

    /** 当前激活的预设名（null = 未加载，或加载后配置已被修改）。 */
    public static String getActivePreset() {
        return activePreset;
    }

    /** 配置发生修改时调用：取消“当前预设”标记。 */
    public static void invalidateActive() {
        activePreset = null;
    }

    private static File presetsDir() {
        return new File(BasaltClient.PLATFORM.getGameDir().toFile(), "basalt/presets");
    }

    private static File presetFile(String name) {
        return new File(presetsDir(), name + ".json");
    }

    /** 校验预设名：字母/数字/下划线/连字符/中文，1-32 位。 */
    public static boolean isValidName(String name) {
        return name != null && name.matches("[\\w\\-\\u4e00-\\u9fa5]{1,32}");
    }

    public static List<String> list() {
        File dir = presetsDir();
        String[] names = dir.list((d, n) -> n.endsWith(".json"));
        if (names == null) {
            return List.of();
        }
        Arrays.sort(names);
        List<String> result = new ArrayList<>();
        for (String n : names) {
            result.add(n.substring(0, n.length() - 5));
        }
        return result;
    }

    /** 保存当前模块状态为预设。 */
    public static boolean save(String name) {
        JsonObject root = new JsonObject();
        root.addProperty("name", name);
        root.add("modules", captureModules());
        activePreset = name;

        try {
            File dir = presetsDir();
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }
            try (FileWriter writer = new FileWriter(presetFile(name))) {
                GSON.toJson(root, writer);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 应用预设：预设中包含的模块按预设状态设置，未包含的模块关闭。 */
    public static boolean apply(String name) {
        File file = presetFile(name);
        if (!file.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("modules")) {
                return false;
            }
            JsonObject modulesJson = root.getAsJsonObject("modules");

            Module.batchApplying = true;
            try {
                for (Module module : Modules.get().getAll()) {
                    if (modulesJson.has(module.getName())) {
                        JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());
                        module.setEnabled(moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean());
                        if (moduleJson.has("settings")) {
                            applySettings(module, moduleJson.getAsJsonObject("settings"));
                        }
                    } else {
                        module.setEnabled(false);
                    }
                }
            } finally {
                Module.batchApplying = false;
            }

            activePreset = name;
            ConfigManager.save();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(String name) {
        File file = presetFile(name);
        if (!file.exists()) {
            return false;
        }
        if (name.equals(activePreset)) {
            activePreset = null;
        }
        return file.delete();
    }

    /** 捕获当前全部模块的开关与设置。 */
    private static JsonObject captureModules() {
        JsonObject modulesJson = new JsonObject();
        for (Module module : Modules.get().getAll()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", module.getKey());
            moduleJson.add("settings", captureSettings(module));
            modulesJson.add(module.getName(), moduleJson);
        }
        return modulesJson;
    }

    private static JsonObject captureSettings(Module module) {
        JsonObject settingsJson = new JsonObject();
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                settingsJson.addProperty(setting.getName(), bool.get());
            } else if (setting instanceof DoubleSetting dbl) {
                settingsJson.addProperty(setting.getName(), dbl.get());
            } else if (setting instanceof ModeSetting mode) {
                settingsJson.addProperty(setting.getName(), mode.get());
            }
        }
        return settingsJson;
    }

    private static void applySettings(Module module, JsonObject settingsJson) {
        for (Setting<?> setting : module.getSettings()) {
            if (!settingsJson.has(setting.getName())) {
                continue;
            }
            try {
                if (setting instanceof BooleanSetting bool) {
                    bool.set(settingsJson.get(setting.getName()).getAsBoolean());
                } else if (setting instanceof DoubleSetting dbl) {
                    dbl.set(settingsJson.get(setting.getName()).getAsDouble());
                } else if (setting instanceof ModeSetting mode) {
                    mode.set(settingsJson.get(setting.getName()).getAsString());
                }
            } catch (Exception ignored) {
            }
        }
    }
}
