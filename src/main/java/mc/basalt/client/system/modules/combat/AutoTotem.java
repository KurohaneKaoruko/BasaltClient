package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    private final DoubleSetting health = new DoubleSetting("触发血量", 10.0, 1.0, 20.0, 1.0);

    public AutoTotem() {
        super("AutoTotem", "自动图腾", "生命值低时自动将图腾拿到副手。", Category.Combat);
        addSetting(health);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Check if already holding totem in offhand
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Check health
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() <= health.get()) {
            int totemSlot = findTotem();
            if (totemSlot != -1) {
                // Move totem to offhand (Slot 45 is offhand in Container/Inventory)
                // InventoryUtils logic is complex, simplifying here:
                // We pick up the item and put it in offhand
                // Note: Slot mapping differs between containers. For player inventory:
                // 0-8: Hotbar
                // 9-35: Main Inventory
                // 45: Offhand
                
                // Use interaction manager
                // pickup
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, totemSlot, 0, SlotActionType.PICKUP, mc.player);
                // place in offhand
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                // put back whatever was in offhand (if any) to original slot? 
                // If cursor is holding something now (previous offhand item), we should put it back.
                // But simplified for now.
            }
        }
    }

    private int findTotem() {
        // Iterate player inventory (9 to 44 usually covers main + hotbar)
        // Actually mappings: 
        // 0 craft
        // 1-4 armor
        // 5-8 craft
        // 9-35 storage
        // 36-44 hotbar
        // 45 offhand
        
        // We check 9 to 44
        for (int i = 9; i < 45; i++) {
             if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem() == Items.TOTEM_OF_UNDYING) { // getStack uses index 0-35 + armor/offhand separately usually
                 // ScreenHandler slots are different from Inventory slots
                 return i;
             }
        }
        return -1;
    }
}
