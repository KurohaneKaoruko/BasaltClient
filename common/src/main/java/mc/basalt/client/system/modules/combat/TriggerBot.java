package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    private final BooleanSetting autoDelay = new BooleanSetting("自动冷却", true);

    public TriggerBot() {
        super("TriggerBot", "触发机器人", "准星对准实体时自动攻击。", Category.Combat);
        addSetting(autoDelay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.gameMode == null || mc.screen != null) {
            return;
        }

        if (!(mc.hitResult instanceof EntityHitResult hit)) {
            return;
        }
        if (!hit.getEntity().isAlive()) {
            return;
        }
        if (autoDelay.get() && mc.player.getAttackStrengthScale(0.5f) < 1.0f) {
            return;
        }

        mc.gameMode.attack(mc.player, hit.getEntity());
        mc.player.swing(InteractionHand.MAIN_HAND);
    }
}
