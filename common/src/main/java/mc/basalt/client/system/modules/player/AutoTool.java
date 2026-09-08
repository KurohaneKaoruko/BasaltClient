package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoTool extends Module {
    private final BooleanSetting onlyBreaking = new BooleanSetting("仅在挖掘时", true);

    public AutoTool() {
        super("AutoTool", "自动切工具", "挖掘方块时自动切换到最快的工具。", Category.Player);
        addSetting(onlyBreaking);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (onlyBreaking.get() && !mc.options.keyAttack.isDown()) {
            return;
        }

        if (!(mc.hitResult instanceof BlockHitResult hit)
                || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockState state = mc.level.getBlockState(hit.getBlockPos());
        if (state.isAir()) {
            return;
        }

        int bestSlot = -1;
        float bestSpeed = -1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot == -1) {
            return;
        }

        float currentSpeed = mc.player.getMainHandItem().getDestroySpeed(state);
        if (bestSpeed > currentSpeed + 0.01f) {
            mc.player.getInventory().selected = bestSlot;
        }
    }
}
