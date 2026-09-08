package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold", "自动铺路", "自动在脚下放置方块。", Category.Movement);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null)
            return;

        BlockPos pos = mc.player.blockPosition().below();
        if (mc.options.keyShift.isDown()) {
            pos = pos.below();
        }

        BlockState state = mc.level.getBlockState(pos);

        if (state.canBeReplaced()) {
            int slot = getBlockSlot();
            if (slot == -1)
                return;

            int prevSlot = mc.player.getInventory().selected;
            mc.player.getInventory().selected = slot;

            // Simple placement logic
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(
                    new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5),
                    Direction.UP, pos, false));

            mc.player.swing(InteractionHand.MAIN_HAND);

            // Switch back (silent switch would be better but requires packets)
            mc.player.getInventory().selected = prevSlot;
        }
    }

    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
