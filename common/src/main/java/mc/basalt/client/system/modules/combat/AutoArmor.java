package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class AutoArmor extends Module {
    private final DoubleSetting delay = new DoubleSetting("延迟", 5.0, 0.0, 20.0, 1.0);
    private int timer = 0;

    public AutoArmor() {
        super("AutoArmor", "自动穿甲", "自动装备最好的护甲。", Category.Combat);
        addSetting(delay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.screen != null) return;
        if (timer < delay.get()) { timer++; return; }
        timer = 0;

        int[] bestSlots = new int[4];
        int[] bestValues = new int[4];
        for (int i = 0; i < 4; i++) {
            bestValues[i] = getArmorValue(mc.player.getInventory().getArmor(i));
            bestSlots[i] = -1;
        }

        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof ArmorItem armor) {
                int type = armor.getEquipmentSlot().getIndex();
                if (type >= 0 && type < 4) {
                    int value = getArmorValue(stack);
                    if (value > bestValues[type]) {
                        bestValues[type] = value;
                        bestSlots[type] = i;
                    }
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            if (bestSlots[i] != -1) {
                int armorSlotId = 8 - i;
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestSlots[i], 0,
                        net.minecraft.world.inventory.ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    private int getArmorValue(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;
        return armor.getDefense() * 10 + (int) armor.getToughness();
    }
}