package mc.basalt.client.gui;

import mc.basalt.client.system.config.PresetManager;
import mc.basalt.client.ui.notifications.NotificationManager;
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
 * 配置预设管理面板：列表加载、命名保存当前配置、删除。
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
        super(Component.literal("配置预设"));
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
        int by = windowY + windowHeight - 46;

        nameBox = new EditBox(this.font, x, by, CONTENT_WIDTH - 92, 20, Component.literal("预设名"));
        nameBox.setMaxLength(32);
        nameBox.setHint(Component.literal("输入新预设名称..."));
        this.addRenderableWidget(nameBox);
    }

    /** 列表可视区域。 */
    private int listTop() {
        return windowY + 42;
    }

    private int listBottom() {
        return windowY + windowHeight - 54;
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

        // 窗口
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        GuiUtil.border(context, windowX, windowY, windowWidth, windowHeight, theme.accent());
        context.drawString(this.font, this.title, windowX + 12, windowY + 12, 0xFFFFFFFF);
        context.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, theme.accent());

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        List<String> presets = PresetManager.list();
        String active = PresetManager.getActivePreset();

        // 列表
        if (presets.isEmpty()) {
            context.drawCenteredString(this.font, "暂无预设 · 在下方输入名称保存当前配置",
                    windowX + windowWidth / 2, listTop() + 20, 0x808080);
        }

        for (int i = 0; i < presets.size(); i++) {
            int ry = rowY(i);
            if (ry + ROW_HEIGHT < listTop() || ry > listBottom()) {
                continue;
            }
            String name = presets.get(i);
            boolean isActive = name.equals(active);
            boolean hover = mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= ry && mouseY <= ry + ROW_HEIGHT;
            boolean hoverDelete = hoverDelete(name, presets, mouseX, mouseY, x);

            context.fill(x, ry, x + CONTENT_WIDTH, ry + ROW_HEIGHT, hover ? ROW_BG_HOVER : ROW_BG);
            GuiUtil.border(context, x, ry, CONTENT_WIDTH, ROW_HEIGHT,
                    isActive ? theme.accent() : (hover ? 0xFF707078 : 0xFF46464E));

            // 名称 + 当前标记
            String label = isActive ? name + "  (当前)" : name;
            context.drawString(this.font, label, x + 8, ry + 8, isActive ? theme.accent() : 0xFFE0E0E8);

            // 删除按钮
            int delX = x + CONTENT_WIDTH - 20;
            context.drawString(this.font, "✕", delX, ry + 8, hoverDelete ? 0xFFE85555 : 0xFF707078);
        }

        // 底部：输入框 + 保存按钮（由 super.render 绘制 EditBox）
        int by = windowY + windowHeight - 46;
        boolean hoverSave = mouseX >= x + CONTENT_WIDTH - 86 && mouseX <= x + CONTENT_WIDTH && mouseY >= by && mouseY <= by + 20;
        context.fill(x + CONTENT_WIDTH - 86, by, x + CONTENT_WIDTH, by + 20, hoverSave ? ROW_BG_HOVER : ROW_BG);
        GuiUtil.border(context, x + CONTENT_WIDTH - 86, by, 86, 20, hoverSave ? theme.accent() : 0xFF505058);
        String save = "保存为预设";
        context.drawString(this.font, save, x + CONTENT_WIDTH - 86 + (86 - this.font.width(save)) / 2, by + 6, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredString(this.font, "点击列表切换预设 · ✕ 删除 · Enter 保存 · ESC 返回",
                this.width / 2, windowY + windowHeight - 14, 0x707070);
    }

    private boolean hoverDelete(String name, List<String> presets, int mouseX, int mouseY, int x) {
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
        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        List<String> presets = PresetManager.list();

        // 保存按钮
        int by = windowY + windowHeight - 46;
        if (button == 0 && mouseX >= x + CONTENT_WIDTH - 86 && mouseX <= x + CONTENT_WIDTH
                && mouseY >= by && mouseY <= by + 20) {
            saveFromBox();
            return true;
        }

        // 列表行 / 删除
        for (int i = 0; i < presets.size(); i++) {
            int ry = rowY(i);
            if (mouseX >= x && mouseX <= x + CONTENT_WIDTH && mouseY >= ry && mouseY <= ry + ROW_HEIGHT) {
                String name = presets.get(i);
                if (button == 0) {
                    if (hoverDelete(name, presets, (int) mouseX, (int) mouseY, x)) {
                        if (PresetManager.delete(name)) {
                            NotificationManager.info("预设", "已删除: " + name);
                        }
                    } else if (PresetManager.apply(name)) {
                        NotificationManager.success("预设", "已切换: " + name);
                    }
                } else if (button == 1) {
                    if (PresetManager.delete(name)) {
                        NotificationManager.info("预设", "已删除: " + name);
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
            NotificationManager.warn("预设", "名称无效（字母/数字/下划线/中文）");
            return;
        }
        if (PresetManager.save(name)) {
            NotificationManager.success("预设", "已保存: " + name);
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

    /** 禁用原版背景（毛玻璃/泥土纹理）。 */
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
