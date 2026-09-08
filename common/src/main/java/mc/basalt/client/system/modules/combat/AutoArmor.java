package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

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

        // 以当前已穿戴护甲为基准
        for (int i = 0; i < 4; i++) {
            ItemStack stack = mc.player.getInventory().getArmor(i);
            bestArmorValues[i] = getArmorValue(stack);
            bestArmorSlots[i] = -1;
        }

        // 扫描背包（InventoryMenu: 9-35 主背包, 36-44 快捷栏）
        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            int armorType = armorTypeOf(stack);
            if (armorType == -1) {
                continue;
            }

            int value = getArmorValue(stack);
            if (value > bestArmorValues[armorType]) {
                bestArmorValues[armorType] = value;
                bestArmorSlots[armorType] = i;
            }
        }

        // 装备最优（一次一件）
        for (int i = 0; i < 4; i++) {
            if (bestArmorSlots[i] != -1) {
                int armorSlotId = 8 - i; // 0->8(脚), 1->7(腿), 2->6(胸), 3->5(头)
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestArmorSlots[i], 0,
                        net.minecraft.world.inventory.ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    /** 通过 EQUIPPABLE 组件判断护甲类型：0=脚, 1=腿, 2=胸, 3=头；非护甲返回 -1。 */
    private int armorTypeOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return -1;
        }
        return switch (equippable.slot()) {
            case FEET -> 0;
            case LEGS -> 1;
            case CHEST -> 2;
            case HEAD -> 3;
            default -> -1;
        };
    }

    /** 通过属性修饰符评估护甲价值（防御 ×10 + 韧性）。 */
    private int getArmorValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0;
        }

        int defense = 0;
        int toughness = 0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Holder<Attribute> attribute = entry.attribute();
            if (attribute.value() == Attributes.ARMOR.value()) {
                defense = (int) entry.modifier().amount();
            } else if (attribute.value() == Attributes.ARMOR_TOUGHNESS.value()) {
                toughness = (int) entry.modifier().amount();
            }
        }
        return defense * 10 + toughness;
    }
}
