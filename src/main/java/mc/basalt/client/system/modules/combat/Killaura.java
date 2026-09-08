package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Killaura extends Module {
    private final DoubleSetting range = new DoubleSetting("距离", 4.0, 1.0, 6.0, 0.1);
    private final BooleanSetting players = new BooleanSetting("攻击玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("攻击怪物", true);
    private final BooleanSetting animals = new BooleanSetting("攻击动物", false);
    private final BooleanSetting autoDelay = new BooleanSetting("自动冷却", true);
    private final BooleanSetting rotate = new BooleanSetting("旋转视角", true);
    private final BooleanSetting throughWalls = new BooleanSetting("透过墙壁", false);

    public Killaura() {
        super("Killaura", "杀戮光环", "自动攻击周围最近的实体。", Category.Combat);
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(autoDelay);
        addSetting(rotate);
        addSetting(throughWalls);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            return;
        }

        if (autoDelay.get() && mc.player.getAttackCooldownProgress(0.5f) < 1.0f) {
            return;
        }

        LivingEntity target = findTarget();
        if (target == null) {
            return;
        }

        if (rotate.get()) {
            lookAt(target);
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double maxDistSq = range.get() * range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (living == mc.player || !living.isAlive() || living.isDead()) {
                continue;
            }

            if (!isTypeEnabled(living)) {
                continue;
            }

            double distSq = mc.player.squaredDistanceTo(living);
            if (distSq > maxDistSq || distSq >= bestDist) {
                continue;
            }

            if (!throughWalls.get() && !canSee(living)) {
                continue;
            }

            best = living;
            bestDist = distSq;
        }

        return best;
    }

    private boolean isTypeEnabled(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            return players.get();
        }
        if (entity instanceof Monster) {
            return mobs.get();
        }
        if (entity instanceof AnimalEntity) {
            return animals.get();
        }
        return false;
    }

    private boolean canSee(LivingEntity target) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d center = target.getBoundingBox().getCenter();
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                eye, center,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void lookAt(LivingEntity target) {
        Box box = target.getBoundingBox();
        double dx = box.getCenter().x - mc.player.getX();
        double dy = box.getCenter().y - mc.player.getEyeY();
        double dz = box.getCenter().z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
