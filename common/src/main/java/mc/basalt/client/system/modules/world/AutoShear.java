package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.Items;

public class AutoShear extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 4.0, 1.0, 6.0, 0.1);

    private int delay;

    public AutoShear() {
        super("AutoShear", "自动剪毛", "手持剪刀时自动剪附近有毛的羊。", Category.World);
        addSetting(range);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (!mc.player.getMainHandItem().is(Items.SHEARS)) return;

        if (delay > 0) {
            delay--;
            return;
        }

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Sheep sheep)) continue;
            if (sheep.isDeadOrDying() || sheep.hasCustomName() && sheep.isLeashed()) continue;
            if (!sheep.readyForShearing()) continue;
            if (mc.player.distanceToSqr(sheep) > range.get() * range.get()) continue;

            if (mc.gameMode.interact(mc.player, sheep, InteractionHand.MAIN_HAND).consumesAction()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                delay = 6;
                return;
            }
        }
    }
}
