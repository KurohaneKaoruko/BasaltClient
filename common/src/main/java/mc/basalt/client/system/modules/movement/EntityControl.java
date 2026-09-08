package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityControl extends Module {
    private final DoubleSetting speed = new DoubleSetting("速度", 1.0, 0.1, 5.0, 0.1);

    public EntityControl() {
        super("EntityControl", "实体控制", "强制控制骑行的实体飞行。", Category.Movement);
        addSetting(speed);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || !mc.player.isPassenger())
            return;

        Entity vehicle = mc.player.getVehicle();

        // Control rotation
        vehicle.setYRot(mc.player.getYRot());
        vehicle.setXRot(mc.player.getXRot());

        // Control movement
        double s = speed.get();
        Vec3 vel = Vec3.ZERO;

        double forward = 0;
        double strafe = 0;
        double vertical = 0;

        if (mc.options.keyUp.isDown())
            forward++;
        if (mc.options.keyDown.isDown())
            forward--;
        if (mc.options.keyLeft.isDown())
            strafe++;
        if (mc.options.keyRight.isDown())
            strafe--;
        if (mc.options.keyJump.isDown())
            vertical++;

        // Calculate velocity vector（手动三角函数，避免映射差异）
        if (forward != 0 || strafe != 0) {
            float yawRad = mc.player.getYRot() * ((float) Math.PI / 180F);
            float sin = (float) Math.sin(yawRad);
            float cos = (float) Math.cos(yawRad);
            double mx = (strafe * cos - forward * sin) * s;
            double mz = (forward * cos + strafe * sin) * s;
            vel = new Vec3(mx, 0, mz);
        }

        // Apply vertical
        if (vertical > 0) {
            vel = vel.add(0, s, 0);
        } else {
            // Anti-gravity (hover)
            vel = vel.add(0, 0, 0);
        }

        // Set velocity
        if (forward != 0 || strafe != 0 || vertical != 0) {
            vehicle.setDeltaMovement(vel);
        } else {
            vehicle.setDeltaMovement(0, 0, 0);
        }
    }
}
