package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

public class WatermarkElement extends HudElement {
    public WatermarkElement() {
        super("Watermark", Anchor.TOP_LEFT, 2, 2);
    }

    private String text() {
        return BasaltClient.NAME + " v" + BasaltClient.VERSION;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        context.drawTextWithShadow(mc.textRenderer, text(), getX(), getY(), 0xFFA060F0);
    }

    @Override
    public int getWidth() {
        return mc.textRenderer.getWidth(text());
    }

    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight;
    }
}
