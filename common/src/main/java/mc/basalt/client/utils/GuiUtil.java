package mc.basalt.client.utils;

import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI 绘制小工具（Mojang 映射下 GuiGraphics 无直接边框方法）。
 */
public final class GuiUtil {
    private GuiUtil() {
    }

    /** 绘制 1px 边框，坐标语义同 fill：覆盖 [x1, x2) × [y1, y2)。 */
    public static void border(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y1 + 1, color);      // 上
        graphics.fill(x1, y2 - 1, x2, y2, color);      // 下
        graphics.fill(x1, y1, x1 + 1, y2, color);      // 左
        graphics.fill(x2 - 1, y1, x2, y2, color);      // 右
    }
}
