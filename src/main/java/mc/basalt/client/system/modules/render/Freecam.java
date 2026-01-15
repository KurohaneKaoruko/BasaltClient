package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {
    private final DoubleSetting speed = new DoubleSetting("速度", 1.0, 0.1, 5.0, 0.1);

    public Freecam() {
        super("Freecam", "自由视角", "允许相机脱离身体自由移动。", Category.Render);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            BasaltClient.FREECAM_X = mc.player.getX();
            BasaltClient.FREECAM_Y = mc.player.getEyeY(); // Use eye height for camera start
            BasaltClient.FREECAM_Z = mc.player.getZ();
            BasaltClient.FREECAM_YAW = mc.player.getYaw();
            BasaltClient.FREECAM_PITCH = mc.player.getPitch();
            BasaltClient.FREECAM_ENABLED = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.FREECAM_ENABLED = false;
        // Optional: teleport player to camera? usually not for freecam.
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!BasaltClient.FREECAM_ENABLED || mc.player == null)
            return;

        // Freeze player input (handled by not passing inputs to server?
        // Or overriding input. For now let's just update camera pos locally)
        // To prevent player movement, we could cancel movement packets or override
        // input.
        // A simple way is setting player velocity to 0 or resetting position
        // (rubberband).
        // Better: MixinClientPlayerEntity to ignore input when Freecam is on.

        // Update Freecam position based on keyboard input
        float forward = 0;
        float strafe = 0;
        float vertical = 0;

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
        if (mc.options.sneakKey.isPressed())
            vertical--;

        float yaw = BasaltClient.FREECAM_YAW;
        float pitch = BasaltClient.FREECAM_PITCH;

        // Update rotation from player (since we don't block mouse input yet, player
        // rotates)
        // Ideally we detach rotation too.
        // For now, let's sync camera rotation with player rotation for control,
        // but keep position separate.
        BasaltClient.FREECAM_YAW = mc.player.getYaw();
        BasaltClient.FREECAM_PITCH = mc.player.getPitch();

        double s = speed.get();

        // Calculate movement vector
        double sin = Math.sin(Math.toRadians(yaw + 90));
        double cos = Math.cos(Math.toRadians(yaw + 90)); // Forward is +90 deg relative to sin/cos standard?
        // Standard Minecraft yaw: 0=South(+Z), 90=West(-X), 180=North(-Z), 270=East(+X)
        // Math: 0=East(+X), 90=North(+Y)...

        // Let's use Vec3d.fromPolar
        Vec3d forwardVec = Vec3d.fromPolar(0, yaw).multiply(forward * s);
        Vec3d strafeVec = Vec3d.fromPolar(0, yaw - 90).multiply(strafe * s); // -90 for left strafe
        // Actually fromPolar(pitch, yaw)

        double mx = 0;
        double mz = 0;

        // Manual calc to ensure horizontal only
        float f = yaw * ((float) Math.PI / 180F);
        float h = MathHelper.cos(f);
        float i = MathHelper.sin(f);

        // Forward
        mx += (double) (i * -forward);
        mz += (double) (h * forward);

        // Strafe
        float f2 = (yaw - 90) * ((float) Math.PI / 180F);
        float h2 = MathHelper.cos(f2);
        float i2 = MathHelper.sin(f2);

        mx += (double) (i2 * -strafe);
        mz += (double) (h2 * strafe);

        BasaltClient.FREECAM_X += mx * s;
        BasaltClient.FREECAM_Z += mz * s;
        BasaltClient.FREECAM_Y += vertical * s;

        // Prevent player from moving by canceling input?
        // Simple hack: set player velocity to 0
        mc.player.setVelocity(0, 0, 0);
    }
}
