package mc.basalt.client.ui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public abstract class HudElement {
    /** 屏幕锚点：元素位置 = 锚点 + 偏移，窗口尺寸变化时自动保持相对位置。 */
    public enum Anchor {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER
    }

    protected final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private Anchor anchor;
    private int offsetX;
    private int offsetY;
    private int x;
    private int y;
    private boolean enabled = true;

    public HudElement(String name, Anchor anchor, int offsetX, int offsetY) {
        this.name = name;
        this.anchor = anchor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public abstract void render(GuiGraphics context, float tickDelta);

    /** 元素内容宽度（用于锚点计算与 HUD 编辑器命中框）。 */
    public abstract int getWidth();

    /** 元素内容高度（用于锚点计算与 HUD 编辑器命中框）。 */
    public abstract int getHeight();

    /** BOTTOM_CENTER 锚点的额外水平偏移（默认 0，可覆盖以实现热键栏右侧对齐等）。 */
    protected int getCenterExtraOffsetX(int width) {
        return 0;
    }

    /** 每帧调用：根据锚点、偏移与当前屏幕尺寸计算实际位置。 */
    public final void updatePosition(int screenWidth, int screenHeight) {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        int[] base = computeBase(anchor, screenWidth, screenHeight, w, h);

        this.x = Mth.clamp(base[0] + offsetX, 0, Math.max(0, screenWidth - 4));
        this.y = Mth.clamp(base[1] + offsetY, 0, Math.max(0, screenHeight - 4));
    }

    /**
     * 编辑器拖拽：吸附到距离元素中心最近的锚点，
     * 并记录相对该锚点的偏移，使元素精确停留在松开的位置。
     */
    public final void dragTo(int posX, int posY, int screenWidth, int screenHeight) {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        int cx = posX + w / 2;
        int cy = posY + h / 2;

        Anchor[] anchors = Anchor.values();
        int[][] points = {
                {0, 0},                          // TOP_LEFT
                {screenWidth, 0},                // TOP_RIGHT
                {0, screenHeight},               // BOTTOM_LEFT
                {screenWidth, screenHeight},     // BOTTOM_RIGHT
                {screenWidth / 2, screenHeight}  // BOTTOM_CENTER
        };

        long bestDist = Long.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < points.length; i++) {
            long dx = cx - points[i][0];
            long dy = cy - points[i][1];
            long dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                bestIndex = i;
            }
        }

        this.anchor = anchors[bestIndex];
        int[] base = computeBase(anchor, screenWidth, screenHeight, w, h);
        this.offsetX = posX - base[0];
        this.offsetY = posY - base[1];

        updatePosition(screenWidth, screenHeight);
    }

    private int[] computeBase(Anchor anchor, int screenWidth, int screenHeight, int w, int h) {
        int baseX;
        int baseY;
        switch (anchor) {
            case TOP_RIGHT -> {
                baseX = screenWidth - w;
                baseY = 0;
            }
            case BOTTOM_LEFT -> {
                baseX = 0;
                baseY = screenHeight - h;
            }
            case BOTTOM_RIGHT -> {
                baseX = screenWidth - w;
                baseY = screenHeight - h;
            }
            case BOTTOM_CENTER -> {
                baseX = screenWidth / 2 - w / 2 + getCenterExtraOffsetX(w);
                baseY = screenHeight - h;
            }
            default -> {
                baseX = 0;
                baseY = 0;
            }
        }
        return new int[]{baseX, baseY};
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }
}
