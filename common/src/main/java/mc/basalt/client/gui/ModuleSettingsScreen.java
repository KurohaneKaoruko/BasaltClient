package mc.basalt.client.gui;

import mc.basalt.client.utils.GuiUtil;

import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;
import mc.basalt.client.ui.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
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

    private static final int WINDOW_BG = new Color(20, 20, 20, 240).getRGB();
    private static final int BORDER_COLOR = new Color(60, 60, 60, 255).getRGB();
    private static final int ACCENT_COLOR = new Color(220, 220, 220, 255).getRGB();

    private static final int BUTTON_NORMAL_BG = new Color(30, 30, 30, 255).getRGB();
    private static final int BUTTON_HOVER_BG = new Color(50, 50, 50, 255).getRGB();
    private static final int BUTTON_BORDER = new Color(80, 80, 80, 255).getRGB();
    

    private static final int SLIDER_BG = new Color(40, 40, 40, 255).getRGB();
    

    private static final int CONTENT_WIDTH = 220;
    private static final int ROW_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_HEIGHT = 40;

    private final List<Row> rows = new ArrayList<>();

    private int windowWidth = 240;
    private int windowHeight = 200;
    private int windowX;
    private int windowY;
    private int contentTop;
    private int contentBottom;
    private int maxScroll;
    private int scrollOffset;

    private boolean waitingForBind;
    private BasaltButton keyBindButton;
    private BasaltButton toggleButton;

    private record Row(AbstractWidget widget, int baseY) {
    }

    public ModuleSettingsScreen(Module module, Screen parent) {
        this(module, parent, false);
    }

    public ModuleSettingsScreen(Module module, Screen parent, boolean startBinding) {
        super(Component.literal(module.getDisplayName() + " 设置"));
        this.module = module;
        this.parent = parent;
        this.startBinding = startBinding;
    }

    @Override
    protected void init() {
        rows.clear();
        scrollOffset = 0;

        List<Setting<?>> settings = module.getSettings().stream()
                .filter(Setting::isVisible)
                .toList();

        // 2 个固定行（状态 / 按键）+ 设置行 + 重置行
        int rowCount = settings.size() + 3;
        int desiredHeight = HEADER_HEIGHT + rowCount * ROW_HEIGHT + FOOTER_HEIGHT;
        this.windowHeight = Math.min(desiredHeight, Math.max(150, this.height - 16));
        this.windowWidth = Math.max(240, CONTENT_WIDTH + 20);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;

        this.contentTop = windowY + HEADER_HEIGHT;
        this.contentBottom = windowY + windowHeight - FOOTER_HEIGHT;
        this.maxScroll = Math.max(0, desiredHeight - HEADER_HEIGHT - FOOTER_HEIGHT - (contentBottom - contentTop));

        int contentStartX = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int baseY = windowY + HEADER_HEIGHT;

        this.toggleButton = new BasaltButton(
                contentStartX, baseY, CONTENT_WIDTH, 20,
                Component.literal(module.isEnabled() ? "状态: 开启" : "状态: 关闭"),
                button -> {
                    module.toggle();
                    button.setMessage(Component.literal(module.isEnabled() ? "状态: 开启" : "状态: 关闭"));
                });
        this.addRenderableWidget(toggleButton);
        rows.add(new Row(toggleButton, baseY));
        baseY += ROW_HEIGHT;

        this.keyBindButton = new BasaltButton(
                contentStartX, baseY, CONTENT_WIDTH, 20,
                Component.empty(),
                button -> {
                    waitingForBind = !waitingForBind;
                    updateKeyBindButtonLabel();
                });
        this.addRenderableWidget(keyBindButton);
        rows.add(new Row(keyBindButton, baseY));
        baseY += ROW_HEIGHT;
        updateKeyBindButtonLabel();

        for (Setting<?> setting : settings) {
            AbstractWidget widget;
            if (setting instanceof BooleanSetting boolSetting) {
                widget = new BasaltButton(
                        contentStartX, baseY, CONTENT_WIDTH, 20,
                        Component.literal(boolSetting.getName() + ": " + (boolSetting.get() ? "开" : "关")),
                        btn -> {
                            boolSetting.toggle();
                            btn.setMessage(Component.literal(boolSetting.getName() + ": " + (boolSetting.get() ? "开" : "关")));
                        });
            } else if (setting instanceof ModeSetting modeSetting) {
                widget = new BasaltButton(
                        contentStartX, baseY, CONTENT_WIDTH, 20,
                        Component.literal(modeSetting.getName() + ": " + modeSetting.get()),
                        btn -> {
                            modeSetting.cycle();
                            btn.setMessage(Component.literal(modeSetting.getName() + ": " + modeSetting.get()));
                        });
            } else if (setting instanceof DoubleSetting doubleSetting) {
                widget = new BasaltSlider(contentStartX, baseY, CONTENT_WIDTH, 20, doubleSetting);
            } else {
                continue;
            }
            this.addRenderableWidget(widget);
            rows.add(new Row(widget, baseY));
            baseY += ROW_HEIGHT;
        }

        BasaltButton resetButton = new BasaltButton(
                contentStartX, baseY, CONTENT_WIDTH, 20,
                Component.literal("重置默认设置"),
                button -> resetSettingsToDefaults());
        this.addRenderableWidget(resetButton);
        rows.add(new Row(resetButton, baseY));

        BasaltButton backButton = new BasaltButton(
                contentStartX, windowY + windowHeight - 30, CONTENT_WIDTH, 20,
                Component.literal("返回"),
                button -> onClose());
        this.addRenderableWidget(backButton);

        if (startBinding) {
            waitingForBind = true;
            updateKeyBindButtonLabel();
        }

        layoutRows();
    }

    /** 按滚动偏移重排行位置，并隐藏窗口外的控件。 */
    private void layoutRows() {
        for (Row row : rows) {
            int y = row.baseY() - scrollOffset;
            row.widget().setY(y);
            boolean visible = y >= contentTop - 2 && y + row.widget().getHeight() <= contentBottom + 2;
            row.widget().visible = visible;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 独立模态弹窗：不重绘父级界面，避免 1.21.2+ 毛玻璃/背景合成层级问题
        context.fill(0, 0, this.width, this.height, 0xB0000000);
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        GuiUtil.border(context, windowX, windowY, windowWidth, windowHeight, BORDER_COLOR);

        context.drawString(this.font, this.title, windowX + 10, windowY + 8, ACCENT_COLOR);

        // 模块描述（超长截断）
        String desc = this.font.plainSubstrByWidth(module.getDescription(), windowWidth - 20);
        context.drawString(this.font, Component.literal(desc), windowX + 10, windowY + 21, 0x909090);
        context.fill(windowX, windowY + 33, windowX + windowWidth, windowY + 34, BORDER_COLOR);

        super.render(context, mouseX, mouseY, delta);

        // 滚动条
        int contentHeight = contentBottom - contentTop;
        int totalHeight = maxScroll + contentHeight;
        if (totalHeight > 0 && maxScroll > 0) {
            int barHeight = Math.max(12, contentHeight * contentHeight / totalHeight);
            int barY = contentTop + (contentHeight - barHeight) * scrollOffset / maxScroll;
            context.fill(windowX + windowWidth - 3, barY, windowX + windowWidth - 1, barY + barHeight, 0xFF606060);
        }

        if (waitingForBind) {
            context.drawCenteredString(
                    this.font,
                    Component.literal("按任意键绑定 (ESC 清除)"),
                    windowX + windowWidth / 2,
                    windowY + windowHeight - 42,
                    ACCENT_COLOR);
        }
    }

    /** 禁用原版背景（毛玻璃/泥土纹理），弹窗使用自身遮罩。 */
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int) (verticalAmount * ROW_HEIGHT), 0, maxScroll);
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
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
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void updateKeyBindButtonLabel() {
        if (keyBindButton == null) {
            return;
        }

        if (waitingForBind) {
            keyBindButton.setMessage(Component.literal("按键绑定: <按任意键>"));
            return;
        }

        keyBindButton.setMessage(Component.literal("按键绑定: " + module.getKeyName()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void resetSettingsToDefaults() {
        for (Setting<?> setting : module.getSettings()) {
            ((Setting) setting).set(setting.getDefaultValue());
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ModuleSettingsScreen(module, parent));
        }
    }

    private class BasaltButton extends Button {
        public BasaltButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isHovered();
            int bgColor = hovered ? BUTTON_HOVER_BG : BUTTON_NORMAL_BG;
            int borderColor = hovered ? ThemeManager.get().accent() : BUTTON_BORDER;
            int textColor = hovered ? 0xFFFFFF : 0xDDDDDD;

            context.fill(getX(), getY(), getX() + this.width, getY() + this.height, bgColor);
            GuiUtil.border(context, getX(), getY(), this.width, this.height, borderColor);

            int textWidth = Minecraft.getInstance().font.width(getMessage());
            int textX = getX() + (this.width - textWidth) / 2;
            int textY = getY() + (this.height - 8) / 2;

            context.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, textColor);
        }
    }

    private class BasaltSlider extends AbstractWidget {
        private final DoubleSetting setting;
        private boolean dragging;

        public BasaltSlider(int x, int y, int width, int height, DoubleSetting setting) {
            super(x, y, width, height, Component.empty());
            this.setting = setting;
            updateMessage();
        }

        private void updateMessage() {
            setMessage(Component.literal(setting.getName() + ": " + String.format(Locale.ROOT, "%.2f", setting.get())));
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isHovered();

            context.fill(getX(), getY(), getX() + this.width, getY() + this.height, SLIDER_BG);
            GuiUtil.border(context, getX(), getY(), this.width, this.height, hovered ? ThemeManager.get().accent() : BUTTON_BORDER);

            double range = setting.getMax() - setting.getMin();
            double percent = range <= 0 ? 0 : (setting.get() - setting.getMin()) / range;
            int fillWidth = (int) (this.width * percent);
            context.fill(getX(), getY(), getX() + fillWidth, getY() + this.height, ThemeManager.get().accent());

            int textWidth = Minecraft.getInstance().font.width(getMessage());
            int textX = getX() + (this.width - textWidth) / 2;
            int textY = getY() + (this.height - 8) / 2;
            context.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, 0xFFFFFF);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            setValueFromMouse(mouseX);
            dragging = true;
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            dragging = false;
        }

        @Override
        public void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
            if (dragging) {
                setValueFromMouse(mouseX);
            }
        }

        private void setValueFromMouse(double mouseX) {
            double percent = (mouseX - getX()) / (double) this.width;
            percent = Mth.clamp(percent, 0.0, 1.0);

            double range = setting.getMax() - setting.getMin();
            setting.set(setting.getMin() + (range * percent));
            updateMessage();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
        }
    }
}
