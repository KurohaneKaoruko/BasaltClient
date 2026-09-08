package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;

public class AutoArmor extends Module {
    private final DoubleSetting delay = new DoubleSetting("延迟", 5.0, 0.0, 20.0, 1.0);
    private int timer = 0;

    public AutoArmor() {
        super("AutoArmor", "自动穿甲", "自动装备最好的护甲。", Category.Combat);
        addSetting(delay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.screen != null)
            return;

        if (timer < delay.get()) {
            timer++;
            return;
        }
        timer = 0;

        int[] bestArmorSlots = new int[4];
        int[] bestArmorValues = new int[4];

        // Init with current armor
        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getArmor(i);
            bestArmorValues[i] = getArmorValue(stack);
            bestArmorSlots[i] = -1;
        }

        // Scan inventory
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) stack.getItem();
                int slotType = armor.getEquipmentSlot().getIndex(); // 0-3 for armor slots in some mappings, need
                                                                      // check
                // getEntitySlotId returns EquipmentSlot id.
                // FEET=0, LEGS=1, CHEST=2, HEAD=3 in EquipmentSlot order for armor usually?
                // Actually EquipmentSlot: MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD
                // FEET=0, LEGS=1, CHEST=2, HEAD=3 relative to armor start

                int armorType = -1;
                switch (armor.getEquipmentSlot()) {
                    case FEET:
                        armorType = 0;
                        break;
                    case LEGS:
                        armorType = 1;
                        break;
                    case CHEST:
                        armorType = 2;
                        break;
                    case HEAD:
                        armorType = 3;
                        break;
                    default:
                        break;
                }

                if (armorType != -1) {
                    int value = getArmorValue(stack);
                    if (value > bestArmorValues[armorType]) {
                        bestArmorValues[armorType] = value;
                        bestArmorSlots[armorType] = i;
                    }
                }
            }
        }

        // Equip best
        for (int i = 0; i < 4; i++) {
            if (bestArmorSlots[i] != -1) {
                // If current slot has armor, swap it out first or simple swap?
                // Simple click with shift usually works or pickup and click.
                // Using QUICK_MOVE on the item in inventory usually equips it if slot is empty
                // or swaps?
                // Safer: pickup new, click armor slot.

                // Let's use simple QUICK_MOVE for now, assuming empty slot or standard behavior
                // But if slot is full, quick move might not swap.
                // Let's just pick up the item and click the armor slot.
                // Armor slots in container 0 (player inventory) are 5, 6, 7, 8.
                // 5=Head, 6=Chest, 7=Legs, 8=Feet.
                // Wait, InventoryMenu slots:
                // 0=CraftResult, 1-4=CraftInput, 5=Head, 6=Chest, 7=Legs, 8=Feet.

                int armorSlotId = 8 - i; // 0->8(Feet), 1->7(Legs), 2->6(Chest), 3->5(Head)

                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestArmorSlots[i], 0,
                        ClickType.QUICK_MOVE, mc.player);
                return; // One per tick
            }
        }
    }

    private int getArmorValue(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem))
            return 0;
        ArmorItem armor = (ArmorItem) stack.getItem();
        int reduction = armor.getDefense();
        int toughness = (int) armor.getToughness();
        return reduction * 10 + toughness; // Simple weight
    }
}
