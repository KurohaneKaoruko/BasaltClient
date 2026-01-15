package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla", "Strafe");

    public Speed() {
        super("Speed", "速度", "让你移动得更快。", Category.Movement);
        addSetting(mode);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
            if (mode.get().equals("Vanilla")) {
                if (mc.player.isOnGround() && !mc.player.input.jumping) {
                    mc.player.jump();
                }
            } else if (mode.get().equals("Strafe")) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
                // Very basic strafe implementation placeholder
                // In a real client, this would involve complex yaw calculations
            }
        }
    }
}
