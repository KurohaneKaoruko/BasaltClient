package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoDoor extends Module {
    private final DoubleSetting distance = new DoubleSetting("触发距离", 2.5, 1.0, 5.0, 0.1);
    private final BooleanSetting doors = new BooleanSetting("普通门", true);
    private final BooleanSetting trapdoors = new BooleanSetting("活板门", true);
    private final BooleanSetting closeBehind = new BooleanSetting("身后关门", false);

    private int delay;

    public AutoDoor() {
        super("AutoDoor", "自动开门", "走向门时自动打开（可选身后关门）。", Category.World);
        addSetting(distance);
        addSetting(doors);
        addSetting(trapdoors);
        addSetting(closeBehind);
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

        // 仅在行走时触发
        if (mc.player.input.forwardImpulse <= 0 && !mc.options.keyUp.isDown()) return;

        // 视线射线检测门
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(distance.get()));
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        boolean isDoor = doors.get() && state.getBlock() instanceof DoorBlock;
        boolean isTrap = trapdoors.get() && state.getBlock() instanceof TrapDoorBlock;
        if (!isDoor && !isTrap) return;

        // 已经打开的不处理
        if (state.getValue(DoorBlock.OPEN) || state.getValue(TrapDoorBlock.OPEN)) return;

        if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            delay = 10;
        }
    }
}
