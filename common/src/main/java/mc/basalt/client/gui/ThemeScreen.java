package mc.basalt.client.gui;

import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.ui.notifications.NotificationManager;
import mc.basalt.client.ui.theme.Lang;
import mc.basalt.client.ui.theme.ThemeManager;
import mc.basalt.client.utils.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.util.function.IntConsumer;

/**
 * 主题设置界面：主色调取色器 + 预设色板 + 彩虹模式 + 列表样式 + HUD 背景设置。
 * 修改实时生效，退出自动保存。
 */
public class ThemeScreen extends Screen {
    private static final int WINDOW_BG = new Color(18, 18, 22, 245).getRGB();
    private static final int PANEL_BG = new Color(30, 30, 36, 255).getRGB();
    private static final int TEXT_DIM = 0xFF909090;

    private static final int[] PRESETS = {
            0xFFA060F0, 0xFF7C4DFF, 0xFF4D8CFF, 0xFF33C9A8, 0xFF5DD55D, 0xFFE8C33C,
            0xFFE8793C, 0xFFE85555, 0xFFE85FA8, 0xFF60CBE8, 0xFFB0B8C8, 0xFFF2F2F2
    };

    private static final int CONTENT_WIDTH = 250;
    private static final int SLIDER_H = 16;
    private static final int BTN_H = 18;
    private static final int ROW_GAP = 8;

    private final ThemeManager theme = ThemeManager.get();
    private final Screen parent;

    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<Label> labels = new ArrayList<>();
    private final List<Integer> sectionDividers = new ArrayList<>();

    private record Label(String text, int x, int y, int color) {
    }

    private int windowX;
    private int windowY;
    private int windowWidth = 310;
    private int windowHeight = 320;
    private int contentHeight;
    private int scrollOffset;

    public ThemeScreen(Screen parent) {
        super(Component.literal(Lang.get("主题设置", "Theme Settings")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.windowWidth = Math.min(310, this.width - 10);
        this.windowHeight = Math.min(320, this.height - 10);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        widgets.clear();
        labels.clear();
        sectionDividers.clear();

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int y = windowY + 44;

        // ===== 主色调 =====
        labels.add(new Label(Lang.get("主色调", "Accent color"), x, y, TEXT_DIM));
        y += 14;
        Color c = new Color(theme.getAccentColor());
        widgets.add(new Slider(x, y, CONTENT_WIDTH, SLIDER_H, Lang.get("红", "R"), c.getRed(), 0, 255, 1,
                (java.util.function.IntConsumer) v -> setRgb(v, null, null)));
        y += SLIDER_H + 4;
        widgets.add(new Slider(x, y, CONTENT_WIDTH, SLIDER_H, Lang.get("绿", "G"), c.getGreen(), 0, 255, 1,
                (java.util.function.IntConsumer) v -> setRgb(null, v, null)));
        y += SLIDER_H + 4;
        widgets.add(new Slider(x, y, CONTENT_WIDTH, SLIDER_H, Lang.get("蓝", "B"), c.getBlue(), 0, 255, 1,
                (java.util.function.IntConsumer) v -> setRgb(null, null, v)));
        y += SLIDER_H + 8;

        int cols = 6;
        int cell = CONTENT_WIDTH / cols;
        for (int i = 0; i < PRESETS.length; i++) {
            widgets.add(new Swatch(x + (i % cols) * cell + 2, y + (i / cols) * 22, cell - 6, 18, PRESETS[i]));
        }
        y += (PRESETS.length + cols - 1) / cols * 22 + 6;
        sectionDividers.add(y);
        y += ROW_GAP;

        // ===== 动态效果 =====
        labels.add(new Label(Lang.get("动态效果", "Effects"), x, y, TEXT_DIM));
        y += 14;
        widgets.add(new Toggle(x, y, CONTENT_WIDTH, BTN_H, Lang.get("彩虹模式", "Rainbow"), theme.isRainbow(), on -> {
            theme.setRainbow(on);
            rebuild();
        }));
        y += BTN_H + 4;
        widgets.add(new Slider(x, y, CONTENT_WIDTH, SLIDER_H, Lang.get("循环速度", "Cycle speed"),
                theme.getRainbowSpeed(), 0.2f, 4.0f, 0.1f, theme::setRainbowSpeed));
        y += SLIDER_H + 8;
        sectionDividers.add(y);
        y += ROW_GAP;

        // ===== 模块列表 =====
        labels.add(new Label(Lang.get("模块列表样式", "ArrayList style"), x, y, TEXT_DIM));
        y += 14;
        String[] modes = {Lang.get("主色", "Accent"), Lang.get("白色", "White"), Lang.get("彩虹", "Rainbow")};
        int third = CONTENT_WIDTH / 3;
        for (int i = 0; i < 3; i++) {
            int mode = i;
            widgets.add(new Toggle(x + i * third, y, third - 4, BTN_H, modes[i],
                    theme.getListMode() == i, on -> {
                        if (on) {
                            theme.setListMode(mode);
                        }
                        rebuild();
                    }));
        }
        y += BTN_H + 8;
        sectionDividers.add(y);
        y += ROW_GAP;

        // ===== HUD =====
        labels.add(new Label("HUD", x, y, TEXT_DIM));
        y += 14;
        widgets.add(new Toggle(x, y, CONTENT_WIDTH, BTN_H, Lang.get("列表背景", "List background"), theme.isHudBackgroundEnabled(), theme::setHudBackgroundEnabled));
        y += BTN_H + 4;
        widgets.add(new Slider(x, y, CONTENT_WIDTH, SLIDER_H, Lang.get("背景不透明度", "BG opacity"),
                theme.getHudBackgroundAlpha(), 0f, 1f, 0.05f, theme::setHudBackgroundAlpha));
        y += SLIDER_H + 10;

        contentHeight = y - (windowY + 44);
        for (AbstractWidget w : widgets) {
            this.addRenderableWidget(w);
        }
        clampScroll();
    }

    private int rgbR, rgbG, rgbB;

    private void setRgb(Integer r, Integer g, Integer b) {
        Color c = new Color(theme.getAccentColor());
        rgbR = r != null ? r : c.getRed();
        rgbG = g != null ? g : c.getGreen();
        rgbB = b != null ? b : c.getBlue();
        theme.setAccentColor(new Color(rgbR, rgbG, rgbB).getRGB());
    }

    private void clampScroll() {
        int view = windowHeight - 44 - 40;
        int maxScroll = Math.max(0, contentHeight - view);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);
        int top = windowY + 44;
        int bottom = windowY + windowHeight - 40;
        for (AbstractWidget w : widgets) {
            w.setY(w.getY()); // 保持构建位置
        }
        for (AbstractWidget w : widgets) {
            w.visible = w.getY() >= top - 2 && w.getY() + w.getHeight() <= bottom + 2;
        }
    }

    /** 禁用原版背景（毛玻璃）。 */
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xB0000000);

        // 窗口
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        GuiUtil.border(context, windowX, windowY, windowWidth, windowHeight, theme.accent());

        // 标题 + 当前主色预览
        context.drawString(this.font, this.title, windowX + 12, windowY + 12, 0xFFFFFFFF);
        context.fill(windowX + windowWidth - 26, windowY + 8, windowX + windowWidth - 10, windowY + 22, theme.accentStatic());
        GuiUtil.border(context, windowX + windowWidth - 26, windowY + 8, 16, 14, 0xFFFFFFFF);
        context.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, theme.accent());

        // 裁剪区域内绘制标签与分隔线
        int clipTop = windowY + 34;
        int clipBottom = windowY + windowHeight - 38;
        for (Label label : labels) {
            int ly = label.y() - scrollOffset;
            if (ly >= clipTop && ly <= clipBottom) {
                context.drawString(this.font, label.text(), label.x(), ly, label.color());
            }
        }
        for (int divider : sectionDividers) {
            int dy = divider - scrollOffset;
            if (dy >= clipTop && dy <= clipBottom) {
                context.fill(windowX + 10, dy, windowX + windowWidth - 10, dy + 1, 0x40FFFFFF);
            }
        }

        // 控件整体上移滚动量
        for (AbstractWidget w : widgets) {
            int originalY = w.getY();
            w.setY(originalY - scrollOffset);
        }
        super.render(context, mouseX, mouseY, delta);
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() + scrollOffset);
        }

        // 底部“完成”按钮
        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int by = windowY + windowHeight - 30;
        boolean hover = mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= by && mouseY <= by + 20;
        context.fill(x, by, x + CONTENT_WIDTH, by + 20, PANEL_BG);
        GuiUtil.border(context, x, by, CONTENT_WIDTH, 20, hover ? theme.accent() : 0xFF505058);
        String done = Lang.get("完成", "Done");
        context.drawString(this.font, done, x + (CONTENT_WIDTH - this.font.width(done)) / 2, by + 6, 0xFFFFFFFF);

        context.drawCenteredString(this.font, Lang.get("修改实时生效 · 滚轮翻页 · ESC 退出并保存", "Live · Scroll · ESC to save"),
                this.width / 2, this.height - 12, 0x707070);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int by = windowY + windowHeight - 30;
        if (button == 0 && mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= by && mouseY <= by + 20) {
            onClose();
            return true;
        }

        // 控件点击需要在滚动坐标下进行：临时下移
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() - scrollOffset);
        }
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() + scrollOffset);
        }
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() - scrollOffset);
        }
        boolean result = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() + scrollOffset);
        }
        return result;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() - scrollOffset);
        }
        super.mouseMoved(mouseX, mouseY);
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() + scrollOffset);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() - scrollOffset);
        }
        boolean result = super.mouseReleased(mouseX, mouseY, button);
        for (AbstractWidget w : widgets) {
            w.setY(w.getY() + scrollOffset);
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int view = windowHeight - 44 - 40;
        int maxScroll = Math.max(0, contentHeight - view);
        if (maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int) (verticalAmount * 20), 0, maxScroll);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        NotificationManager.success("主题", "设置已保存");
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** 浮点 setter（标准库无 ThemeScreen.FloatSetter）。 */
    @FunctionalInterface
    interface FloatSetter {
        void accept(float value);
    }

    /** 预设色块。 */
    private class Swatch extends AbstractWidget {
        private final int color;

        Swatch(int x, int y, int width, int height, int color) {
            super(x, y, width, height, Component.empty());
            this.color = color;
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.fill(getX(), getY(), getX() + this.width, getY() + this.height, color);
            boolean selected = theme.getAccentColor() == color;
            GuiUtil.border(context, getX(), getY(), this.width, this.height,
                    selected || isHovered() ? 0xFFFFFFFF : 0x50000000);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            theme.setAccentColor(color);
            rebuild();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    /** 开关按钮。 */
    private class Toggle extends AbstractWidget {
        private final Consumer<Boolean> onToggle;
        private boolean state;

        Toggle(int x, int y, int width, int height, String label, boolean on, Consumer<Boolean> onToggle) {
            super(x, y, width, height, Component.literal(label));
            this.onToggle = onToggle;
            this.state = on;
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.fill(getX(), getY(), getX() + this.width, getY() + this.height, PANEL_BG);
            GuiUtil.border(context, getX(), getY(), this.width, this.height, state ? theme.accent() : 0xFF505058);

            String text = getMessage().getString() + (state ? ": 开" : ": 关");
            int tw = ThemeScreen.this.font.width(text);
            context.drawString(ThemeScreen.this.font, text, getX() + (this.width - tw) / 2, getY() + 5,
                    state ? 0xFFFFFFFF : 0xFFB0B0B8);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            state = !state;
            onToggle.accept(state);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }

    /** 通用滑条（整数与浮点两用）。 */
    private class Slider extends AbstractWidget {
        private final String label;
        private final IntConsumer intSetter;
        private final ThemeScreen.FloatSetter floatSetter;
        private final int intMin;
        private final int intMax;
        private final float floatMin;
        private final float floatMax;
        private final float floatStep;
        private boolean dragging;
        private int intValue;
        private float floatValue;

        Slider(int x, int y, int width, int height, String label, int value, int min, int max, int step, IntConsumer setter) {
            super(x, y, width, height, Component.empty());
            this.label = label;
            this.intSetter = setter;
            this.floatSetter = null;
            this.intMin = min;
            this.intMax = max;
            this.floatMin = 0;
            this.floatMax = 1;
            this.floatStep = 0.05f;
            this.intValue = value;
        }

        Slider(int x, int y, int width, int height, String label, float value, float min, float max, float step, ThemeScreen.FloatSetter setter) {
            super(x, y, width, height, Component.empty());
            this.label = label;
            this.intSetter = null;
            this.floatSetter = setter;
            this.intMin = 0;
            this.intMax = 255;
            this.floatMin = min;
            this.floatMax = max;
            this.floatStep = step;
            this.floatValue = value;
        }

        private String display() {
            return intSetter != null
                    ? label + ": " + intValue
                    : label + ": " + String.format(java.util.Locale.ROOT, "%.2f", floatValue);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.fill(getX(), getY(), getX() + this.width, getY() + this.height, PANEL_BG);
            double percent = intSetter != null
                    ? (intValue - intMin) / (double) (intMax - intMin)
                    : (floatValue - floatMin) / (double) (floatMax - floatMin);
            int fill = (int) (this.width * Mth.clamp(percent, 0, 1));
            context.fill(getX(), getY(), getX() + fill, getY() + this.height, theme.accent());
            GuiUtil.border(context, getX(), getY(), this.width, this.height,
                    isHovered() || dragging ? 0xFFFFFFFF : 0xFF505058);

            String text = display();
            int tw = ThemeScreen.this.font.width(text);
            context.drawString(ThemeScreen.this.font, text, getX() + (this.width - tw) / 2, getY() + 4, 0xFFFFFFFF);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            dragging = true;
            apply(mouseX);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            dragging = false;
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            if (dragging) {
                apply(mouseX);
            }
        }

        private void apply(double mouseX) {
            double percent = Mth.clamp((mouseX - getX()) / (double) this.width, 0, 1);
            if (intSetter != null) {
                int v = (int) Math.round(intMin + percent * (intMax - intMin));
                if (v != intValue) {
                    intValue = v;
                    intSetter.accept(v);
                }
            } else {
                float v = floatMin + (float) percent * (floatMax - floatMin);
                v = Math.round(v / floatStep) * floatStep;
                floatValue = v;
                floatSetter.accept(v);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }
}
