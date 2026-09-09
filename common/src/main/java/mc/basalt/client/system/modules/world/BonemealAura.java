package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BonemealAura extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 4.5, 1.0, 6.0, 0.1);
    private final BooleanSetting crops = new BooleanSetting("催熟作物", true);
    private final BooleanSetting saplings = new BooleanSetting("催熟树苗", true);

    private int delay;

    public BonemealAura() {
        super("BonemealAura", "骨粉光环", "自动对范围内作物/树苗使用骨粉。", Category.World);
        addSetting(range);
        addSetting(crops);
        addSetting(saplings);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!mc.player.getMainHandItem().is(Items.BONE_MEAL)) return;

        if (delay > 0) {
            delay--;
            return;
        }

        BlockPos center = mc.player.blockPosition();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            double dist = mc.player.getEyePosition().distanceTo(
                    Vec3.atCenterOf(pos));
            if (dist > range.get()) continue;

            BlockState state = mc.level.getBlockState(pos);

            boolean valid = false;
            if (crops.get() && state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                valid = true;
            } else if (saplings.get() && state.getBlock() instanceof SaplingBlock) {
                valid = true;
            }
            if (!valid) continue;

            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                delay = 4;
                return;
            }
        }
    }
}
