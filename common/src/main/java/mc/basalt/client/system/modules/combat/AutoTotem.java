package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;

public class AutoTotem extends Module {
    /** InventoryMenu 中的副手槽位编号。 */
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
        if (mc.player == null || mc.gameMode == null || mc.screen != null) {
            return;
        }

        if (mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        if (!always.get() && mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get()) {
            return;
        }

        int slot = findTotemSlot();
        if (slot == -1) {
            return;
        }

        int syncId = mc.player.inventoryMenu.containerId;
        // 拿起图腾 → 放入副手 → 将原副手物品放回图腾原来的格子
        mc.gameMode.handleInventoryMouseClick(syncId, slot, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(syncId, OFFHAND_SLOT, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(syncId, slot, 0, ClickType.PICKUP, mc.player);
    }

    private int findTotemSlot() {
        // InventoryMenu: 9-35 主背包, 36-44 快捷栏（优先消耗主背包，保留快捷栏）
        for (int i = 9; i < 45; i++) {
            if (mc.player.inventoryMenu.getSlot(i).getItem().getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
}
