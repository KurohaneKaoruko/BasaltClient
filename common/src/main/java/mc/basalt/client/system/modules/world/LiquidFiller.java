package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class LiquidFiller extends Module {
    private final ModeSetting liquid = new ModeSetting("目标液体", "全部", "全部", "水", "岩浆");
    private final DoubleSetting range = new DoubleSetting("范围", 4.0, 1.0, 6.0, 0.1);
    private final BooleanSetting onlySource = new BooleanSetting("仅源头", true);
    private final BooleanSetting keepHotbar = new BooleanSetting("仅当前手持", false);

    private int delay;

    public LiquidFiller() {
        super("LiquidFiller", "液体填充", "用方块自动填平附近的液体。", Category.World);
        addSetting(liquid);
        addSetting(range);
        addSetting(onlySource);
        addSetting(keepHotbar);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        // 找方块物品
        int slot = findBlockSlot();
        if (slot == -1) return;
        if (!keepHotbar.get()) {
            mc.player.getInventory().selected = slot;
        }

        BlockPos center = mc.player.blockPosition();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            var fluid = mc.level.getFluidState(pos);
            if (fluid.isEmpty()) continue;
            if (onlySource.get() && !fluid.isSource()) continue;

            boolean isWater = fluid.is(net.minecraft.tags.FluidTags.WATER);
            boolean isLava = fluid.is(net.minecraft.tags.FluidTags.LAVA);
            if (liquid.get().equals("水") && !isWater) continue;
            if (liquid.get().equals("岩浆") && !isLava) continue;

            // 找一个相邻实心面放置
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (!mc.level.getBlockState(neighbor).isFaceSturdy(mc.level, neighbor, dir.getOpposite())) continue;

                BlockHitResult hit = new BlockHitResult(
                        Vec3.atCenterOf(pos), dir.getOpposite(), neighbor, false);
                if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    delay = 2;
                    return;
                }
            }
        }
    }

    private int findBlockSlot() {
        if (keepHotbar.get()) {
            ItemStack stack = mc.player.getMainHandItem();
            return stack.getItem() instanceof BlockItem ? mc.player.getInventory().selected : -1;
        }
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
