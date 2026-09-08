package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.utils.RenderUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ESP extends Module {
    private final BooleanSetting boxes = new BooleanSetting("方框", true);
    private final BooleanSetting fill = new BooleanSetting("填充", false);
    private final BooleanSetting tracerLines = new BooleanSetting("连线", false);
    private final BooleanSetting players = new BooleanSetting("玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("怪物", true);
    private final BooleanSetting animals = new BooleanSetting("动物", false);
    private final BooleanSetting others = new BooleanSetting("其他实体", false);
    private final DoubleSetting range = new DoubleSetting("范围", 64.0, 16.0, 128.0, 8.0);
    private final DoubleSetting fillAlpha = new DoubleSetting("填充透明度", 0.2, 0.05, 0.6, 0.05);

    public ESP() {
        super("ESP", "实体透视", "在实体周围绘制高亮框与连线，可穿透墙壁。", Category.Render);
        addSetting(boxes);
        addSetting(fill);
        addSetting(tracerLines);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(others);
        addSetting(range);
        addSetting(fillAlpha);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!boxes.get() && !fill.get() && !tracerLines.get()) {
            return;
        }

        RenderUtils.begin3D();
        try {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!shouldRender(entity)) {
                    continue;
                }

                float delta = event.getGameTimeDeltaPartialTick();
                Vec3 lerped = entity.getPosition(delta);
                AABB box = entity.getBoundingBox().move(
                        lerped.x - entity.getX(),
                        lerped.y - entity.getY(),
                        lerped.z - entity.getZ());

                int color = getColor(entity);

                if (fill.get()) {
                    int alpha = (int) (fillAlpha.get() * 255);
                    int fillColor = (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
                    RenderUtils.drawBoxFill(event.getMatrices(), event.getCamera(), box, fillColor);
                }

                if (boxes.get()) {
                    RenderUtils.drawBoxOutline(event.getMatrices(), event.getCamera(), box, color);
                }

                if (tracerLines.get()) {
                    Vec3 eye = event.getCamera().getPosition();
                    RenderUtils.drawLine(event.getMatrices(), event.getCamera(), eye, box.getCenter(), color);
                }
            }
        } finally {
            RenderUtils.end3D();
        }
    }

    private boolean shouldRender(Entity entity) {
        if (entity == mc.player || !entity.isAlive()) {
            return false;
        }
        if (!(entity instanceof LivingEntity)) {
            return others.get();
        }
        if (mc.player.distanceToSqr(entity) > range.get() * range.get()) {
            return false;
        }
        return isTypeEnabled(entity);
    }

    private boolean isTypeEnabled(Entity entity) {
        if (entity instanceof Player) {
            return players.get();
        }
        if (entity instanceof Monster) {
            return mobs.get();
        }
        if (entity instanceof Animal) {
            return animals.get();
        }
        return others.get();
    }

    private int getColor(Entity entity) {
        if (entity instanceof Player) {
            return 0xFFFF5555;
        }
        if (entity instanceof Monster) {
            return 0xFFE0A030;
        }
        if (entity instanceof Animal) {
            return 0xFF55CC55;
        }
        return 0xFF909090;
    }
}
