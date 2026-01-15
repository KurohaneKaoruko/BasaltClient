package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class Jesus extends Module {
    public Jesus() {
        super("Jesus", "水上行走", "允许你在水面上行走。", Category.Movement);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null)
            return;

        if (mc.player.isTouchingWater() && !mc.player.isSneaking()) {
            // Apply upward velocity to keep player on surface
            // 0.11 is usually enough to bob up
            mc.player.setVelocity(mc.player.getVelocity().x, 0.11, mc.player.getVelocity().z);
        }
    }
}
