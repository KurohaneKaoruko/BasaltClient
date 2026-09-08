package mc.basalt.client.gui;

import mc.basalt.client.utils.GuiUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BasaltClickGUI extends Screen {
    public static final BasaltClickGUI INSTANCE = new BasaltClickGUI();

    private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath(BasaltClient.MOD_ID, "logo.png");
    private static final int LOGO_TEXTURE_WIDTH = 900;
    private static final int LOGO_TEXTURE_HEIGHT = 381;
    /** logo 纹理过滤只需设置一次（双线性 + mipmap，消除缩小采样锯齿）。 */
    private static boolean logoFilterApplied;

    private static final int HEADER_BG = new Color(20, 20, 20, 230).getRGB();
    private static final int MODULE_BG_DISABLED = new Color(30, 30, 30, 180).getRGB();
    private static final int MODULE_BG_ENABLED_START = new Color(40, 40, 40, 230).getRGB();
    private static final int MODULE_BG_ENABLED_END = new Color(20, 20, 20, 230).getRGB();
    private static final int ACCENT_COLOR = new Color(220, 220, 220, 255).getRGB();
    private static final int HOVER_OVERLAY = new Color(255, 255, 255, 20).getRGB();
    private static final int BORDER_COLOR = new Color(60, 60, 60, 255).getRGB();

    private static final int PANEL_X = 16;
    private static final int PANEL_Y = 30;
    private static final int CATEGORY_WIDTH = 132;
    private static final int CATEGORY_GAP = 8;
    private static final int CATEGORY_ROW_GAP = 10;
    private static final int HEADER_HEIGHT = 22;
    private static final int MODULE_HEIGHT = 18;
    private static final int MODULE_GAP = 1;

    private final Map<Category, Boolean> categoryExpanded = new EnumMap<>(Category.class);
    /** 搜索输入框（带光标/占位提示的原生输入体验）。 */
    private EditBox searchBox;

    public BasaltClickGUI() {
        super(Component.literal("ClickGUI"));
        for (Category category : Category.values()) {
            categoryExpanded.put(category, true);
        }
    }

    @Override
    protected void init() {
        searchBox = new EditBox(this.font, PANEL_X, 8, Math.min(230, this.width - PANEL_X * 2), 18,
                Component.literal("搜索"));
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("搜索模块..."));
        this.addRenderableWidget(searchBox);
    }

    private String searchQuery() {
        return searchBox != null ? searchBox.getValue() : "";
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 自绘暗色遮罩。不调用 renderBackground：
        // 1.21.2+ 的模糊后处理为帧末延迟合成，会把本帧已绘制的面板等内容一并覆盖
        context.fill(0, 0, this.width, this.height, 0xC00E0E12);

        // 顶栏按钮悬停状态
        int searchWidthNow = Math.min(230, this.width - PANEL_X * 2);
        int themeX = PANEL_X + searchWidthNow + 6;
        int presetX = themeX + BUTTON_WIDTH + 4;
        gearHover = mouseX >= themeX && mouseX <= themeX + BUTTON_WIDTH && mouseY >= 8 && mouseY <= 26;
        presetHover = mouseX >= presetX && mouseX <= presetX + BUTTON_WIDTH && mouseY >= 8 && mouseY <= 26;

        // 顶栏功能按钮（搜索输入框由 super.render 绘制）
        drawTopButton(context, themeX, 8, "主题", gearHover);
        drawTopButton(context, presetX, 8, "预设", presetHover);

        String tooltipText = null;
        for (CategoryLayout layout : buildLayouts()) {
            Category category = layout.category();
            boolean expanded = categoryExpanded.getOrDefault(category, true);

            context.fill(layout.x(), layout.y(), layout.x() + CATEGORY_WIDTH, layout.y() + HEADER_HEIGHT, HEADER_BG);
            GuiUtil.border(context, layout.x(), layout.y(), CATEGORY_WIDTH, HEADER_HEIGHT, BORDER_COLOR);

            String headerLabel = (expanded ? "- " : "+ ") + category.name;
            Component headerText = Component.literal(headerLabel);
            int textWidth = font.width(headerText);
            context.drawString(font, headerText, layout.x() + (CATEGORY_WIDTH - textWidth) / 2, layout.y() + 7, ACCENT_COLOR);

            if (!expanded) {
                continue;
            }

            int moduleY = layout.y() + HEADER_HEIGHT + 1;
            for (Module module : layout.modules()) {
                boolean isHovered = mouseX >= layout.x() && mouseX <= layout.x() + CATEGORY_WIDTH && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT;

                if (module.isEnabled()) {
                    context.fillGradient(layout.x(), moduleY, layout.x() + CATEGORY_WIDTH, moduleY + MODULE_HEIGHT, MODULE_BG_ENABLED_START, MODULE_BG_ENABLED_END);
                    context.fill(layout.x(), moduleY, layout.x() + 2, moduleY + MODULE_HEIGHT, ThemeManager.get().accent());
                } else {
                    context.fill(layout.x(), moduleY, layout.x() + CATEGORY_WIDTH, moduleY + MODULE_HEIGHT, MODULE_BG_DISABLED);
                }

                if (isHovered) {
                    context.fill(layout.x(), moduleY, layout.x() + CATEGORY_WIDTH, moduleY + MODULE_HEIGHT, HOVER_OVERLAY);
                    tooltipText = module.getDescription();
                }

                GuiUtil.border(context, layout.x(), moduleY, CATEGORY_WIDTH, MODULE_HEIGHT, BORDER_COLOR);

                Component moduleText = Component.literal(module.getDisplayName());
                int moduleTextColor = module.isEnabled() ? 0xFFFFFF : 0xAAAAAA;
                context.drawString(font, moduleText, layout.x() + 6, moduleY + 5, moduleTextColor);

                String bindName = module.getKeyName();
                Component bindText = Component.literal(bindName.equals("NONE") ? "-" : bindName);
                int bindWidth = font.width(bindText);
                context.drawString(font, bindText, layout.x() + CATEGORY_WIDTH - bindWidth - 4, moduleY + 5, 0x909090);

                moduleY += MODULE_HEIGHT + MODULE_GAP;
            }
        }

        if (tooltipText != null) {
            context.renderTooltip(font, Component.literal(tooltipText), mouseX, mouseY);
        }

        // 搜索输入框等原版控件
        super.render(context, mouseX, mouseY, delta);

        context.drawString(
                font,
                Component.literal("左键: 开关  右键: 设置  中键: 绑定  标题: 折叠"),
                10,
                this.height - 14,
                0xB0B0B0
        );

        drawLogo(context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 顶栏功能按钮
        int searchWidth = Math.min(230, this.width - PANEL_X * 2);
        int themeX = PANEL_X + searchWidth + 6;
        int presetX = themeX + BUTTON_WIDTH + 4;
        if (button == 0 && mouseY >= 8 && mouseY <= 26) {
            if (mouseX >= themeX && mouseX <= themeX + BUTTON_WIDTH) {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new ThemeScreen(this));
                }
                return true;
            }
            if (mouseX >= presetX && mouseX <= presetX + BUTTON_WIDTH) {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new PresetScreen(this));
                }
                return true;
            }
        }

        for (CategoryLayout layout : buildLayouts()) {
            int headerX1 = layout.x();
            int headerY1 = layout.y();
            int headerX2 = layout.x() + CATEGORY_WIDTH;
            int headerY2 = layout.y() + HEADER_HEIGHT;

            if (mouseX >= headerX1 && mouseX <= headerX2 && mouseY >= headerY1 && mouseY <= headerY2 && button == 0) {
                categoryExpanded.put(layout.category(), !categoryExpanded.getOrDefault(layout.category(), true));
                return true;
            }

            if (!categoryExpanded.getOrDefault(layout.category(), true)) {
                continue;
            }

            int moduleY = layout.y() + HEADER_HEIGHT + 1;
            for (Module module : layout.modules()) {
                if (mouseX >= layout.x() && mouseX <= layout.x() + CATEGORY_WIDTH && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT) {
                    if (button == 0) {
                        module.toggle();
                        return true;
                    }

                    if (button == 1) {
                        if (minecraft != null) {
                            minecraft.setScreen(new ModuleSettingsScreen(module, this));
                        }
                        return true;
                    }

                    if (button == 2) {
                        // 中键：直接进入按键绑定模式
                        if (minecraft != null) {
                            minecraft.setScreen(new ModuleSettingsScreen(module, this, true));
                        }
                        return true;
                    }
                }
                moduleY += MODULE_HEIGHT + MODULE_GAP;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** 顶栏文字按钮。 */
    private void drawTopButton(GuiGraphics context, int x, int y, String label, boolean hover) {
        context.fill(x, y, x + BUTTON_WIDTH, y + 18, hover ? MODULE_BG_ENABLED_START : HEADER_BG);
        GuiUtil.border(context, x, y, BUTTON_WIDTH, 18, hover ? ThemeManager.get().accent() : BORDER_COLOR);
        context.drawString(font, Component.literal(label), x + (BUTTON_WIDTH - font.width(label)) / 2, y + 5,
                hover ? 0xFFFFFFFF : 0xFFC8C8D0);
    }

    private static final int BUTTON_WIDTH = 44;
    private boolean gearHover;
    private boolean presetHover;

    private List<CategoryLayout> buildLayouts() {
        List<CategoryLayout> layouts = new ArrayList<>();

        int x = PANEL_X;
        int y = PANEL_Y;
        int rowHeight = 0;

        int maxX = this.width - PANEL_X;

        for (Category category : Category.values()) {
            List<Module> modules = getFilteredModules(category);
            boolean expanded = categoryExpanded.getOrDefault(category, true);
            int panelHeight = HEADER_HEIGHT + 1 + (expanded ? modules.size() * (MODULE_HEIGHT + MODULE_GAP) : 0);

            if (x + CATEGORY_WIDTH > maxX) {
                x = PANEL_X;
                y += rowHeight + CATEGORY_ROW_GAP;
                rowHeight = 0;
            }

            layouts.add(new CategoryLayout(category, x, y, modules));

            rowHeight = Math.max(rowHeight, panelHeight);
            x += CATEGORY_WIDTH + CATEGORY_GAP;
        }

        return layouts;
    }

    private List<Module> getFilteredModules(Category category) {
        List<Module> modules = Modules.get().getByCategory(category);
        String query = searchQuery();
        if (query.isBlank()) {
            return modules;
        }

        String needle = query.toLowerCase(Locale.ROOT);
        return modules.stream()
                .filter(module -> module.getName().toLowerCase(Locale.ROOT).contains(needle)
                        || module.getDisplayName().toLowerCase(Locale.ROOT).contains(needle)
                        || module.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    private void drawLogo(GuiGraphics context) {
        int maxLogoSize = 150;
        int margin = 8;
        int logoWidth;
        int logoHeight;

        if (LOGO_TEXTURE_WIDTH >= LOGO_TEXTURE_HEIGHT) {
            logoWidth = maxLogoSize;
            logoHeight = Math.max(1, (int) Math.round((double) maxLogoSize * LOGO_TEXTURE_HEIGHT / LOGO_TEXTURE_WIDTH));
        } else {
            logoHeight = maxLogoSize;
            logoWidth = Math.max(1, (int) Math.round((double) maxLogoSize * LOGO_TEXTURE_WIDTH / LOGO_TEXTURE_HEIGHT));
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 为 logo 纹理启用双线性 + mipmap 过滤（仅需一次），避免大图缩小到 150px 时的锯齿边缘
        if (minecraft != null && !logoFilterApplied) {
            try {
                AbstractTexture texture = minecraft.getTextureManager().getTexture(LOGO);
                RenderSystem.bindTexture(texture.getId());
                texture.setFilter(true, true);
                logoFilterApplied = true;
            } catch (Exception ignored) {
                // 纹理尚未就绪时下一帧重试
            }
        }

        context.blit(
                RenderType::guiTextured,
                LOGO,
                this.width - logoWidth - margin,
                this.height - logoHeight - margin,
                logoWidth,
                logoHeight,
                0,
                0,
                LOGO_TEXTURE_WIDTH,
                LOGO_TEXTURE_HEIGHT,
                LOGO_TEXTURE_WIDTH,
                LOGO_TEXTURE_HEIGHT
        );

        RenderSystem.disableBlend();
    }

    /** 禁用原版背景（毛玻璃为帧末延迟合成，会覆盖 GUI 内容）。 */
    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record CategoryLayout(Category category, int x, int y, List<Module> modules) {
    }
}
