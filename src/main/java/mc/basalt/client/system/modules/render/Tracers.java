package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import mc.basalt.client.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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
        if (mc.player == null || mc.world == null) {
            return;
        }

        RenderUtils.begin3D();
        try {
            Vec3d eye = event.getCamera().getPos();
            float delta = event.getTickDelta();

            for (Entity entity : mc.world.getEntities()) {
                if (!shouldRender(entity)) {
                    continue;
                }

                Vec3d lerped = entity.getLerpedPos(delta);
                Box box = entity.getBoundingBox().offset(
                        lerped.x - entity.getX(),
                        lerped.y - entity.getY(),
                        lerped.z - entity.getZ());
                Vec3d target = box.getCenter();

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
        if (mc.player.squaredDistanceTo(entity) > range.get() * range.get()) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            return players.get();
        }
        if (entity instanceof Monster) {
            return mobs.get();
        }
        if (entity instanceof AnimalEntity) {
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
                if (entity instanceof PlayerEntity) {
                    yield 0xFFFF5555;
                }
                if (entity instanceof Monster) {
                    yield 0xFFE0A030;
                }
                if (entity instanceof AnimalEntity) {
                    yield 0xFF55CC55;
                }
                yield 0xFF909090;
            }
        };
    }
}
