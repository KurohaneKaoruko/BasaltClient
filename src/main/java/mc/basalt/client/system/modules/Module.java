package mc.basalt.client.system.modules;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.system.settings.Setting;
import mc.basalt.client.ui.notifications.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Module {
    protected final MinecraftClient mc = MinecraftClient.getInstance();
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

    public void enable() {
        if (enabled) {
            return;
        }
        enabled = true;
        EventBus.subscribe(this);
        onEnable();
        NotificationManager.success(getDisplayName(), "已开启");
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        enabled = false;
        EventBus.unsubscribe(this);
        onDisable();
        NotificationManager.show(getDisplayName(), "已关闭", NotificationManager.Type.INFO, 1500);
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
            String localized = InputUtil.fromKeyCode(key, -1).getLocalizedText().getString();
            if (localized == null || localized.isBlank()) {
                return "KEY_" + key;
            }
            return localized.toUpperCase(Locale.ROOT);
        } catch (Exception ignored) {
            return "KEY_" + key;
        }
    }
}
