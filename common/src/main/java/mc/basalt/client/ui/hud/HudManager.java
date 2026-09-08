package mc.basalt.client.ui.hud;

import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.ui.hud.elements.ArrayListElement;
import mc.basalt.client.ui.hud.elements.ArmorStatusElement;
import mc.basalt.client.ui.hud.elements.InfoElement;
import mc.basalt.client.ui.hud.elements.WatermarkElement;
import mc.basalt.client.ui.notifications.NotificationManager;
import net.minecraft.client.gui.GuiGraphics;

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

    public List<HudElement> getElements() {
        return elements;
    }

    public HudElement getByName(String name) {
        return elements.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void render(GuiGraphics context, float tickDelta) {
        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        boolean editor = HudEditorScreen.isActive();

        for (HudElement element : elements) {
            // 每帧根据锚点+偏移重算位置，窗口尺寸变化时自动保持正确布局
            element.updatePosition(screenWidth, screenHeight);
            // 编辑器中即使隐藏的元素也要显示
            if (element.isEnabled() || editor) {
                element.render(context, tickDelta);
            }
        }

        NotificationManager.render(context);
    }
}
