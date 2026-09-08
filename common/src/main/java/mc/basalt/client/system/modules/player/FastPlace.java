package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.mixin.IMinecraftAccessor;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "快速放置", "Removes place delay", Category.Player);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        ((IMinecraftAccessor) mc).setMissTime(0);
    }
}
