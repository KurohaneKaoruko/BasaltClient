package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.utils.RenderUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Tracers extends Module {
    private final ModeSetting color = new ModeSetting("颜色", "类型颜色", "类型颜色", "白色", "红色", "绿色", "蓝色", "紫色");
    private final BooleanSetting players = new BooleanSetting("玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("怪物", true);
    private final BooleanSetting animals = new BooleanSetting("动物", false);
    private final BooleanSetting others = new BooleanSetting("其他实体", false);
    private final DoubleSetting range = new DoubleSetting("范围", 64.0, 16.0, 128.0, 8.0);

    public Tracers() {
        super("Tracers", "实体连线", "从屏幕中心向实体绘制追踪线。", Category.Render);
        addSetting(color);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(others);
        addSetting(range);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        RenderUtils.begin3D();
        try {
            Vec3 eye = event.getCamera().getPosition();
            float delta = event.getGameTimeDeltaPartialTick();

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!shouldRender(entity)) {
                    continue;
                }

                Vec3 lerped = entity.getPosition(delta);
                AABB box = entity.getBoundingBox().move(
                        lerped.x - entity.getX(),
                        lerped.y - entity.getY(),
                        lerped.z - entity.getZ());
                Vec3 target = box.getCenter();

                RenderUtils.drawLine(event.getMatrices(), event.getCamera(), eye, target, getColor(entity));
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
        return switch (color.get()) {
            case "白色" -> 0xFFFFFFFF;
            case "红色" -> 0xFFFF5555;
            case "绿色" -> 0xFF55CC55;
            case "蓝色" -> 0xFF5588FF;
            case "紫色" -> 0xFFA060F0;
            default -> {
                if (entity instanceof Player) {
                    yield 0xFFFF5555;
                }
                if (entity instanceof Monster) {
                    yield 0xFFE0A030;
                }
                if (entity instanceof Animal) {
                    yield 0xFF55CC55;
                }
                yield 0xFF909090;
            }
        };
    }
}
