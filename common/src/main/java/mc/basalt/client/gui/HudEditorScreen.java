package mc.basalt.client.gui;

import mc.basalt.client.utils.GuiUtil;

import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.hud.HudManager;
import mc.basalt.client.ui.notifications.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;

/**
 * HUD 编辑器：拖拽移动元素、右键显示/隐藏、关闭时保存布局。
 */
public class HudEditorScreen extends Screen {
    private static boolean active;

    private HudElement dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("HUD 编辑器"));
    }

    public static boolean isActive() {
        return active;
    }

    @Override
    protected void init() {
        active = true;
        dragging = null;
    }

    @Override
    public void removed() {
        active = false;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        HudManager.get().render(context, delta);

        List<HudElement> elements = HudManager.get().getElements();
        for (HudElement element : elements) {
            int w = Math.max(element.getWidth(), 10);
            int h = Math.max(element.getHeight(), 8);
            int border = element.isEnabled() ? 0xFFA060F0 : 0xFF707070;

            GuiUtil.border(context, element.getX(), element.getY(), w, h, border);

            // 名称标签（左上角）
            String label = element.getName() + (element.isEnabled() ? "" : " (隐藏)");
            int labelWidth = this.font.width(label);
            context.fill(element.getX(), element.getY() - 10, element.getX() + labelWidth + 4, element.getY() - 1, 0xA0000000);
            context.drawString(this.font, label, element.getX() + 2, element.getY() - 9, border);
        }

        context.drawCenteredString(
                this.font,
                Component.literal("拖拽: 移动  |  右键: 显示/隐藏  |  ESC: 退出并保存"),
                this.width / 2,
                this.height - 14,
                0xB0B0B0);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        HudElement hit = findElementAt(mouseX, mouseY);

        if (hit != null && button == 0) {
            dragging = hit;
            dragOffsetX = (int) (mouseX - hit.getX());
            dragOffsetY = (int) (mouseY - hit.getY());
            return true;
        }

        if (hit != null && button == 1) {
            hit.toggle();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null && button == 0) {
            int x = Mth.clamp((int) mouseX - dragOffsetX, 0, Math.max(0, this.width - 10));
            int y = Mth.clamp((int) mouseY - dragOffsetY, 0, Math.max(0, this.height - 10));
            // 吸附最近锚点并记录偏移，分辨率变化后仍保持相对位置
            dragging.dragTo(x, y, this.width, this.height);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != null && button == 0) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        NotificationManager.success("HUD 编辑器", "布局已保存");
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private HudElement findElementAt(double mouseX, double mouseY) {
        List<HudElement> elements = HudManager.get().getElements();
        // 后添加的优先（渲染在上层）
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            int w = Math.max(element.getWidth(), 10);
            int h = Math.max(element.getHeight(), 8);
            if (mouseX >= element.getX() && mouseX <= element.getX() + w
                    && mouseY >= element.getY() && mouseY <= element.getY() + h) {
                return element;
            }
        }
        return null;
    }
}
