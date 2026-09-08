package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Nametags extends Module {
    private static final int BACKGROUND_COLOR = 0x75000000;
    private static final int FULL_LIGHT = 0xF000F0;

    private final DoubleSetting scale = new DoubleSetting("缩放", 1.0, 0.5, 3.0, 0.1);
    private final BooleanSetting showHealth = new BooleanSetting("显示血量", true);
    private final BooleanSetting showDistance = new BooleanSetting("显示距离", false);
    private final BooleanSetting showMobs = new BooleanSetting("显示怪物", false);

    public Nametags() {
        super("Nametags", "名牌显示", "放大并透过墙壁显示实体名牌与血量。", Category.Render);
        addSetting(scale);
        addSetting(showHealth);
        addSetting(showDistance);
        addSetting(showMobs);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || mc.textRenderer == null) {
            return;
        }

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        Vec3d camPos = event.getCamera().getPos();
        float delta = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !entity.isAlive()) {
                continue;
            }

            boolean isPlayer = entity instanceof PlayerEntity;
            if (!isPlayer && !(showMobs.get() && entity instanceof Monster)) {
                continue;
            }

            Vec3d lerped = entity.getLerpedPos(delta);
            double tagY = lerped.y + entity.getHeight() + 0.4;

            event.getMatrices().push();
            event.getMatrices().translate(lerped.x - camPos.x, tagY - camPos.y, lerped.z - camPos.z);
            // 撤销相机旋转 → 始终面向屏幕（公告板）
            event.getMatrices().multiply(event.getCamera().getRotation().conjugate(new Quaternionf()));
            float s = 0.025f * scale.get().floatValue();
            event.getMatrices().scale(-s, -s, s);
            Matrix4f matrix = event.getMatrices().peek().getPositionMatrix();

            TextRenderer textRenderer = mc.textRenderer;
            String name = entity.getDisplayName().getString();
            int nameWidth = textRenderer.getWidth(name);
            textRenderer.draw(Text.literal(name), -nameWidth / 2f, 0, 0xFFFFFFFF, true,
                    matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, BACKGROUND_COLOR, FULL_LIGHT);

            int lineY = 10;

            if (showHealth.get() && entity instanceof LivingEntity living) {
                float health = living.getHealth();
                float max = Math.max(1.0f, living.getMaxHealth());
                String hp = String.format("%.0f / %.0f HP", health, max);
                int hpWidth = textRenderer.getWidth(hp);
                textRenderer.draw(Text.literal(hp), -hpWidth / 2f, lineY, healthColor(health / max), true,
                        matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, BACKGROUND_COLOR, FULL_LIGHT);
                lineY += 10;
            }

            if (showDistance.get()) {
                String dist = String.format("%.1fm", mc.player.distanceTo(entity));
                int distWidth = textRenderer.getWidth(dist);
                textRenderer.draw(Text.literal(dist), -distWidth / 2f, lineY, 0xFFB0B0B0, true,
                        matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, BACKGROUND_COLOR, FULL_LIGHT);
            }

            event.getMatrices().pop();
        }

        immediate.draw();
    }

    private int healthColor(float fraction) {
        fraction = Math.max(0.0f, Math.min(1.0f, fraction));
        if (fraction > 0.5f) {
            // 绿 → 黄
            float t = (fraction - 0.5f) * 2.0f;
            int r = (int) (0xE0 * (1.0f - t));
            return 0xFF000000 | (r << 16) | (0xCC << 8) | 0x44;
        }
        // 黄 → 红
        float t = fraction * 2.0f;
        int g = (int) (0xCC * t);
        return 0xFF000000 | (0xE0 << 16) | (g << 8) | 0x44;
    }
}
