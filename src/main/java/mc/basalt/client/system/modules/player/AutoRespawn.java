package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "自动重生", "死亡后自动点击重生按钮。", Category.Player);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player != null && mc.player.isDead() && mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
        }
    }
}
