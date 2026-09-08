package mc.basalt.client.gui;

import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.ui.hud.HudElement;
import mc.basalt.client.ui.hud.HudManager;
import mc.basalt.client.ui.theme.Lang;
import mc.basalt.client.ui.theme.ThemeManager;
import mc.basalt.client.utils.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * GUI 设置：坐标显示位置、列表位置、语言（均为点击循环切换）。
 */
public class GuiSettingsScreen extends Screen {
    private static final int WINDOW_BG = new java.awt.Color(18, 18, 22, 245).getRGB();
    private static final int PANEL_BG = new java.awt.Color(30, 30, 36, 255).getRGB();
    private static final int PANEL_BG_HOVER = new java.awt.Color(42, 42, 50, 255).getRGB();
    private static final int BORDER = new java.awt.Color(70, 70, 78, 255).getRGB();
    private static final int TEXT_DIM = 0xFF909090;

    private static final int CONTENT_WIDTH = 264;
    private static final int BTN_H = 20;
    private static final int LABEL_W = 100;
    private static final int VALUE_W = CONTENT_WIDTH - LABEL_W - 8;

    private static final HudElement.Anchor[] ANCHOR_ORDER = {
            HudElement.Anchor.TOP_LEFT, HudElement.Anchor.TOP_RIGHT,
            HudElement.Anchor.MIDDLE_LEFT, HudElement.Anchor.MIDDLE_RIGHT,
            HudElement.Anchor.BOTTOM_LEFT, HudElement.Anchor.BOTTOM_RIGHT
    };
    private static final String[] LANG_NAMES = {"中文", "English"};
    private static final String[] LANG_CODES = {"zh", "en"};

    private final ThemeManager theme = ThemeManager.get();
    private final Screen parent;

    private int windowX, windowY, windowWidth = 300, windowHeight = 180;
    private int infoY, listY, langY;

    public GuiSettingsScreen(Screen parent) {
        super(Component.literal("GUI 设置"));
        this.parent = parent;
    }

    private HudElement infoElement() { return HudManager.get().getByName("Info"); }
    private HudElement listElement() { return HudManager.get().getByName("ArrayList"); }
    private int closeX() { return windowX + windowWidth - 22; }
    private int closeY() { return windowY + 8; }

    @Override
    protected void init() {
        this.windowWidth = Math.min(300, this.width - 10);
        this.windowHeight = Math.min(180, this.height - 10);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;

        int y = windowY + 44;
        infoY = y; y += BTN_H + 10;
        listY = y; y += BTN_H + 10;
        langY = y;
    }

    private String anchorName(int idx) {
        String[] names = {Lang.get("左上","Top-L"), Lang.get("右上","Top-R"), Lang.get("左中","Mid-L"), Lang.get("右中","Mid-R"), Lang.get("左下","Bot-L"), Lang.get("右下","Bot-R")};
        return names[idx];
    }

    private int anchorIndex(HudElement el) {
        for (int i = 0; i < ANCHOR_ORDER.length; i++) {
            if (el.getAnchor() == ANCHOR_ORDER[i]) return i;
        }
        return 0;
    }

    private void cycleAnchor(HudElement el) {
        int next = (anchorIndex(el) + 1) % ANCHOR_ORDER.length;
        HudElement.Anchor anchor = ANCHOR_ORDER[next];
        el.setAnchor(anchor);
        el.setOffsetY(switch (anchor) {
            case TOP_LEFT -> 18;
            case MIDDLE_LEFT, MIDDLE_RIGHT -> 0;
            case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER -> -2;
            default -> 2;
        });
        el.setOffsetX(switch (anchor) {
            case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> -2;
            default -> 0;
        });
    }

    private int langIndex() {
        String lang = theme.getLanguage();
        for (int i = 0; i < LANG_CODES.length; i++) {
            if (LANG_CODES[i].equals(lang)) return i;
        }
        return 0;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xB0000000);

        // 窗口
        ctx.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, WINDOW_BG);
        GuiUtil.border(ctx, windowX, windowY, windowWidth, windowHeight, theme.accent());
        ctx.drawString(this.font, Component.literal(Lang.get("GUI 设置", "GUI Settings")), windowX + 12, windowY + 12, 0xFFFFFFFF);
        ctx.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, theme.accent());

        boolean hoverClose = isHover(mouseX, mouseY, closeX(), closeY(), 16, 14);
        ctx.drawString(this.font, "✕", closeX() + 4, closeY() + 3, hoverClose ? 0xFFE85555 : 0xFF909090);

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int vx = x + LABEL_W + 8;

        // 坐标位置
        ctx.drawString(this.font, Lang.get("坐标显示位置", "Coordinates"), x, infoY + 6, TEXT_DIM);
        drawValueButton(ctx, vx, infoY, anchorName(anchorIndex(infoElement())) + "  ▸", isHover(mouseX, mouseY, vx, infoY, VALUE_W, BTN_H));

        // 列表位置
        ctx.drawString(this.font, Lang.get("模块列表位置", "ArrayList pos"), x, listY + 6, TEXT_DIM);
        drawValueButton(ctx, vx, listY, anchorName(anchorIndex(listElement())) + "  ▸", isHover(mouseX, mouseY, vx, listY, VALUE_W, BTN_H));

        // 语言（点击循环）
        ctx.drawString(this.font, Lang.get("语言 / Language", "Language"), x, langY + 6, TEXT_DIM);
        drawValueButton(ctx, vx, langY, LANG_NAMES[langIndex()] + "  ▸", isHover(mouseX, mouseY, vx, langY, VALUE_W, BTN_H));
    }

    private boolean isHover(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void drawValueButton(GuiGraphics ctx, int x, int y, String text, boolean hover) {
        ctx.fill(x, y, x + VALUE_W, y + BTN_H, hover ? PANEL_BG_HOVER : PANEL_BG);
        GuiUtil.border(ctx, x, y, VALUE_W, BTN_H, hover ? theme.accent() : BORDER);
        ctx.drawString(this.font, text, x + 10, y + 6, hover ? 0xFFFFFFFF : 0xFFE0E0E8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        if (isHover(mouseX, mouseY, closeX(), closeY(), 16, 14)) {
            onClose();
            return true;
        }

        int x = windowX + (windowWidth - CONTENT_WIDTH) / 2;
        int vx = x + LABEL_W + 8;

        if (isHover(mouseX, mouseY, vx, infoY, VALUE_W, BTN_H)) {
            cycleAnchor(infoElement());
            return true;
        }

        if (isHover(mouseX, mouseY, vx, listY, VALUE_W, BTN_H)) {
            cycleAnchor(listElement());
            return true;
        }

        if (isHover(mouseX, mouseY, vx, langY, VALUE_W, BTN_H)) {
            theme.setLanguage(LANG_CODES[(langIndex() + 1) % LANG_CODES.length]);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
