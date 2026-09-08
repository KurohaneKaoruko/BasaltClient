package mc.basalt.client.ui.widget;

import mc.basalt.client.ui.theme.ThemeManager;
import mc.basalt.client.utils.GuiUtil;
import mc.basalt.client.utils.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 统一视觉语言的控件库（所有设置类界面共用，保证一致性）。
 * 视觉规范：面板底色 + 1px 边框；悬停亮边框；选中/激活为主题色；文字居中。
 */
public final class UiKit {
    private UiKit() {
    }

    /** 统一配色。 */
    public static final int BG_WINDOW = new java.awt.Color(18, 18, 22, 245).getRGB();
    public static final int BG_PANEL = new java.awt.Color(30, 30, 36, 255).getRGB();
    public static final int BG_PANEL_HOVER = new java.awt.Color(42, 42, 50, 255).getRGB();
    public static final int BORDER = new java.awt.Color(70, 70, 78, 255).getRGB();
    public static final int TEXT = 0xFFE8E8EC;
    public static final int TEXT_DIM = 0xFFB0B0B8;
    public static final int BTN_HEIGHT = 18;

    /** 绘制标准面板底。 */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, boolean hover) {
        g.fill(x, y, x + w, y + h, hover ? BG_PANEL_HOVER : BG_PANEL);
        GuiUtil.border(g, x, y, w, h, hover ? ThemeManager.get().accent() : BORDER);
    }

    /** 居中文字。 */
    public static void centerText(GuiGraphics g, String text, int x, int y, int w, int color) {
        Minecraft mc = Minecraft.getInstance();
        g.drawString(mc.font, text, x + (w - mc.font.width(text)) / 2, y, color);
    }

    /**
     * 操作按钮：labelSupplier 每帧取文案（可反映实时状态），
     * selectedSupplier 非空时以主题色高亮表示"选中"。
     */
    public static class ActionButton extends AbstractWidget {
        private final Supplier<String> label;
        private final Runnable onPress;
        private final Supplier<Boolean> selected;

        public ActionButton(int x, int y, int w, int h, Supplier<String> label, Runnable onPress) {
            this(x, y, w, h, label, onPress, null);
        }

        public ActionButton(int x, int y, int w, int h, Supplier<String> label, Runnable onPress, Supplier<Boolean> selected) {
            super(x, y, w, h, Component.empty());
            this.label = label;
            this.onPress = onPress;
            this.selected = selected;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            boolean sel = selected != null && selected.get();
            panel(g, getX(), getY(), this.width, this.height, isHovered() || sel);
            GuiUtil.border(g, getX(), getY(), this.width, this.height,
                    sel ? ThemeManager.get().accent() : (isHovered() ? ThemeManager.get().accent() : BORDER));
            centerText(g, label.get(), getX(), getY() + (this.height - 8) / 2, this.width,
                    sel || isHovered() ? 0xFFFFFFFF : TEXT_DIM);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onPress.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    /**
     * 开关按钮：内部维护布尔状态。
     */
    public static class ToggleButton extends AbstractWidget {
        private final Supplier<String> label;
        private final java.util.function.BooleanSupplier stateGetter;
        private final java.util.function.Consumer<Boolean> onChange;

        public ToggleButton(int x, int y, int w, int h, Supplier<String> label,
                java.util.function.BooleanSupplier stateGetter, java.util.function.Consumer<Boolean> onChange) {
            super(x, y, w, h, Component.empty());
            this.label = label;
            this.stateGetter = stateGetter;
            this.onChange = onChange;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            boolean state = stateGetter.getAsBoolean();
            panel(g, getX(), getY(), this.width, this.height, isHovered());
            GuiUtil.border(g, getX(), getY(), this.width, this.height, state ? ThemeManager.get().accent() : BORDER);
            String onText = mc.basalt.client.ui.theme.Lang.get("开", "ON");
            String offText = mc.basalt.client.ui.theme.Lang.get("关", "OFF");
            centerText(g, label.get() + ": " + (state ? onText : offText),
                    getX(), getY() + (this.height - 8) / 2, this.width, state ? 0xFFFFFFFF : TEXT_DIM);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onChange.accept(!stateGetter.getAsBoolean());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    /**
     * 浮点滑条：accent 填充 + 居中数值。
     */
    public static class Slider extends AbstractWidget {
        private final Supplier<String> label;
        private final java.util.function.DoubleSupplier valueGetter;
        private final java.util.function.DoubleConsumer valueSetter;
        private final double min;
        private final double max;
        private boolean dragging;

        public Slider(int x, int y, int w, int h, Supplier<String> label,
                double min, double max, java.util.function.DoubleSupplier valueGetter, java.util.function.DoubleConsumer valueSetter) {
            super(x, y, w, h, Component.empty());
            this.label = label;
            this.min = min;
            this.max = max;
            this.valueGetter = valueGetter;
            this.valueSetter = valueSetter;
        }

        private double value() {
            return Mth.clamp(valueGetter.getAsDouble(), min, max);
        }

        private String display() {
            double v = value();
            return label.get() + ": " + (v == Math.floor(v) && Math.abs(v) < 1000
                    ? String.valueOf((long) v)
                    : String.format(java.util.Locale.ROOT, "%.2f", v));
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            g.fill(getX(), getY(), getX() + this.width, getY() + this.height, BG_PANEL);
            double percent = (value() - min) / (max - min);
            int fill = (int) (this.width * Mth.clamp(percent, 0, 1));
            g.fill(getX(), getY(), getX() + fill, getY() + this.height, ThemeManager.get().accent());
            GuiUtil.border(g, getX(), getY(), this.width, this.height,
                    isHovered() || dragging ? ThemeManager.get().accent() : BORDER);
            centerText(g, display(), getX(), getY() + (this.height - 8) / 2, this.width, 0xFFFFFFFF);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            dragging = true;
            apply(mouseX);
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            if (dragging) {
                apply(mouseX);
            }
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            dragging = false;
        }

        private void apply(double mouseX) {
            double percent = Mth.clamp((mouseX - getX()) / (double) this.width, 0, 1);
            valueSetter.accept(min + percent * (max - min));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    /**
     * 色块按钮（预设色板用）：纯色填充 + 选中/悬停白边。
     */
    public static class Swatch extends AbstractWidget {
        private final int color;
        private final Supplier<Boolean> selected;
        private final Runnable onSelect;

        public Swatch(int x, int y, int w, int h, int color, Supplier<Boolean> selected, Runnable onSelect) {
            super(x, y, w, h, Component.empty());
            this.color = color;
            this.selected = selected;
            this.onSelect = onSelect;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float delta) {
            g.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            GuiUtil.border(g, getX(), getY(), this.width, this.height,
                    selected.get() || isHovered() ? 0xFFFFFFFF : 0x50000000);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onSelect.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }
}
