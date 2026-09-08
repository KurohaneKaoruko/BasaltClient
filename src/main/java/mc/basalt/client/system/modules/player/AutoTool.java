package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    private final BooleanSetting onlyBreaking = new BooleanSetting("仅在挖掘时", true);

    public AutoTool() {
        super("AutoTool", "自动切工具", "挖掘方块时自动切换到最快的工具。", Category.Player);
        addSetting(onlyBreaking);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (onlyBreaking.get() && !mc.options.attackKey.isPressed()) {
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult hit)
                || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockState state = mc.world.getBlockState(hit.getBlockPos());
        if (state.isAir()) {
            return;
        }

        int bestSlot = -1;
        float bestSpeed = -1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) {
            return;
        }

        float currentSpeed = mc.player.getMainHandStack().getMiningSpeedMultiplier(state);
        if (bestSpeed > currentSpeed + 0.01f) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
