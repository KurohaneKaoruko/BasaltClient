package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class AutoNametag extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 3.5, 1.0, 6.0, 0.1);

    private int delay;

    public AutoNametag() {
        super("AutoNametag", "自动命名", "手持命名牌时自动命名附近可命名实体。", Category.World);
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
        if (!mc.player.getMainHandItem().is(net.minecraft.world.item.Items.NAME_TAG)) return;
        if (delay > 0) {
            delay--;
            return;
        }

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) continue;
            if (living.hasCustomName()) continue;
            if (!living.isAlive() || living.isDeadOrDying()) continue;
            if (mc.player.distanceToSqr(entity) > range.get() * range.get()) continue;

            if (mc.gameMode.interact(mc.player, entity, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction()) {
                mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                delay = 4;
                return;
            }
        }
    }
}