package mc.basalt.client.gui;

import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.system.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.List;

public class ModuleSettingsScreen extends Screen {
    private final Module module;
    private final Screen parent;

    // Basalt Tech Theme Colors
    private static final int WINDOW_BG = new Color(20, 20, 20, 240).getRGB();
    private static final int BORDER_COLOR = new Color(60, 60, 60, 255).getRGB();
    private static final int ACCENT_COLOR = new Color(220, 220, 220, 255).getRGB();

    // Button Colors
    private static final int BUTTON_NORMAL_BG = new Color(30, 30, 30, 255).getRGB();
    private static final int BUTTON_HOVER_BG = new Color(50, 50, 50, 255).getRGB();
    private static final int BUTTON_BORDER = new Color(80, 80, 80, 255).getRGB();
    private static final int BUTTON_BORDER_HOVER = new Color(200, 200, 200, 255).getRGB();

    // Slider Colors
    private static final int SLIDER_BG = new Color(40, 40, 40, 255).getRGB();
    private static final int SLIDER_FILL = new Color(160, 32, 240, 255).getRGB(); // Purple

    // Window Dimensions
    private int windowWidth = 220;
    private int windowHeight = 200; // Increased height
    private int windowX;
    private int windowY;

    public ModuleSettingsScreen(Module module, Screen parent) {
        super(Text.literal(module.getDisplayName() + " 设置"));
        this.module = module;
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Calculate dynamic height based on settings count
        List<Setting<?>> settings = module.getSettings();
        int settingsHeight = (settings.size() + 2) * 25; // +2 for toggle and back buttons
        this.windowHeight = Math.max(140, settingsHeight + 40); // Min height 140, +40 for header/padding

        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;

        int contentStartX = windowX + 10;
        int currentY = windowY + 30;

        // Toggle Button
        this.addDrawableChild(new BasaltButton(
                contentStartX, currentY, 200, 20,
                Text.literal(module.isEnabled() ? "状态: 已开启" : "状态: 已关闭"),
                button -> {
                    module.toggle();
                    button.setMessage(Text.literal(module.isEnabled() ? "状态: 已开启" : "状态: 已关闭"));
                }));
        currentY += 25;

        // Dynamic Settings
        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;

            if (setting instanceof BooleanSetting boolSetting) {
                this.addDrawableChild(new BasaltButton(
                        contentStartX, currentY, 200, 20,
                        Text.literal(boolSetting.getName() + ": " + (boolSetting.get() ? "开启" : "关闭")),
                        btn -> {
                            boolSetting.toggle();
                            btn.setMessage(Text.literal(boolSetting.getName() + ": " + (boolSetting.get() ? "开启" : "关闭")));
                        }
                ));
            } else if (setting instanceof ModeSetting modeSetting) {
                this.addDrawableChild(new BasaltButton(
                        contentStartX, currentY, 200, 20,
                        Text.literal(modeSetting.getName() + ": " + modeSetting.get()),
                        btn -> {
                            modeSetting.cycle();
                            btn.setMessage(Text.literal(modeSetting.getName() + ": " + modeSetting.get()));
                        }
                ));
            } else if (setting instanceof DoubleSetting doubleSetting) {
                this.addDrawableChild(new BasaltSlider(
                        contentStartX, currentY, 200, 20, doubleSetting
                ));
            }
            currentY += 25;
        }

        // Back Button
        this.addDrawableChild(new BasaltButton(
                contentStartX, windowY + windowHeight - 30, 200, 20,
                Text.literal("返回"),
                button -> this.client.setScreen(this.parent)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            parent.render(context, -1, -1, delta);
        }

        context.fill(0, 0, this.width, this.height, 0x80000000);
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        context.drawBorder(windowX, windowY, windowWidth, windowHeight, BORDER_COLOR);

        int headerHeight = 25;
        context.drawTextWithShadow(this.textRenderer, this.title, windowX + 10, windowY + 8, ACCENT_COLOR);
        context.fill(windowX, windowY + headerHeight, windowX + windowWidth, windowY + headerHeight + 1, BORDER_COLOR);

        for (var child : this.children()) {
            if (child instanceof net.minecraft.client.gui.Drawable) {
                ((net.minecraft.client.gui.Drawable) child).render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private class BasaltButton extends ButtonWidget {
        public BasaltButton(int x, int y, int width, int height, Text message, PressAction onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isSelected();
            int bgColor = hovered ? BUTTON_HOVER_BG : BUTTON_NORMAL_BG;
            int borderColor = hovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER;
            int textColor = hovered ? 0xFFFFFF : 0xDDDDDD;

            context.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
            context.drawBorder(getX(), getY(), width, height, borderColor);

            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(getMessage());
            int textX = getX() + (width - textWidth) / 2;
            int textY = getY() + (height - 8) / 2;

            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), textX, textY, textColor);
        }
    }

    private class BasaltSlider extends ClickableWidget {
        private final DoubleSetting setting;
        private boolean dragging;

        public BasaltSlider(int x, int y, int width, int height, DoubleSetting setting) {
            super(x, y, width, height, Text.empty());
            this.setting = setting;
            updateMessage();
        }

        private void updateMessage() {
            setMessage(Text.literal(setting.getName() + ": " + String.format("%.2f", setting.get())));
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = this.isHovered();
            
            // Background
            context.fill(getX(), getY(), getX() + width, getY() + height, SLIDER_BG);
            context.drawBorder(getX(), getY(), width, height, hovered ? BUTTON_BORDER_HOVER : BUTTON_BORDER);

            // Fill
            double range = setting.getMax() - setting.getMin();
            double percent = (setting.get() - setting.getMin()) / range;
            int fillWidth = (int) (width * percent);
            context.fill(getX(), getY(), getX() + fillWidth, getY() + height, SLIDER_FILL);

            // Text
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(getMessage());
            int textX = getX() + (width - textWidth) / 2;
            int textY = getY() + (height - 8) / 2;
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), textX, textY, 0xFFFFFF);
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
            double percent = (mouseX - getX()) / (double) width;
            percent = MathHelper.clamp(percent, 0.0, 1.0);
            
            double range = setting.getMax() - setting.getMin();
            double newValue = setting.getMin() + (range * percent);
            
            // Round to increment
            double inc = setting.getIncrement();
            if (inc > 0) {
                newValue = Math.round(newValue / inc) * inc;
            }
            
            setting.set(newValue);
            updateMessage();
        }

        @Override
        protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        }
    }
}
