package mc.basalt.client.system.modules;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.system.settings.Setting;
import mc.basalt.client.ui.notifications.NotificationManager;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private final String displayName;
    private final String description;
    private final Category category;
    private int key;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String displayName, String description, Category category) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.key = -1; // No key bound by default
    }

    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    /** 批量应用状态（预设加载等场景）：跳过逐模块通知与保存。 */
    public static boolean batchApplying;

    public void enable() {
        if (enabled) {
            return;
        }
        enabled = true;
        EventBus.subscribe(this);
        onEnable();
        if (!batchApplying) {
            NotificationManager.success(getDisplayName(), "已开启");
            saveIfInGame();
        }
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        enabled = false;
        EventBus.unsubscribe(this);
        onDisable();
        if (!batchApplying) {
            NotificationManager.show(getDisplayName(), "已关闭", NotificationManager.Type.INFO, 1500);
            saveIfInGame();
        }
    }

    /** 游戏内开关模块时立即持久化（配置加载阶段不触发）。 */
    private void saveIfInGame() {
        if (mc != null && mc.player != null) {
            ConfigManager.save();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            enable();
            return;
        }
        disable();
    }

    public String getKeyName() {
        if (key < 0) {
            return "NONE";
        }

        try {
            String localized = InputConstants.getKey(key, -1).getDisplayName().getString();
            if (localized == null || localized.isBlank()) {
                return "KEY_" + key;
            }
            return localized.toUpperCase(Locale.ROOT);
        } catch (Exception ignored) {
            return "KEY_" + key;
        }
    }
}
