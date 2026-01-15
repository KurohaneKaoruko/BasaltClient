package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

public class InfoElement extends HudElement {
    public InfoElement() {
        super("Info", 2, 0); // Y handled dynamically
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null) return;

        int fps = mc.getCurrentFps();
        String fpsText = "FPS: " + fps;
        
        String coordsText = String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        int y = context.getScaledWindowHeight() - mc.textRenderer.fontHeight - 2;
        context.drawTextWithShadow(mc.textRenderer, coordsText, getX(), y, 0xAAAAAA);
        
        y -= mc.textRenderer.fontHeight + 2;
        context.drawTextWithShadow(mc.textRenderer, fpsText, getX(), y, 0xAAAAAA);
    }
}
