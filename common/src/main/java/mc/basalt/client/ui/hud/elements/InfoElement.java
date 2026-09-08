package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.GuiGraphics;

public class InfoElement extends HudElement {
    public InfoElement() {
        super("Info", Anchor.BOTTOM_LEFT, 2, -2);
    }

    private String fpsText() {
        return "FPS: " + mc.getFps();
    }

    private String coordsText() {
        if (mc.player == null) {
            return "XYZ: -";
        }
        return String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        context.drawString(mc.font, coordsText(), getX(), getY(), 0xAAAAAA);
        context.drawString(mc.font, fpsText(), getX(), getY() + mc.font.lineHeight + 2, 0xAAAAAA);
    }

    @Override
    public int getWidth() {
        return Math.max(mc.font.width(fpsText()), mc.font.width(coordsText()));
    }

    @Override
    public int getHeight() {
        return mc.font.lineHeight * 2 + 2;
    }
}
