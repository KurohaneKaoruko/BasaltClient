package mc.basalt.client.gui;

import mc.basalt.client.system.config.PresetManager;
import mc.basalt.client.ui.notifications.NotificationManager;
import mc.basalt.client.ui.theme.Lang;
import mc.basalt.client.ui.theme.ThemeManager;
import mc.basalt.client.utils.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * 配置预设管理面板：列表加载、命名保存当前配置、删除、关闭。
 */
public class PresetScreen extends Screen {
    private static final int WINDOW_BG = new java.awt.Color(18, 18, 22, 245).getRGB();
    private static final int ROW_BG = new java.awt.Color(30, 30, 36, 255).getRGB();
    private static final int ROW_BG_HOVER = new java.awt.Color(42, 42, 50, 255).getRGB();

    private static final int ROW_HEIGHT = 24;
    private static final int CONTENT_WIDTH = 260;

    private final Screen parent;
    private final ThemeManager theme = ThemeManager.get();

    private int windowX;
    private int windowY;
    private int windowWidth = 330;
    private int windowHeight = 290;
    private int scrollOffset;

    private EditBox nameBox;

    public PresetScreen(Screen parent) {
        super(Component.literal(Lang.get("配置预设", "Config Presets")));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.windowWidth = Math.min(330, this.width - 10);
        this.windowHeight = Math.min(290, this.height - 10);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;
        scrollOffset = 0;

        clearWidgets();
        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int by = windowY + windowHeight - 34;

        nameBox = new EditBox(this.font, x, by, CONTENT_WIDTH - 92, 20, Component.literal(Lang.get("预设名", "Name")));
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.literal(Lang.get("输入新预设名称...", "New preset name...")));
        this.addRenderableWidget(nameBox);
    }

    private int closeX() {
        return windowX + windowWidth - 22;
    }

    private int closeY() {
        return windowY + 8;
    }

    private int listTop() {
        return windowY + 42;
    }

    private int listBottom() {
        return windowY + windowHeight - 42;
    }

    private int rowY(int index) {
        return listTop() + 4 + index * (ROW_HEIGHT + 2) - scrollOffset;
    }

    private int maxScroll(List<String> presets) {
        int content = presets.size() * (ROW_HEIGHT + 2) + 4;
        int view = listBottom() - listTop();
        return Math.max(0, content - view);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xB0000000);

        // 窗口（同 ThemeScreen 风格）
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        GuiUtil.border(context, windowX, windowY, windowWidth, windowHeight, theme.accent());
        context.drawString(this.font, this.title, windowX + 12, windowY + 12, 0xFFFFFFFF);
        context.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, theme.accent());

        // 关闭按钮 ✕（右上角）
        boolean hoverClose = mouseX >= closeX() && mouseX <= closeX() + 16 && mouseY >= closeY() && mouseY <= closeY() + 14;
        context.drawString(this.font, "✕", closeX() + 4, closeY() + 3, hoverClose ? 0xFFE85555 : 0xFF909090);

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        List<String> presets = PresetManager.list();
        String active = PresetManager.getActivePreset();

        // 预设列表（裁剪，防止溢出）
        context.enableScissor(windowX + 2, listTop(), windowX + windowWidth - 2, listBottom());
        if (presets.isEmpty()) {
            context.drawCenteredString(this.font, Lang.get("暂无预设 · 在下方输入名称保存当前配置", "No presets yet · Type a name below"),
                    windowX + windowWidth / 2, listTop() + 20, 0x808080);
        }

        for (int i = 0; i < presets.size(); i++) {
            int ry = rowY(i);
            String name = presets.get(i);
            boolean isActive = name.equals(active);
            boolean hover = mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= ry && mouseY <= ry + ROW_HEIGHT;
            boolean hoverDelete = hoverDelete(name, presets, mouseX, mouseY, x);

            context.fill(x, ry, x + CONTENT_WIDTH, ry + ROW_HEIGHT, hover ? ROW_BG_HOVER : ROW_BG);
            GuiUtil.border(context, x, ry, CONTENT_WIDTH, ROW_HEIGHT,
                    isActive ? theme.accent() : (hover ? 0xFF707078 : 0xFF46464E));

            String label = isActive ? name + "  (当前)" : name;
            context.drawString(this.font, label, x + 8, ry + 8, isActive ? theme.accent() : 0xFFE0E0E8);

            int delX = x + CONTENT_WIDTH - 20;
            context.drawString(this.font, "✕", delX, ry + 8, hoverDelete ? 0xFFE85555 : 0xFF707078);
        }
        context.disableScissor();

        // 底部：输入框 + 保存按钮
        int by = windowY + windowHeight - 34;
        boolean hoverSave = mouseX >= x + CONTENT_WIDTH - 86 && mouseX <= x + CONTENT_WIDTH && mouseY >= by && mouseY <= by + 20;
        context.fill(x + CONTENT_WIDTH - 86, by, x + CONTENT_WIDTH, by + 20, hoverSave ? ROW_BG_HOVER : ROW_BG);
        GuiUtil.border(context, x + CONTENT_WIDTH - 86, by, 86, 20, hoverSave ? theme.accent() : 0xFF505058);
        String save = Lang.get("保存为预设", "Save preset");
        context.drawString(this.font, save, x + CONTENT_WIDTH - 86 + (86 - this.font.width(save)) / 2, by + 6, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean hoverDelete(String name, List<String> presets, double mouseX, double mouseY, int x) {
        int i = presets.indexOf(name);
        if (i < 0) {
            return false;
        }
        int ry = rowY(i);
        int delX = x + CONTENT_WIDTH - 20;
        return mouseX >= delX && mouseX <= delX + 12 && mouseY >= ry && mouseY <= ry + ROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 关闭按钮
        if (button == 0 && mouseX >= closeX() && mouseX <= closeX() + 16 && mouseY >= closeY() && mouseY <= closeY() + 14) {
            onClose();
            return true;
        }

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        List<String> presets = PresetManager.list();

        // 保存按钮
        int by = windowY + windowHeight - 34;
        if (button == 0 && mouseX >= x + CONTENT_WIDTH - 86 && mouseX <= x + CONTENT_WIDTH
                && mouseY >= by && mouseY <= by + 20) {
            saveFromBox();
            return true;
        }

        // 预设行 / 删除
        for (int i = 0; i < presets.size(); i++) {
            int ry = rowY(i);
            if (mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= ry && mouseY <= ry + ROW_HEIGHT) {
                String name = presets.get(i);
                if (button == 0) {
                    if (hoverDelete(name, presets, mouseX, mouseY, x)) {
                        if (PresetManager.delete(name)) {
                            NotificationManager.info(Lang.get("预设", "Preset"), Lang.get("已删除: ", "Deleted: ") + name);
                        }
                    } else if (PresetManager.apply(name)) {
                        NotificationManager.success(Lang.get("预设", "Preset"), Lang.get("已切换: ", "Switched: ") + name);
                    }
                } else if (button == 1) {
                    if (PresetManager.delete(name)) {
                        NotificationManager.info(Lang.get("预设", "Preset"), Lang.get("已删除: ", "Deleted: ") + name);
                    }
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void saveFromBox() {
        String name = nameBox != null ? nameBox.getValue().trim() : "";
        if (!PresetManager.isValidName(name)) {
            NotificationManager.warn(Lang.get("预设", "Preset"), Lang.get("名称无效（字母/数字/下划线/中文）", "Invalid name"));
            return;
        }
        if (PresetManager.save(name)) {
            NotificationManager.success(Lang.get("预设", "Preset"), Lang.get("已保存: ", "Saved: ") + name);
            nameBox.setValue("");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (nameBox != null && nameBox.isFocused()) {
                saveFromBox();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<String> presets = PresetManager.list();
        int max = maxScroll(presets);
        if (max > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int) (verticalAmount * ROW_HEIGHT), 0, max);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
