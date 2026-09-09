package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;

public class Flight extends Module {
    private static final double DEFAULT_FLY_SPEED = 0.05;

    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla", "Static", "Jetpack", "Glide");
    private final DoubleSetting speed = new DoubleSetting("飞行速度", 1.0, 0.1, 5.0, 0.1);
    private final DoubleSetting verticalSpeed = new DoubleSetting("垂直速度", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting antiKick = new BooleanSetting("反踢出", false);
    private final BooleanSetting inLiquids = new BooleanSetting("液体中飞行", true);

    private int antiKickTimer;

    public Flight() {
        super("Flight", "飞行", "多种飞行模式，可调速度与反检测。", Category.Movement);
        addSetting(mode); addSetting(speed); addSetting(verticalSpeed); addSetting(antiKick); addSetting(inLiquids);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!inLiquids.get() && (mc.player.isInWater() || mc.player.isInLava())) return;

        switch (mode.get()) {
            case "Vanilla" -> vanillaFlight();
            case "Static" -> staticFlight();
            case "Jetpack" -> jetpack();
            case "Glide" -> glide();
        }

        if (antiKick.get()) {
            antiKickTimer++;
            if (antiKickTimer >= 80) {
                antiKickTimer = 0;
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -0.04, mc.player.getDeltaMovement().z);
            }
        }
    }

    private void vanillaFlight() {
        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().setFlyingSpeed(speed.get().floatValue() * (float) DEFAULT_FLY_SPEED);
        if (!mc.player.getAbilities().flying) {
            mc.player.getAbilities().flying = true;
        }
    }

    private void staticFlight() {
        mc.player.getAbilities().mayfly = true;
        mc.player.noPhysics = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlyingSpeed(speed.get().floatValue() * (float) DEFAULT_FLY_SPEED);
        mc.player.setDeltaMovement(0, 0, 0);
        double s = speed.get() * DEFAULT_FLY_SPEED * 10;
        if (mc.options.keyUp.isDown()) {
            float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
            mc.player.setDeltaMovement(-Math.sin(yaw) * s, 0, Math.cos(yaw) * s);
        }
        if (mc.options.keyDown.isDown()) {
            float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
            mc.player.setDeltaMovement(Math.sin(yaw) * s, 0, -Math.cos(yaw) * s);
        }
        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, verticalSpeed.get() * 0.5, mc.player.getDeltaMovement().z);
        }
        if (mc.options.keyShift.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -verticalSpeed.get() * 0.5, mc.player.getDeltaMovement().z);
        }
    }

    private void jetpack() {
        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x,
                Math.min(mc.player.getDeltaMovement().y + 0.1 * verticalSpeed.get(), 0.5),
                mc.player.getDeltaMovement().z);
        }
        if (mc.options.keyUp.isDown()) {
            float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
            double s = speed.get() * 0.05;
            mc.player.setDeltaMovement(-Math.sin(yaw) * s, mc.player.getDeltaMovement().y, Math.cos(yaw) * s);
        }
    }

    private void glide() {
        if (!mc.player.onGround()) {
            double fallSpeed = 0.02;
            if (mc.options.keySprint.isDown()) fallSpeed = 0.01;
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x,
                Math.max(mc.player.getDeltaMovement().y, -fallSpeed),
                mc.player.getDeltaMovement().z);
            double horizontal = speed.get() * 0.1;
            float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
            if (mc.options.keyUp.isDown()) {
                mc.player.setDeltaMovement(-Math.sin(yaw) * horizontal, mc.player.getDeltaMovement().y, Math.cos(yaw) * horizontal);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
        }
        mc.player.getAbilities().setFlyingSpeed((float) DEFAULT_FLY_SPEED);
        mc.player.noPhysics = false;
    }
}
