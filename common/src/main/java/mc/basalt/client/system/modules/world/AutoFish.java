package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;

public class AutoFish extends Module {
    private final DoubleSetting reelSensitivity = new DoubleSetting("咬钩灵敏度", 0.04, 0.01, 0.15, 0.01);
    private final DoubleSetting recastDelay = new DoubleSetting("重抛延迟tick", 20.0, 5.0, 100.0, 5.0);
    private final BooleanSetting autoSwitch = new BooleanSetting("自动拿鱼竿", true);
    private final BooleanSetting stopWhenFull = new BooleanSetting("鱼满暂停", false);

    private int waitTimer;
    private boolean reeling;

    public AutoFish() {
        super("AutoFish", "自动钓鱼", "自动收竿和抛竿。", Category.World);
        addSetting(reelSensitivity);
        addSetting(recastDelay);
        addSetting(autoSwitch);
        addSetting(stopWhenFull);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        waitTimer = 0;
        reeling = false;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.gameMode == null) return;

        if (stopWhenFull.get() && isInventoryFull()) return;

        if (!mc.player.getMainHandItem().is(Items.FISHING_ROD)) {
            if (!autoSwitch.get()) return;
            int slot = findRodSlot();
            if (slot == -1) return;
            mc.player.getInventory().selected = slot;
            waitTimer = 5;
            return;
        }

        FishingHook hook = mc.player.fishing;

        if (waitTimer > 0) {
            waitTimer--;
            return;
        }

        if (hook == null) {
            // 抛竿
            if (!reeling) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
                waitTimer = 10;
            } else {
                reeling = false;
                waitTimer = recastDelay.get().intValue();
            }
            return;
        }

        // 检测咬钩：浮漂被拉下水（y 速度骤降）
        if (hook.isInWater() && hook.tickCount > 20) {
            if (hook.getDeltaMovement().y < -reelSensitivity.get()) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);
                reeling = true;
                waitTimer = 10;
            }
        }
    }

    private boolean isInventoryFull() {
        if (mc.player == null) return false;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) return false;
        }
        return true;
    }

    private int findRodSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(Items.FISHING_ROD)) return i;
        }
        return -1;
    }
}
