package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla", "Strafe", "BHop", "YPort");
    private final DoubleSetting speedAmount = new DoubleSetting("速度", 5.6, 0.5, 20.0, 0.1);
    private final DoubleSetting strafeSpeed = new DoubleSetting("Strafe速度", 1.6, 0.1, 3.0, 0.05);
    private final BooleanSetting speedLimit = new BooleanSetting("速度限制(严格AC)", false);
    private final BooleanSetting inLiquids = new BooleanSetting("液体中生效", false);
    private final BooleanSetting whenSneaking = new BooleanSetting("潜行时生效", false);
    private final BooleanSetting onlyOnGround = new BooleanSetting("仅地面生效", false);
    private final DoubleSetting timerSpeed = new DoubleSetting("Timer加速", 1.0, 1.0, 5.0, 0.1);

    public Speed() {
        super("Speed", "速度", "多种移动加速模式。", Category.Movement);
        addSetting(mode); addSetting(speedAmount); addSetting(strafeSpeed); addSetting(speedLimit);
        addSetting(inLiquids); addSetting(whenSneaking); addSetting(onlyOnGround); addSetting(timerSpeed);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (stopSpeed()) {
            BasaltClient.TIMER = 1.0f;
            return;
        }

        // Timer 加速
        BasaltClient.TIMER = timerSpeed.get().floatValue();

        float forward = mc.player.input.forwardImpulse > 0 ? 1 : 0;
        float strafe = mc.player.input.leftImpulse > 0 ? 1 : 0;
        if (mc.player.input.forwardImpulse < 0) forward -= 1;
        if (mc.player.input.leftImpulse < 0) strafe -= 1;
        if (forward == 0 && strafe == 0) return;

        switch (mode.get()) {
            case "Vanilla" -> vanillaSpeed();
            case "Strafe" -> strafeMode();
            case "BHop" -> bhopMode();
            case "YPort" -> yportMode();
        }
    }

    private void vanillaSpeed() {
        double bps = speedAmount.get() / 20.0; // blocks per tick
        if (onlyOnGround.get() && !mc.player.onGround()) return;

        if (mc.player.onGround()) {
            applySpeed(bps);
        } else {
            applySpeed(bps * 0.85); // 空中略慢
        }
    }

    private void strafeMode() {
        double target = strafeSpeed.get() * 0.2873;
        if (speedLimit.get()) target = Math.min(target, 0.28);

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            applySpeed(target);
        } else {
            double cur = Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x
                + mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
            if (cur < target) {
                applySpeed(target);
            }
        }
    }

    private void bhopMode() {
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            applySpeed(speedAmount.get() / 20.0 * 1.2);
        }
    }

    private void yportMode() {
        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            applySpeed(speedAmount.get() / 20.0);
        } else if (mc.player.getDeltaMovement().y > 0) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -0.1, mc.player.getDeltaMovement().z);
        }
    }

    private void applySpeed(double targetSpeed) {
        float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
        float sin = (float) Math.sin(yaw);
        float cos = (float) Math.cos(yaw);

        float forward = mc.player.input.forwardImpulse > 0 ? 1 : (mc.player.input.forwardImpulse < 0 ? -1 : 0);
        float strafe = mc.player.input.leftImpulse > 0 ? 1 : (mc.player.input.leftImpulse < 0 ? -1 : 0);
        if (forward == 0 && strafe == 0) return;

        double vx = (strafe * cos - forward * sin) * targetSpeed;
        double vz = (forward * cos + strafe * sin) * targetSpeed;
        mc.player.setDeltaMovement(vx, mc.player.getDeltaMovement().y, vz);
    }

    private boolean stopSpeed() {
        if (mc.player.isFallFlying() || mc.player.onClimbable() || mc.player.getVehicle() != null) return true;
        if (!whenSneaking.get() && mc.player.isShiftKeyDown()) return true;
        if (!inLiquids.get() && (mc.player.isInWater() || mc.player.isInLava())) return true;
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.TIMER = 1.0f;
    }
}
