package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorStatusElement extends HudElement {
    public ArmorStatusElement() {
        super("ArmorStatus", 0, 0);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null)
            return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        // Draw above hunger bar (right side of hotbar)
        int startX = width / 2 + 10;
        int y = height - 55;

        int offset = 0;
        // Draw Helmet -> Boots (Index 3 down to 0)
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, startX + offset, y);
                // Draw durability/count overlay
                context.drawItemInSlot(mc.textRenderer, stack, startX + offset, y);
                offset += 16;
            }
        }
    }
}
