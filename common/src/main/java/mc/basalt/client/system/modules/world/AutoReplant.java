package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class AutoReplant extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 3.5, 1.0, 6.0, 0.1);
    private final DoubleSetting delay = new DoubleSetting("放置延迟tick", 4.0, 0.0, 40.0, 1.0);

    /** 作物 -> 对应种子/作物 */
    private static final Map<Item, BlockPos> CROP_TO_SEED = new HashMap<>();
    static {
        CROP_TO_SEED.put(Items.WHEAT_SEEDS, new BlockPos(0, 0, 0));
    }

    private int timer;

    public AutoReplant() {
        super("AutoReplant", "自动补种", "自动补种附近的成熟作物。", Category.World);
        addSetting(range);
        addSetting(delay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        BlockPos center = mc.player.blockPosition();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            BlockState state = mc.level.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock crop)) continue;
            if (!crop.isMaxAge(state)) continue;

            // 收割（挖掘成熟作物）
            if (mc.gameMode.continueDestroyBlock(pos, Direction.UP)) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }

            // 寻找对应种子放置
            Item seed = seedFor(state.getBlock());
            if (seed == null) continue;
            int slot = findSeedSlot(seed);
            if (slot == -1) continue;

            mc.player.getInventory().selected = slot;

            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                timer = delay.get().intValue();
                return;
            }
        }
    }

    private Item seedFor(net.minecraft.world.level.block.Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (block == Blocks.MELON_STEM || block == Blocks.PUMPKIN_STEM) return Items.MELON_SEEDS;
        return null;
    }

    private int findSeedSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }
}