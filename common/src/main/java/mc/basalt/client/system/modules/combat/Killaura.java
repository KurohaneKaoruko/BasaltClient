package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

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
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }

        if (autoDelay.get() && mc.player.getAttackStrengthScale(0.5f) < 1.0f) {
            return;
        }

        LivingEntity target = findTarget();
        if (target == null) {
            return;
        }

        if (rotate.get()) {
            lookAt(target);
        }

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double maxDistSq = range.get() * range.get();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (living == mc.player || !living.isAlive() || living.isDeadOrDying()) {
                continue;
            }

            if (!isTypeEnabled(living)) {
                continue;
            }

            double distSq = mc.player.distanceToSqr(living);
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
        if (entity instanceof Player) {
            return players.get();
        }
        if (entity instanceof Monster) {
            return mobs.get();
        }
        if (entity instanceof Animal) {
            return animals.get();
        }
        return false;
    }

    private boolean canSee(LivingEntity target) {
        Vec3 eye = mc.player.getEyePosition();
        Vec3 center = target.getBoundingBox().getCenter();
        BlockHitResult hit = mc.level.clip(new ClipContext(
                eye, center,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void lookAt(LivingEntity target) {
        AABB box = target.getBoundingBox();
        double dx = box.getCenter().x - mc.player.getX();
        double dy = box.getCenter().y - mc.player.getEyeY();
        double dz = box.getCenter().z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
