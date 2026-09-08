package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;

public class Speed extends Module {
    /** 原版行走速度（格/tick）。 */
    private static final double WALK_SPEED = 0.2873;

    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla", "Strafe");
    private final DoubleSetting multiplier = new DoubleSetting("速度倍率", 1.3, 1.0, 3.0, 0.1);

    public Speed() {
        super("Speed", "速度", "让你移动得更快（Vanilla 蹦跳 / Strafe 平移加速）。", Category.Movement);
        addSetting(mode);
        addSetting(multiplier);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }

        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        if (forward == 0 && strafe == 0) {
            return;
        }

        if ("Vanilla".equals(mode.get())) {
            if (mc.player.isOnGround() && !mc.player.input.jumping) {
                mc.player.jump();
            }
        } else if ("Strafe".equals(mode.get())) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                applyStrafe(forward, strafe);
            } else if (!mc.player.input.jumping) {
                // 空中保持水平速度（原版空中仅 0.02/tick 的控制力）
                applyStrafe(forward, strafe);
            }
        }
    }

    private void applyStrafe(float forward, float strafe) {
        double vx = mc.player.getVelocity().x;
        double vz = mc.player.getVelocity().z;
        double current = Math.sqrt(vx * vx + vz * vz);
        double target = Math.max(current, WALK_SPEED * multiplier.get());
        if (target < 0.01) {
            return;
        }

        double len = Math.sqrt(forward * forward + strafe * strafe);
        if (len < 1.0E-4) {
            return;
        }
        double fx = (forward / len) * target;
        double sx = (strafe / len) * target;

        float yawRad = mc.player.getYaw() * ((float) Math.PI / 180F);
        float sin = (float) Math.sin(yawRad);
        float cos = (float) Math.cos(yawRad);

        mc.player.setVelocity(
                sx * cos - fx * sin,
                mc.player.getVelocity().y,
                fx * cos + sx * sin);
    }
}
