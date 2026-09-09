package mc.basalt.client.ui.theme;

import mc.basalt.client.system.modules.Module;

import java.util.Map;

/**
 * 多语言助手：当前仅支持中文(zh)与英文(en)。
 * 用法：Lang.get("中文", "English")
 */
public final class Lang {
    public static final String ZH = "zh";
    public static final String EN = "en";

    /** 模块英文描述表（key = 模块注册名）。 */
    private static final Map<String, String> MODULE_DESCRIPTIONS_EN = Map.ofEntries(
            Map.entry("Killaura", "Automatically attacks the nearest target."),
            Map.entry("Velocity", "Cancels knockback and explosion motion."),
            Map.entry("AutoTotem", "Automatically swaps a totem to the offhand at low health."),
            Map.entry("AutoArmor", "Automatically equips the best armor."),
            Map.entry("AutoCrystal", "Automatically places and breaks end crystals."),
            Map.entry("Criticals", "Automatically performs critical hits."),
            Map.entry("AutoClicker", "Clicks automatically while holding attack."),
            Map.entry("TriggerBot", "Attacks automatically when the crosshair is on a target."),
            Map.entry("Flight", "Creative-style flight with adjustable speed."),
            Map.entry("Speed", "Move faster (vanilla bhop / strafe)."),
            Map.entry("NoFall", "Eliminates fall damage."),
            Map.entry("Step", "Step up blocks without jumping."),
            Map.entry("Jesus", "Walk on water."),
            Map.entry("Scaffold", "Automatically places blocks under you."),
            Map.entry("SafeWalk", "Prevents falling off edges while walking."),
            Map.entry("EntityControl", "Fly and steer riding entities."),
            Map.entry("Timer", "Changes the client tick speed."),
            Map.entry("AntiAFK", "Periodic actions to avoid AFK kick."),
            Map.entry("ESP", "Highlights entities through walls."),
            Map.entry("Tracers", "Draws tracer lines from screen center."),
            Map.entry("Nametags", "Through-wall nametags with health info."),
            Map.entry("Fullbright", "Permanent full brightness."),
            Map.entry("XRay", "Shows only ores."),
            Map.entry("Freecam", "Detached camera; your body stays put."),
            Map.entry("Zoom", "Lower FOV like binoculars while enabled."),
            Map.entry("NoWeather", "Removes rain and thunder client-side."),
            Map.entry("FastPlace", "Removes block place delay."),
            Map.entry("ChestStealer", "Automatically loots chests."),
            Map.entry("Blink", "Holds packets and sends them at once on disable."),
            Map.entry("AutoEat", "Automatically eats when hungry."),
            Map.entry("AutoRespawn", "Automatically respawns on death."),
            Map.entry("AutoTool", "Switches to the fastest tool when mining."),
            Map.entry("AutoShear", "Auto-shears nearby sheep with shears."),
            Map.entry("AutoDoor", "Opens doors and trap doors automatically."),
            Map.entry("AutoMount", "Auto-mounts rideable entities nearby."),
            Map.entry("AutoNametag", "Names namable entities with held name tag."),
            Map.entry("Nuker", "Breaks blocks in a configurable radius."),
            Map.entry("LiquidFiller", "Auto-fills liquids with blocks in range."),
            Map.entry("BonemealAura", "Uses bone meal on crops and saplings."),
            Map.entry("AutoFish", "Reels and recasts fishing rod automatically."),
            Map.entry("StashFinder", "Notifies when chunks contain many containers."),
            Map.entry("AutoReplant", "Re-harvests and replants crops automatically."));

    private Lang() {
    }

    public static boolean isEn() {
        return ThemeManager.get().getLanguage().equals(EN);
    }

    public static String get(String zh, String en) {
        return isEn() ? en : zh;
    }

    /** 模块显示名：英文模式使用模块注册名。 */
    public static String moduleDisplayName(Module module) {
        return isEn() ? module.getName() : module.getDisplayName();
    }

    /** 模块描述：英文模式查表，缺省回退中文。 */
    public static String moduleDescription(Module module) {
        if (isEn()) {
            String en = MODULE_DESCRIPTIONS_EN.get(module.getName());
            if (en != null) {
                return en;
            }
        }
        return module.getDescription();
    }
}
