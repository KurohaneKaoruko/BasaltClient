package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class Flight extends Module {
    /** 原版默认飞行速度。 */
    private static final float DEFAULT_FLY_SPEED = 0.05f;

    private final DoubleSetting speed = new DoubleSetting("飞行速度", 1.0, 0.5, 5.0, 0.5);

    public Flight() {
        super("Flight", "飞行", "允许你在生存模式下飞行，可调节速度。", Category.Movement);
        addSetting(speed);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            return;
        }
        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().setFlyingSpeed(Math.max(0.01f, speed.get().floatValue() * DEFAULT_FLY_SPEED));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
        }
        mc.player.getAbilities().setFlyingSpeed(DEFAULT_FLY_SPEED);
    }
}
