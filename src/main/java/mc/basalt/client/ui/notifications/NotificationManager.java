package mc.basalt.client.ui.notifications;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 右下角弹出式通知：模块开关、命令反馈等消息以淡入淡出的卡片展示。
 */
public final class NotificationManager {
    /** 通知类型（决定左侧色条颜色）。 */
    public enum Type {
        INFO(0xFF4A9EEF),
        SUCCESS(0xFF3FBF6F),
        WARN(0xFFE0A030),
        ERROR(0xFFE05555);

        private final int color;

        Type(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    private static class Notification {
        final String title;
        final String message;
        final Type type;
        final long createdMs;
        final long durationMs;

        Notification(String title, String message, Type type, long durationMs) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdMs = System.currentTimeMillis();
            this.durationMs = durationMs;
        }

        float progress(long now) {
            return Math.min(1.0f, (now - createdMs) / (float) Math.max(1, durationMs));
        }
    }

    private static final List<Notification> ACTIVE = new CopyOnWriteArrayList<>();
    private static final int MAX_NOTIFICATIONS = 5;
    private static final long SLIDE_IN_MS = 150;
    private static final long FADE_OUT_MS = 400;

    private NotificationManager() {
    }

    public static void show(String title, String message, Type type, long durationMs) {
        // 游戏未进入世界时（如启动时加载配置）不弹通知
        if (BasaltClient.MC == null || BasaltClient.MC.player == null) {
            return;
        }

        ACTIVE.add(new Notification(title, message, type, durationMs));
        while (ACTIVE.size() > MAX_NOTIFICATIONS) {
            ACTIVE.remove(0);
        }
    }

    public static void info(String title, String message) {
        show(title, message, Type.INFO, 2000);
    }

    public static void success(String title, String message) {
        show(title, message, Type.SUCCESS, 2000);
    }

    public static void warn(String title, String message) {
        show(title, message, Type.WARN, 3000);
    }

    public static void error(String title, String message) {
        show(title, message, Type.ERROR, 3000);
    }

    /** 由 HudManager 每帧调用，绘制在屏幕右下角。 */
    public static void render(DrawContext context) {
        if (ACTIVE.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<Notification> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Notification n = iterator.next();
            if (now - n.createdMs > n.durationMs) {
                ACTIVE.remove(n);
            }
        }

        if (ACTIVE.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = BasaltClient.MC.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int lineH = textRenderer.fontHeight + 1;
        int boxHeight = lineH * 2 + 6;
        int gap = 3;

        // 从底部往上排布（避开快捷栏区域）
        int y = context.getScaledWindowHeight() - 40;

        for (int i = ACTIVE.size() - 1; i >= 0; i--) {
            Notification n = ACTIVE.get(i);
            long age = now - n.createdMs;
            long remaining = n.durationMs - age;

            // 滑入与淡出
            float slide = Math.min(1.0f, age / (float) SLIDE_IN_MS);
            float alpha = 1.0f;
            if (remaining < FADE_OUT_MS) {
                alpha = Math.max(0.0f, remaining / (float) FADE_OUT_MS);
            }
            int xOffset = (int) ((1.0f - slide) * 12);

            int titleWidth = textRenderer.getWidth(n.title);
            int msgWidth = textRenderer.getWidth(n.message);
            int boxWidth = Math.max(titleWidth, msgWidth) + 14;

            int x1 = screenWidth - boxWidth - 4 + xOffset;
            int y1 = y - boxHeight;

            int bgAlpha = (int) (0xC8 * alpha);
            int barAlpha = (int) (0xFF * alpha);
            int textAlpha = (int) (0xFF * alpha);

            // 背景
            context.fill(x1, y1, x1 + boxWidth, y1 + boxHeight, (bgAlpha << 24));
            // 左侧类型色条
            int barColor = (barAlpha << 24) | (n.type.getColor() & 0xFFFFFF);
            context.fill(x1, y1, x1 + 2, y1 + boxHeight, barColor);
            // 边框
            int border = ((int) (0x50 * alpha) << 24) | 0xFFFFFF;
            context.drawBorder(x1, y1, boxWidth, boxHeight, border);

            // 文本
            int titleColor = 0xFFFFFF | (textAlpha << 24);
            int msgColor = 0xB0B0B0 | (textAlpha << 24);
            context.drawTextWithShadow(textRenderer, n.title, x1 + 7, y1 + 4, titleColor);
            context.drawTextWithShadow(textRenderer, n.message, x1 + 7, y1 + 4 + lineH, msgColor);

            y = y1 - gap;
        }
    }
}
