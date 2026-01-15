package mc.basalt.client.gui;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.util.List;

public class BasaltClickGUI extends Screen {
    public static final BasaltClickGUI INSTANCE = new BasaltClickGUI();
    private static final Identifier LOGO = Identifier.of(BasaltClient.MOD_ID, "logo.png");
    private static final int LOGO_TEXTURE_WIDTH = 3165;
    private static final int LOGO_TEXTURE_HEIGHT = 1340;

    // Basalt Tech Theme Colors
    // Glass/Obsidian Background (Dark with transparency)
    private static final int HEADER_BG = new Color(20, 20, 20, 230).getRGB();
    private static final int MODULE_BG_DISABLED = new Color(30, 30, 30, 180).getRGB();

    // Tech Accent (Deep Lava/Basalt Glow) - You can change to Cyan/White if
    // preferred
    private static final int MODULE_BG_ENABLED_START = new Color(40, 40, 40, 230).getRGB();
    private static final int MODULE_BG_ENABLED_END = new Color(20, 20, 20, 230).getRGB();

    // Accent Line (White/Light Gray for clean tech look)
    private static final int ACCENT_COLOR = new Color(220, 220, 220, 255).getRGB();

    private static final int HOVER_OVERLAY = new Color(255, 255, 255, 20).getRGB();
    private static final int BORDER_COLOR = new Color(60, 60, 60, 255).getRGB();

    // Removed Custom Font Identifier to fix rendering issues
    // private static final Identifier CUSTOM_FONT =
    // Identifier.of(BasaltClient.MOD_ID, "source_han_sans");

    public BasaltClickGUI() {
        super(Text.of("ClickGUI"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        int startX = 20;
        int startY = 20;
        int width = 110;
        int padding = 10;

        String tooltipText = null;

        for (Category category : Category.values()) {
            // Render Category Header
            int headerHeight = 22;

            // Header Background (Glassy Dark)
            context.fill(startX, startY, startX + width, startY + headerHeight, HEADER_BG);
            // Header Border
            context.drawBorder(startX, startY, width, headerHeight, BORDER_COLOR);

            // Centered Header Text
            Text headerText = Text.literal(category.name); // Removed custom font style
            int textWidth = textRenderer.getWidth(headerText);
            context.drawTextWithShadow(textRenderer, headerText, startX + (width - textWidth) / 2, startY + 7,
                    ACCENT_COLOR);

            // Render Modules
            int moduleY = startY + headerHeight + 1; // +1 spacing
            List<Module> modules = Modules.get().getByCategory(category);

            for (Module module : modules) {
                boolean isHovered = mouseX >= startX && mouseX <= startX + width && mouseY >= moduleY
                        && mouseY <= moduleY + 18;

                // Background
                if (module.isEnabled()) {
                    // Gradient for enabled modules
                    context.fillGradient(startX, moduleY, startX + width, moduleY + 18, MODULE_BG_ENABLED_START,
                            MODULE_BG_ENABLED_END);
                    // Left Accent Bar for enabled modules
                    context.fill(startX, moduleY, startX + 2, moduleY + 18, ACCENT_COLOR);
                } else {
                    context.fill(startX, moduleY, startX + width, moduleY + 18, MODULE_BG_DISABLED);
                }

                // Hover Effect
                if (isHovered) {
                    context.fill(startX, moduleY, startX + width, moduleY + 18, HOVER_OVERLAY);
                    tooltipText = module.getDescription();
                }

                // Module Border (Subtle 1px outline for tech feel)
                context.drawBorder(startX, moduleY, width, 18, BORDER_COLOR);

                // Module Name
                Text moduleText = Text.literal(module.getDisplayName()); // Removed custom font style
                int textColor = module.isEnabled() ? 0xFFFFFF : 0xAAAAAA;
                context.drawTextWithShadow(textRenderer, moduleText, startX + 6, moduleY + 5, textColor);

                moduleY += 19; // 18 height + 1 spacing
            }

            startX += width + padding;
        }

        // Render tooltip last so it's on top
        if (tooltipText != null) {
            context.drawTooltip(textRenderer, Text.literal(tooltipText), mouseX, mouseY);
        }

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

        context.drawTexture(
                LOGO,
                this.width - logoWidth - margin,
                this.height - logoHeight - margin,
                logoWidth,
                logoHeight,
                0.0f,
                0.0f,
                LOGO_TEXTURE_WIDTH,
                LOGO_TEXTURE_HEIGHT,
                LOGO_TEXTURE_WIDTH,
                LOGO_TEXTURE_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = 20;
        int startY = 20;
        int width = 110;
        int padding = 10;

        for (Category category : Category.values()) {
            int headerHeight = 22;
            int moduleY = startY + headerHeight + 1;
            List<Module> modules = Modules.get().getByCategory(category);

            for (Module module : modules) {
                if (mouseX >= startX && mouseX <= startX + width && mouseY >= moduleY && mouseY <= moduleY + 18) {
                    if (button == 0) { // Left click to toggle
                        module.toggle();
                        return true;
                    } else if (button == 1) { // Right click for settings
                        if (client != null) {
                            client.setScreen(new ModuleSettingsScreen(module, this));
                        }
                        return true;
                    }
                }
                moduleY += 19;
            }
            startX += width + padding;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
