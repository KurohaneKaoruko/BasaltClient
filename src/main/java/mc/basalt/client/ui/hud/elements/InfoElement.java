package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

public class InfoElement extends HudElement {
    public InfoElement() {
        super("Info", Anchor.BOTTOM_LEFT, 2, -2);
    }

    private String fpsText() {
        return "FPS: " + mc.getCurrentFps();
    }

    private String coordsText() {
        if (mc.player == null) {
            return "XYZ: -";
        }
        return String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        context.drawTextWithShadow(mc.textRenderer, coordsText(), getX(), getY(), 0xAAAAAA);
        context.drawTextWithShadow(mc.textRenderer, fpsText(), getX(), getY() + mc.textRenderer.fontHeight + 2, 0xAAAAAA);
    }

    @Override
    public int getWidth() {
        return Math.max(mc.textRenderer.getWidth(fpsText()), mc.textRenderer.getWidth(coordsText()));
    }

    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight * 2 + 2;
    }
}
