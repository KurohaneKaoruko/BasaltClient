package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "疾跑", "移动时自动疾跑。", Category.Movement);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.player.getDeltaMovement().lengthSqr() > 0 && !mc.player.isShiftKeyDown()
                && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }
}
