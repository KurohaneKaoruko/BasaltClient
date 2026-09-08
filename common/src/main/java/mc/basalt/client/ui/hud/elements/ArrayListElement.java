package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListElement extends HudElement {

    public ArrayListElement() {
        // 右上角锚点 + 偏移 0 → 列表右侧紧贴屏幕右边缘
        super("ArrayList", Anchor.TOP_RIGHT, 0, 2);
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        List<Module> enabledModules = Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> mc.font.width(m.getDisplayName())).reversed())
                .collect(Collectors.toList());

        int width = getContentWidth();
        int y = getY();

        if (enabledModules.isEmpty()) {
            if (HudEditorScreen.isActive()) {
                context.drawString(mc.font, "(无启用模块)", getX(), y, 0x707070);
            }
            return;
        }

        ThemeManager theme = ThemeManager.get();
        int bg = theme.hudBackground();

        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            String text = module.getDisplayName();
            int textWidth = mc.font.width(text);
            int x = getX() + width - textWidth;

            if (bg != 0) {
                context.fill(x - 2, y - 2, getX() + width, y + mc.font.lineHeight + 1, bg);
            }
            context.fill(getX() + width - 1, y - 2, getX() + width, y + mc.font.lineHeight + 1, theme.listColor(i));
            context.drawString(mc.font, text, x, y, 0xFFFFFFFF);

            y += mc.font.lineHeight + 2;
        }
    }

    private int getContentWidth() {
        return Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .mapToInt(m -> mc.font.width(m.getDisplayName()))
                .max()
                .orElse(50);
    }

    @Override
    public int getWidth() {
        // 必须与 render 使用同一宽度，否则右侧对齐会出现缝隙
        return getContentWidth();
    }

    @Override
    public int getHeight() {
        long count = Modules.get().getAll().stream().filter(Module::isEnabled).count();
        return (int) Math.max(1, count) * (mc.font.lineHeight + 2);
    }
}
