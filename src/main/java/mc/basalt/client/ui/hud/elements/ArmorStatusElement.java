package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorStatusElement extends HudElement {
    public ArmorStatusElement() {
        // 底部居中锚点 + 额外偏移 → 显示在快捷栏右侧上方
        super("ArmorStatus", Anchor.BOTTOM_CENTER, 0, -39);
    }

    @Override
    protected int getCenterExtraOffsetX(int width) {
        // 与原版默认位置对齐：屏幕中线右侧 10px
        return width / 2 + 10;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null) {
            return;
        }

        int offset = 0;
        // 头盔 → 靴子（索引 3 → 0）
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, getX() + offset, getY());
                context.drawItemInSlot(mc.textRenderer, stack, getX() + offset, getY());
                offset += 16;
            }
        }
    }

    @Override
    public int getWidth() {
        int count = 0;
        if (mc.player != null) {
            for (int i = 0; i < 4; i++) {
                if (!mc.player.getInventory().armor.get(i).isEmpty()) {
                    count++;
                }
            }
        }
        return Math.max(16, count * 16);
    }

    @Override
    public int getHeight() {
        return 16;
    }
}
