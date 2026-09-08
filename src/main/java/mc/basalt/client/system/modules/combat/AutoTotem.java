package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    /** PlayerScreenHandler 中的副手槽位编号。 */
    private static final int OFFHAND_SLOT = 45;

    private final DoubleSetting health = new DoubleSetting("触发血量", 10.0, 1.0, 20.0, 1.0);
    private final BooleanSetting always = new BooleanSetting("始终持有图腾", false);

    public AutoTotem() {
        super("AutoTotem", "自动图腾", "生命值低时自动将图腾换到副手。", Category.Combat);
        addSetting(health);
        addSetting(always);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) {
            return;
        }

        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        if (!always.get() && mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get()) {
            return;
        }

        int slot = findTotemSlot();
        if (slot == -1) {
            return;
        }

        int syncId = mc.player.playerScreenHandler.syncId;
        // 拿起图腾 → 放入副手 → 将原副手物品放回图腾原来的格子
        mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
    }

    private int findTotemSlot() {
        // PlayerScreenHandler: 9-35 主背包, 36-44 快捷栏（优先消耗主背包，保留快捷栏）
        for (int i = 9; i < 45; i++) {
            if (mc.player.playerScreenHandler.getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
