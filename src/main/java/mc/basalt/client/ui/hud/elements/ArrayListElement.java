package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListElement extends HudElement {
    public ArrayListElement() {
        super("ArrayList", 0, 0); // Position handled dynamically
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        List<Module> enabledModules = Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> mc.textRenderer.getWidth(m.getDisplayName())).reversed())
                .collect(Collectors.toList());

        int y = 2;
        int screenWidth = context.getScaledWindowWidth();

        for (Module module : enabledModules) {
            String text = module.getDisplayName();
            int width = mc.textRenderer.getWidth(text);
            int x = screenWidth - width - 2;
            
            context.fill(x - 2, y - 2, screenWidth, y + mc.textRenderer.fontHeight + 1, 0x80000000);
            context.fill(screenWidth - 1, y - 2, screenWidth, y + mc.textRenderer.fontHeight + 1, 0xFFA020F0); // Side bar
            context.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFF);
            
            y += mc.textRenderer.fontHeight + 2;
        }
    }
}
