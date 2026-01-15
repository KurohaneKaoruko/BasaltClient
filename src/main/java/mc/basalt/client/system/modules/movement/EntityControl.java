package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntityControl extends Module {
    private final DoubleSetting speed = new DoubleSetting("速度", 1.0, 0.1, 5.0, 0.1);

    public EntityControl() {
        super("EntityControl", "实体控制", "强制控制骑行的实体飞行。", Category.Movement);
        addSetting(speed);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || !mc.player.hasVehicle())
            return;

        Entity vehicle = mc.player.getVehicle();

        // Control rotation
        vehicle.setYaw(mc.player.getYaw());
        vehicle.setPitch(mc.player.getPitch());

        // Control movement
        double s = speed.get();
        Vec3d vel = Vec3d.ZERO;

        double forward = 0;
        double strafe = 0;
        double vertical = 0;

        if (mc.options.forwardKey.isPressed())
            forward++;
        if (mc.options.backKey.isPressed())
            forward--;
        if (mc.options.leftKey.isPressed())
            strafe++;
        if (mc.options.rightKey.isPressed())
            strafe--;
        if (mc.options.jumpKey.isPressed())
            vertical++;

        // Calculate velocity vector
        if (forward != 0 || strafe != 0) {
            float yaw = mc.player.getYaw();
            Vec3d forwardVec = Vec3d.fromPolar(0, yaw).multiply(forward);
            Vec3d strafeVec = Vec3d.fromPolar(0, yaw - 90).multiply(strafe);
            vel = forwardVec.add(strafeVec).normalize().multiply(s);
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
            vehicle.setVelocity(vel);
        } else {
            vehicle.setVelocity(0, 0, 0);
        }
    }
}
