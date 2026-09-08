package mc.basalt.client.gui;

import mc.basalt.client.ui.widget.UiKit;
import mc.basalt.client.utils.GuiUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置类页面的统一基类：居中窗口框架 + 单列行式布局 + 滚动。
 * 子类通过 header()/addWidget()/gap() 以光标方式排列内容，
 * 滚动偏移由基类统一应用，杜绝各页面手写坐标导致的错位。
 */
public abstract class BasaltPanelScreen extends Screen {
    protected int windowX;
    protected int windowY;
    protected int windowWidth = 300;
    protected int windowHeight = 240;
    protected int contentWidth = 260;

    private record RowWidget(AbstractWidget widget, int baseY) {
    }

    private record RowLabel(String text, int x, int baseY) {
    }

    private final List<RowWidget> rows = new ArrayList<>();
    private final List<RowLabel> rowLabels = new ArrayList<>();
    private int cursor;
    private int scrollOffset;
    private int contentTop;
    private int contentBottom;

    protected BasaltPanelScreen(Component title) {
        super(title);
    }

    /** 内容左边缘 x 坐标。 */
    protected int contentX() {
        return windowX + (windowWidth - contentWidth) / 2;
    }

    /** 子类 init 里先调用：初始化窗口与光标。 */
    protected void beginLayout(int windowWidth, int windowHeight) {
        this.windowWidth = Math.min(windowWidth, this.width - 10);
        this.windowHeight = Math.min(windowHeight, this.height - 10);
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;
        this.contentTop = windowY + 40;
        this.contentBottom = windowY + windowHeight - 36;
        this.cursor = windowY + 44;
        this.scrollOffset = 0;
        rows.clear();
        rowLabels.clear();
        this.clearWidgets();
    }

    /** 分区标题。 */
    protected void header(String text) {
        rowLabels.add(new RowLabel(text, contentX(), cursor));
        cursor += 16;
    }

    /** 添加控件并推进光标。 */
    protected void addWidget(AbstractWidget widget) {
        place(widget, cursor);
        cursor += widget.getHeight() + 4;
    }

    /** 在当前行添加并排控件（等分内容宽度）。 */
    protected void addRow(List<AbstractWidget> rowWidgets) {
        int n = rowWidgets.size();
        if (n == 0) {
            return;
        }
        int cell = (contentWidth - (n - 1) * 6) / n;
        int baseY = cursor;
        for (int i = 0; i < n; i++) {
            AbstractWidget w = rowWidgets.get(i);
            w.setX(contentX() + i * (cell + 6));
            w.setY(baseY - scrollOffset);
            w.setWidth(cell);
            w.setHeight(Math.max(w.getHeight(), 18));
            rows.add(new RowWidget(w, baseY));
            addRenderableWidget(w);
        }
        cursor += BTN_H + 4;
    }

    private static final int BTN_H = 18;

    protected void gap(int px) {
        cursor += px;
    }

    private void place(AbstractWidget widget, int baseY) {
        widget.setX(contentX());
        widget.setY(baseY - scrollOffset);
    }

    private int maxScroll() {
        return Math.max(0, cursor - contentBottom);
    }

    private void applyScroll() {
        for (RowWidget row : rows) {
            row.widget().setY(row.baseY() - scrollOffset);
            row.widget().visible = row.baseY() - scrollOffset >= contentTop - BTN_H
                    && row.baseY() - scrollOffset <= contentBottom;
        }
        for (RowLabel label : rowLabels) {
            // 标签跟随滚动（渲染时裁剪由可见性近似处理）
        }
    }

    /** 绘制窗口框架 + 全部内容。子类在自绘内容之外调用。 */
    protected void renderPanel(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xB0000000);
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, UiKit.BG_WINDOW);
        GuiUtil.border(context, windowX, windowY, windowWidth, windowHeight, UiKit.BG_PANEL_HOVER);
        context.drawString(this.font, this.title, windowX + 12, windowY + 12, 0xFFFFFFFF);
        context.fill(windowX, windowY + 28, windowX + windowWidth, windowY + 29, UiKit.BORDER);

        context.enableScissor(windowX, contentTop, windowX + windowWidth, contentBottom);
        for (RowWidget row : rows) {
            if (row.widget().visible) {
                row.widget().render(context, mouseX, mouseY, delta);
            }
        }
        for (RowLabel label : rowLabels) {
            int ly = label.baseY() - scrollOffset;
            if (ly >= contentTop && ly <= contentBottom) {
                context.drawString(this.font, label.text(), label.x(), ly, 0xFF909090);
            }
        }
        context.disableScissor();
    }

    /** 滚轮。 */
    protected boolean panelScrolled(double verticalAmount) {
        int max = maxScroll();
        if (max > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (int) (verticalAmount * 20), 0, max);
            applyScroll();
            return true;
        }
        return false;
    }

    /** 滚动到顶部（重建内容后调用）。 */
    protected void resetScroll() {
        scrollOffset = 0;
        applyScroll();
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
