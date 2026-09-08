package mc.basalt.client.ui.hud.elements;

import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.hud.HudElement;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListElement extends HudElement {
    private static final int ACCENT_COLOR = 0xFFA060F0;

    public ArrayListElement() {
        // 右上角锚点 + 偏移 0 → 列表右侧紧贴屏幕右边缘
        super("ArrayList", Anchor.TOP_RIGHT, 0, 2);
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        List<Module> enabledModules = Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> mc.textRenderer.getWidth(m.getDisplayName())).reversed())
                .collect(Collectors.toList());

        int width = getContentWidth();
        int y = getY();

        if (enabledModules.isEmpty()) {
            if (HudEditorScreen.isActive()) {
                context.drawTextWithShadow(mc.textRenderer, "(无启用模块)", getX(), y, 0x707070);
            }
            return;
        }

        for (Module module : enabledModules) {
            String text = module.getDisplayName();
            int textWidth = mc.textRenderer.getWidth(text);
            int x = getX() + width - textWidth;

            context.fill(x - 2, y - 2, getX() + width, y + mc.textRenderer.fontHeight + 1, 0x80000000);
            context.fill(getX() + width - 1, y - 2, getX() + width, y + mc.textRenderer.fontHeight + 1, ACCENT_COLOR);
            context.drawTextWithShadow(mc.textRenderer, text, x, y, 0xFFFFFF);

            y += mc.textRenderer.fontHeight + 2;
        }
    }

    private int getContentWidth() {
        return Modules.get().getAll().stream()
                .filter(Module::isEnabled)
                .mapToInt(m -> mc.textRenderer.getWidth(m.getDisplayName()))
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
        return (int) Math.max(1, count) * (mc.textRenderer.fontHeight + 2);
    }
}
