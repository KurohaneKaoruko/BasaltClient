package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;

public class ChestStealer extends Module {
    private final DoubleSetting delay = new DoubleSetting("延迟", 100.0, 0.0, 1000.0, 10.0);
    private long lastClickTime = 0;

    public ChestStealer() {
        super("ChestStealer", "自动拿取", "自动拿取箱子中的物品。", Category.Player);
        addSetting(delay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.gameMode == null)
            return;

        if (mc.screen instanceof ContainerScreen) {
            ContainerScreen screen = (ContainerScreen) mc.screen;
            ChestMenu handler = screen.getMenu();

            if (System.currentTimeMillis() - lastClickTime < delay.get())
                return;

            int rows = handler.getRowCount();
            int chestSlots = rows * 9;
            boolean empty = true;

            for (int i = 0; i < chestSlots; i++) {
                Slot slot = handler.getSlot(i);
                if (slot.hasItem()) {
                    empty = false;
                    mc.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                    lastClickTime = System.currentTimeMillis();
                    return; // One item per delay
                }
            }

            if (empty) {
                mc.player.closeContainer();
            }
        }
    }
}
