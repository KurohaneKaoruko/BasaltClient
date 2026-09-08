package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.theme.Lang;
import mc.basalt.client.ui.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListElement extends HudElement {

    public ArrayListElement() {
        // 默认右中锚点
        super("ArrayList", Anchor.MIDDLE_RIGHT, -2, 0);
    }

    @Override
    public void render(GuiGraphics context, float tickDelta) {
        List<Module> enabledModules = Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> mc.font.width(Lang.moduleDisplayName(m))).reversed())
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

        // 左侧锚点时左对齐（强调条在左），右侧锚点时右对齐（强调条在右）
        boolean leftAlign = getAnchor() == HudElement.Anchor.TOP_LEFT
                || getAnchor() == HudElement.Anchor.MIDDLE_LEFT
                || getAnchor() == HudElement.Anchor.BOTTOM_LEFT;

        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            String text = Lang.moduleDisplayName(module);
            int textWidth = mc.font.width(text);

            if (leftAlign) {
                int x = getX();
                if (bg != 0) {
                    context.fill(x, y - 2, x + textWidth + 2, y + mc.font.lineHeight + 1, bg);
                }
                context.fill(x, y - 2, x + 1, y + mc.font.lineHeight + 1, theme.listColor(i));
                context.drawString(mc.font, text, x + 3, y, 0xFFFFFFFF);
            } else {
                int x = getX() + width - textWidth;
                if (bg != 0) {
                    context.fill(x - 2, y - 2, getX() + width, y + mc.font.lineHeight + 1, bg);
                }
                context.fill(getX() + width - 1, y - 2, getX() + width, y + mc.font.lineHeight + 1, theme.listColor(i));
                context.drawString(mc.font, text, x, y, 0xFFFFFFFF);
            }

            y += mc.font.lineHeight + 2;
        }
    }

    private int getContentWidth() {
        return Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .mapToInt(m -> mc.font.width(Lang.moduleDisplayName(m)))
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
