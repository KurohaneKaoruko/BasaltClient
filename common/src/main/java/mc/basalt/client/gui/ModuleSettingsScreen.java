package mc.basalt.client.gui;

import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;
import mc.basalt.client.system.config.PresetManager;
import mc.basalt.client.ui.theme.Lang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModuleSettingsScreen extends Screen {
    private final Module module;
    private final Screen parent;
    private final boolean startBinding;

    // 主题色（与 ThemeManager 同步）
    private static int accent() {
        return mc.basalt.client.ui.theme.ThemeManager.get().accent();
    }

    private static final int WINDOW_BG = new Color(18, 18, 22, 245).getRGB();
    private static final int BTN_BG = new Color(30, 30, 36, 255).getRGB();
    private static final int BTN_BG_HOVER = new Color(42, 42, 50, 255).getRGB();
    private static final int BORDER = new Color(70, 70, 78, 255).getRGB();

    private static final int PADDING = 12;
    private static final int ITEM_GAP = 4;
    private static final int ROW_GAP = 6;
    private static final int BTN_H = 20;

    private final List<Row> rows = new ArrayList<>();
    private int windowWidth;
    private int windowHeight;
    private int windowX;
    private int windowY;

    private boolean waitingForBind;
    private BasaltButton keyBindButton;

    private record Row(AbstractWidget widget, int baseY) {
    }

    public ModuleSettingsScreen(Module module, Screen parent) {
        this(module, parent, false);
    }

    public ModuleSettingsScreen(Module module, Screen parent, boolean startBinding) {
        super(Component.literal(module.getDisplayName()));
        this.module = module;
        this.parent = parent;
        this.startBinding = startBinding;
    }

    @Override
    protected void init() {
        rows.clear();
        waitingForBind = false;

        List<Setting<?>> settings = module.getSettings().stream()
                .filter(Setting::isVisible)
                .toList();

        // ===== 预计算所有控件及其偏好宽度 =====
        List<WidgetSpec> specs = new ArrayList<>();

        // 状态 + 按键（流式排列）
        specs.add(new WidgetSpec("toggle", Lang.get("状态: 开启", "State: ON"), 0));
        specs.add(new WidgetSpec("bind", Lang.get("按键: ", "Bind: ") + module.getKeyName(), 0));

        // 设置项（流式排列）
        for (Setting<?> setting : settings) {
            if (setting instanceof BooleanSetting) {
                specs.add(new WidgetSpec("bool:" + setting.getName(),
                        setting.getName() + ": " + Lang.get("开", "ON"), 0));
            } else if (setting instanceof ModeSetting ms) {
                specs.add(new WidgetSpec("mode:" + setting.getName(),
                        setting.getName() + ": " + ms.get(), 0));
            } else if (setting instanceof DoubleSetting) {
                specs.add(new WidgetSpec("slider:" + setting.getName(), setting.getName(), 1)); // 滑条偏好独占
            }
        }

        // ===== 流式布局计算 =====
        int maxWindowW = Math.min(480, this.width - 10);
        int maxContentW = maxWindowW - PADDING * 2;

        // 计算每个控件的理想宽度（紧凑，只比文字多 16px 边距）
        List<Integer> widths = new ArrayList<>();
        for (WidgetSpec spec : specs) {
            int textW = this.font.width(spec.label) + 20; // 文字 + 左右内边距
            if (spec.type.startsWith("slider:")) {
                widths.add(-1); // 滑条标记，稍后用实际宽度
            } else {
                widths.add(Math.min(textW, maxContentW));
            }
        }

        // 先尝试全部放在一行，算出实际需要的宽度
        int totalNeeded = 0;
        int nonSliderCount = 0;
        for (int i = 0; i < widths.size(); i++) {
            if (widths.get(i) == -1) continue; // 滑条
            totalNeeded += widths.get(i) + ITEM_GAP;
            nonSliderCount++;
        }
        if (nonSliderCount > 0) totalNeeded -= ITEM_GAP;

        // 实际内容宽度：所有非滑条控件一行的宽度（上限 maxContentW）
        int actualContentW = Math.min(Math.max(220, totalNeeded), maxContentW);

        // 滑条用实际内容宽
        for (int i = 0; i < widths.size(); i++) {
            if (widths.get(i) == -1) widths.set(i, actualContentW);
        }

        // 贪心换行（"按键"后强制换行，固定操作独占一行）
        List<List<Integer>> lines = new ArrayList<>();
        int currentLineWidth = 0;
        List<Integer> currentLine = new ArrayList<>();

        for (int i = 0; i < specs.size(); i++) {
            boolean isSlider = specs.get(i).type.startsWith("slider:");
            boolean breaksAfter = specs.get(i).type.equals("bind");
            int w = widths.get(i);

            if (!isSlider && currentLineWidth + w + ITEM_GAP <= actualContentW) {
                currentLine.add(i);
                currentLineWidth += w + ITEM_GAP;
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = new ArrayList<>();
                currentLine.add(i);
                currentLineWidth = w + ITEM_GAP;
            }

            // "按键"后强制换行
            if (breaksAfter && !currentLine.isEmpty()) {
                lines.add(currentLine);
                currentLine = new ArrayList<>();
                currentLineWidth = 0;
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine);

        // ===== 同行等宽：无条件下统一为该行最大宽度 =====
        for (List<Integer> line : lines) {
            if (line.size() <= 1) continue;
            int maxW = 0;
            for (int idx : line) {
                if (widths.get(idx) > 0) maxW = Math.max(maxW, widths.get(idx));
            }
            for (int idx : line) {
                if (widths.get(idx) > 0) widths.set(idx, maxW);
            }
        }

        // ===== 全局等宽：如果所有行都可以等宽（按钮总数≤10），统一全局 =====
        {
            int globalMax = 0;
            int btnCount = 0;
            for (int i = 0; i < widths.size(); i++) {
                if (specs.get(i).type.startsWith("slider:") || widths.get(i) <= 0) continue;
                globalMax = Math.max(globalMax, widths.get(i));
                btnCount++;
            }
            if (btnCount > 1 && btnCount <= 10 && globalMax > 0) {
                for (int i = 0; i < widths.size(); i++) {
                    if (!specs.get(i).type.startsWith("slider:") && widths.get(i) > 0) {
                        widths.set(i, globalMax);
                    }
                }
            }
        }

        // 计算窗口尺寸：宽度根据实际内容收缩
        int maxLineWidth = 0;
        for (List<Integer> line : lines) {
            int lw = 0;
            for (int idx : line) lw += widths.get(idx) + ITEM_GAP;
            maxLineWidth = Math.max(maxLineWidth, lw - ITEM_GAP);
        }
        int actualContentW2 = Math.max(200, maxLineWidth);
        this.windowWidth = Math.min(actualContentW2 + PADDING * 2, this.width - 10);

        int totalHeight = 34; // 标题 + 分隔线
        for (var ignored : lines) totalHeight += BTN_H + ROW_GAP;
        totalHeight += BTN_H + 16; // 底部操作行

        this.windowHeight = Math.min(totalHeight, this.height - 10);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;

        // 重新计算实际可用宽度（基于收缩后的窗口）
        int availW = this.windowWidth - PADDING * 2;

        // ===== 每行均分：行内按钮等宽填满整行，不留空隙 =====
        for (List<Integer> line : lines) {
            int count = line.size();
            if (count <= 0) continue;
            // 每个按钮分到的宽度 = (可用宽度 - 间距) / 数量
            int eachW = (availW - (count - 1) * ITEM_GAP) / count;
            for (int idx : line) {
                widths.set(idx, eachW);
            }
        }

        // ===== 创建控件 =====
        int y = windowY + 34;

        for (List<Integer> line : lines) {
            int lineCount = line.size();
            int x = windowX + PADDING;

            for (int colIdx = 0; colIdx < lineCount; colIdx++) {
                int idx = line.get(colIdx);
                WidgetSpec spec = specs.get(idx);
                int w = widths.get(idx);

                AbstractWidget widget = createWidget(spec, x, y, w);
                if (widget != null) {
                    this.addRenderableWidget(widget);
                    rows.add(new Row(widget, y));
                }

                x += w + (colIdx < lineCount - 1 ? ITEM_GAP : 0);
            }
            y += BTN_H + ROW_GAP;
        }

        // ===== 底部固定操作行（重置 + 返回）=====
        int footerY = windowY + windowHeight - BTN_H - 8;
        int footerW = (windowWidth - PADDING * 2 - ITEM_GAP) / 2;
        BasaltButton resetBtn = new BasaltButton(windowX + PADDING, footerY, footerW, BTN_H,
                Component.literal(Lang.get("重置", "Reset")), btn -> resetDefaults());
        this.addRenderableWidget(resetBtn);
        rows.add(new Row(resetBtn, footerY));

        BasaltButton backBtn = new BasaltButton(windowX + PADDING + footerW + ITEM_GAP, footerY, footerW, BTN_H,
                Component.literal(Lang.get("返回", "Back")), btn -> onClose());
        this.addRenderableWidget(backBtn);
        rows.add(new Row(backBtn, footerY));

        if (startBinding) {
            waitingForBind = true;
            updateKeyBindButtonLabel();
        }
    }

    private record WidgetSpec(String type, String label, int flag) {
    }

    private AbstractWidget createWidget(WidgetSpec spec, int x, int y, int w) {
        String type = spec.type;

        if (type.equals("toggle")) {
            return new BasaltButton(x, y, w, BTN_H,
                    Component.literal(module.isEnabled() ? Lang.get("状态: 开启", "State: ON") : Lang.get("状态: 关闭", "State: OFF")),
                    btn -> {
                        module.toggle();
                        btn.setMessage(Component.literal(module.isEnabled() ? Lang.get("状态: 开启", "State: ON") : Lang.get("状态: 关闭", "State: OFF")));
                    });
        }
        if (type.equals("bind")) {
            keyBindButton = new BasaltButton(x, y, w, BTN_H, Component.empty(), btn -> {
                waitingForBind = !waitingForBind;
                updateKeyBindButtonLabel();
            });
            updateKeyBindButtonLabel();
            return keyBindButton;
        }

        // 设置项
        String settingName = type.substring(type.indexOf(':') + 1);
        Setting<?> setting = null;
        for (Setting<?> s : module.getSettings()) {
            if (s.getName().equals(settingName)) { setting = s; break; }
        }
        if (setting == null) return null;

        if (setting instanceof BooleanSetting bool) {
            return new BasaltButton(x, y, w, BTN_H,
                    Component.literal(bool.getName() + ": " + (bool.get() ? Lang.get("开", "ON") : Lang.get("关", "OFF"))),
                    btn -> {
                        bool.toggle();
                        PresetManager.invalidateActive();
                        btn.setMessage(Component.literal(bool.getName() + ": " + (bool.get() ? Lang.get("开", "ON") : Lang.get("关", "OFF"))));
                    });
        }
        if (setting instanceof ModeSetting mode) {
            return new BasaltButton(x, y, w, BTN_H,
                    Component.literal(mode.getName() + ": " + mode.get()),
                    btn -> {
                        mode.cycle();
                        PresetManager.invalidateActive();
                        btn.setMessage(Component.literal(mode.getName() + ": " + mode.get()));
                    });
        }
        if (setting instanceof DoubleSetting dbl) {
            return new BasaltSlider(x, y, w, BTN_H, dbl);
        }
        return null;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xB0000000);
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        border(context, windowX, windowY, windowWidth, windowHeight, accent());
        context.drawString(this.font, this.title, windowX + PADDING, windowY + 12, 0xFFFFFFFF);
        context.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, accent());

        super.render(context, mouseX, mouseY, delta);

        if (waitingForBind) {
            context.drawCenteredString(this.font,
                    Component.literal(Lang.get("按任意键 (ESC 清除)", "Any key (ESC clear)")),
                    windowX + windowWidth / 2, windowY + windowHeight - 12, 0xFF909090);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                module.setKey(-1);
            } else {
                module.setKey(keyCode);
            }
            waitingForBind = false;
            updateKeyBindButtonLabel();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private void updateKeyBindButtonLabel() {
        if (keyBindButton == null) return;
        keyBindButton.setMessage(Component.literal(
                waitingForBind ? Lang.get("按键: <任意>", "Bind: <any>")
                        : Lang.get("按键: ", "Bind: ") + module.getKeyName()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void resetDefaults() {
        for (Setting<?> setting : module.getSettings()) {
            ((Setting) setting).set(setting.getDefaultValue());
        }
        init();
    }

    @Override public void renderBackground(GuiGraphics c, int mX, int mY, float d) {}
    @Override public boolean isPauseScreen() { return false; }

    private static void border(GuiGraphics ctx, int x, int y, int w, int h, int c) {
        ctx.fill(x, y, x + w, y + 1, c);
        ctx.fill(x, y + h - 1, x + w, y + h, c);
        ctx.fill(x, y, x + 1, y + h, c);
        ctx.fill(x + w - 1, y, x + w, y + h, c);
    }

    // ===== 按钮 =====
    private class BasaltButton extends Button {
        BasaltButton(int x, int y, int w, int h, Component msg, Button.OnPress onPress) {
            super(x, y, w, h, msg, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics ctx, int mx, int my, float d) {
            boolean h = isHovered();
            ctx.fill(getX(), getY(), getX() + width, getY() + height, h ? BTN_BG_HOVER : BTN_BG);
            border(ctx, getX(), getY(), width, height, h ? accent() : BORDER);
            String text = getMessage().getString();
            int tw = ModuleSettingsScreen.this.font.width(text);
            int tx = getX() + Math.max(4, (width - tw) / 2);
            ctx.drawString(ModuleSettingsScreen.this.font, text, tx, getY() + (height - 8) / 2,
                    h ? 0xFFFFFFFF : 0xFFC8C8D0);
        }
    }

    // ===== 滑条 =====
    private class BasaltSlider extends AbstractWidget {
        private final DoubleSetting setting;
        private boolean dragging;

        BasaltSlider(int x, int y, int w, int h, DoubleSetting s) {
            super(x, y, w, h, Component.empty());
            this.setting = s;
        }

        @Override
        protected void renderWidget(GuiGraphics ctx, int mx, int my, float d) {
            ctx.fill(getX(), getY(), getX() + width, getY() + height, BTN_BG);
            double range = setting.getMax() - setting.getMin();
            double pct = range <= 0 ? 0 : (setting.get() - setting.getMin()) / range;
            int fill = (int) (width * Mth.clamp(pct, 0, 1));
            ctx.fill(getX(), getY(), getX() + fill, getY() + height, accent());

            String text = setting.getName() + ": " + String.format(Locale.ROOT, "%.1f", setting.get());
            int tw = ModuleSettingsScreen.this.font.width(text);
            ctx.drawString(ModuleSettingsScreen.this.font, text,
                    getX() + Math.max(4, (width - tw) / 2), getY() + (height - 8) / 2, 0xFFFFFFFF);
        }

        @Override public void onClick(double mx, double my) { dragging = true; apply(mx); }
        @Override public void onRelease(double mx, double my) { dragging = false; }
        @Override public void onDrag(double mx, double my, double dx, double dy) { if (dragging) apply(mx); }

        private void apply(double mx) {
            double pct = Mth.clamp((mx - getX()) / (double) width, 0, 1);
            double range = setting.getMax() - setting.getMin();
            setting.set(setting.getMin() + range * pct);
            PresetManager.invalidateActive();
        }

        @Override protected void updateWidgetNarration(NarrationElementOutput b) {}
    }
}
