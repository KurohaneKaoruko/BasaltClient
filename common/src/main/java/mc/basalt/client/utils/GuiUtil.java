package mc.basalt.client.utils;

import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI 绘制小工具（Mojang 映射下 GuiGraphics 无直接边框方法）。
 */
public final class GuiUtil {
    private GuiUtil() {
    }

    /**
     * 绘制 1px 边框。
     * 参数语义同原版 Yarn 的 drawBorder：(x, y) 为左上角，width/height 为尺寸。
     */
    public static void border(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);                    // 上
        graphics.fill(x, y + height - 1, x + width, y + height, color);  // 下
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);           // 左
        graphics.fill(x + width - 1, y + 1, x + width, y + height, color); // 右
    }
}
