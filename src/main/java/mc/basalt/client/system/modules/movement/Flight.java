package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class Flight extends Module {
    public Flight() {
        super("Flight", "飞行", "允许你在生存模式下飞行。", Category.Movement);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player != null) {
            mc.player.getAbilities().allowFlying = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }
}
