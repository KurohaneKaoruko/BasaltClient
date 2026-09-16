package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 4.5, 1.0, 6.0, 0.1);
    private final DoubleSetting fov = new DoubleSetting("视野角度", 60.0, 10.0, 180.0, 5.0);
    private final DoubleSetting speed = new DoubleSetting("平滑速度", 8.0, 1.0, 20.0, 0.5);
    private final BooleanSetting players = new BooleanSetting("瞄准玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("瞄准怪物", true);
    private final BooleanSetting onlyWhenAttack = new BooleanSetting("仅按攻击键时", true);

    public AimAssist() {
        super("AimAssist", "瞄准辅助", "平滑地把准星转向目标（拟真旋转，反检测）。", Category.Combat);
        addSetting(range);
        addSetting(fov);
        addSetting(speed);
        addSetting(players);
        addSetting(mobs);
        addSetting(onlyWhenAttack);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;
        if (onlyWhenAttack.get() && !mc.options.keyAttack.isDown()) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        // 平滑旋转（每 tick 只转一部分角度，模拟人手）
        Vec3 center = target.getBoundingBox().getCenter();
        double dx = center.x - mc.player.getX();
        double dy = center.y - mc.player.getEyeY();
        double dz = center.z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        float factor = (float) (speed.get() / 20.0);

        float yawDelta = Mth.wrapDegrees(targetYaw - mc.player.getYRot());
        float pitchDelta = Mth.wrapDegrees(targetPitch - mc.player.getXRot());

        mc.player.setYRot(mc.player.getYRot() + yawDelta * factor);
        mc.player.setXRot(Mth.clamp(mc.player.getXRot() + pitchDelta * factor, -90.0f, 90.0f));
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestAngle = Double.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player || !entity.isAlive()) continue;

            if (entity instanceof Player) {
                if (!players.get()) continue;
            } else if (entity instanceof Monster) {
                if (!mobs.get()) continue;
            } else {
                continue;
            }

            if (mc.player.distanceToSqr(entity) > range.get() * range.get()) continue;

            double angle = getAngleTo(entity);
            if (angle > fov.get() / 2.0) continue;

            if (angle < bestAngle) {
                bestAngle = angle;
                best = living;
            }
        }
        return best;
    }

    private double getAngleTo(Entity entity) {
        Vec3 center = entity.getBoundingBox().getCenter();
        double dx = center.x - mc.player.getX();
        double dy = center.y - mc.player.getEyeY();
        double dz = center.z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        float yawDiff = Math.abs(Mth.wrapDegrees(yaw - mc.player.getYRot()));
        float pitchDiff = Math.abs(pitch - mc.player.getXRot());
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }
}
