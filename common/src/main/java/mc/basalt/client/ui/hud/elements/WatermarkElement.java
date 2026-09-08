package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphics;

public class WatermarkElement extends HudElement {
    public WatermarkElement() {
        super("Watermark", Anchor.TOP_LEFT, 2, 2);
    }

    private String text() {
        return BasaltClient.NAME + " v" + BasaltClient.displayVersion();
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        context.drawString(mc.font, text(), getX(), getY(), ThemeManager.get().accent());
    }

    @Override
    public int getWidth() {
        return mc.font.width(text());
    }

    @Override
    public int getHeight() {
        return mc.font.lineHeight;
    }
}
