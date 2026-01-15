package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

public class WatermarkElement extends HudElement {
    public WatermarkElement() {
        super("Watermark", 2, 2);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        context.drawTextWithShadow(mc.textRenderer, "Basalt Client v1.0", getX(), getY(), 0xA020F0);
    }
}
