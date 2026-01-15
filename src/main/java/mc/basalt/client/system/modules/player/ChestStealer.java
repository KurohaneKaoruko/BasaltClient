package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealer extends Module {
    private final DoubleSetting delay = new DoubleSetting("延迟", 100.0, 0.0, 1000.0, 10.0);
    private long lastClickTime = 0;

    public ChestStealer() {
        super("ChestStealer", "自动拿取", "自动拿取箱子中的物品。", Category.Player);
        addSetting(delay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.interactionManager == null)
            return;

        if (mc.currentScreen instanceof GenericContainerScreen) {
            GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
            GenericContainerScreenHandler handler = screen.getScreenHandler();

            if (System.currentTimeMillis() - lastClickTime < delay.get())
                return;

            int rows = handler.getRows();
            int chestSlots = rows * 9;
            boolean empty = true;

            for (int i = 0; i < chestSlots; i++) {
                Slot slot = handler.getSlot(i);
                if (slot.hasStack()) {
                    empty = false;
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    lastClickTime = System.currentTimeMillis();
                    return; // One item per delay
                }
            }

            if (empty) {
                mc.player.closeHandledScreen();
            }
        }
    }
}
