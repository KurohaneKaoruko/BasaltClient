package mc.basalt.client.ui.theme;

import java.awt.Color;

/**
 * 客户端主题系统：主色调、彩虹模式、模块列表样式、HUD 背景等个性化设置。
 * 由 ConfigManager 持久化，渲染层每帧读取。
 */
public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();

    /** 模块列表配色模式。 */
    public static final int LIST_ACCENT = 0;
    public static final int LIST_WHITE = 1;
    public static final int LIST_RAINBOW = 2;

    private int accentColor = 0xFFA060F0;
    private boolean rainbow = false;
    private float rainbowSpeed = 1.0f;
    private int listMode = LIST_ACCENT;
    private boolean hudBackground = true;
    private float hudBackgroundAlpha = 0.50f;

    private ThemeManager() {
    }

    public static ThemeManager get() {
        return INSTANCE;
    }

    /** 当前主题色（彩虹模式下返回循环色）。 */
    public int accent() {
        return rainbow ? chroma(0) : accentColor;
    }

    /** 静态主题色（不受彩虹模式影响）。 */
    public int accentStatic() {
        return accentColor;
    }

    /** 彩虹循环色，offset 用于错开相位。 */
    public int chroma(float offsetMs) {
        float cycle = 6000f / Math.max(0.1f, rainbowSpeed);
        float t = ((System.currentTimeMillis() + (long) offsetMs) % (long) cycle) / cycle;
        return 0xFF000000 | Color.HSBtoRGB(t, 0.65f, 1.0f);
    }

    /** 列表条目颜色（按列表配色模式）。 */
    public int listColor(int index) {
        return switch (listMode) {
            case LIST_WHITE -> 0xFFFFFFFF;
            case LIST_RAINBOW -> chroma(index * 120L);
            default -> accent();
        };
    }

    /** HUD 背景色（按背景开关与透明度）。 */
    public int hudBackground() {
        if (!hudBackground) {
            return 0;
        }
        int alpha = (int) (Math.max(0f, Math.min(1f, hudBackgroundAlpha)) * 255);
        return (alpha << 24);
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = 0xFF000000 | (accentColor & 0xFFFFFF);
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(float rainbowSpeed) {
        this.rainbowSpeed = Math.max(0.1f, Math.min(5.0f, rainbowSpeed));
    }

    public int getListMode() {
        return listMode;
    }

    public void setListMode(int listMode) {
        this.listMode = listMode;
    }

    public boolean isHudBackgroundEnabled() {
        return hudBackground;
    }

    public void setHudBackgroundEnabled(boolean hudBackground) {
        this.hudBackground = hudBackground;
    }

    public float getHudBackgroundAlpha() {
        return hudBackgroundAlpha;
    }

    public void setHudBackgroundAlpha(float hudBackgroundAlpha) {
        this.hudBackgroundAlpha = Math.max(0f, Math.min(1f, hudBackgroundAlpha));
    }
}
