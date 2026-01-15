package mc.basalt.client.ui.hud;

import mc.basalt.client.ui.hud.elements.ArrayListElement;
import mc.basalt.client.ui.hud.elements.ArmorStatusElement;
import mc.basalt.client.ui.hud.elements.InfoElement;
import mc.basalt.client.ui.hud.elements.WatermarkElement;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    private static final HudManager INSTANCE = new HudManager();
    private final List<HudElement> elements = new ArrayList<>();

    public static HudManager get() {
        return INSTANCE;
    }

    public void init() {
        elements.add(new WatermarkElement());
        elements.add(new ArrayListElement());
        elements.add(new InfoElement());
        elements.add(new ArmorStatusElement());
    }

    public void render(DrawContext context, float tickDelta) {
        for (HudElement element : elements) {
            if (element.isEnabled()) {
                element.render(context, tickDelta);
            }
        }
    }
}
