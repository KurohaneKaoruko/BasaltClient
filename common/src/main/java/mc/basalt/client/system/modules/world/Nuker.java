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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Nuker extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "球形", "球形", "平面", "同类");
    private final DoubleSetting range = new DoubleSetting("范围", 4.5, 1.0, 6.0, 0.1);
    private final BooleanSetting flatten = new BooleanSetting("仅平地以下", false);
    private final DoubleSetting delay = new DoubleSetting("破坏间隔tick", 2.0, 0.0, 20.0, 1.0);

    private int timer;
    private net.minecraft.world.level.block.Block targetBlock;

    public Nuker() {
        super("Nuker", "范围破坏", "自动破坏范围内的方块。", Category.World);
        addSetting(mode);
        addSetting(range);
        addSetting(flatten);
        addSetting(delay);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer = 0;
        targetBlock = null;
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
        BlockPos min = center.offset(-r, -r, -r);
        BlockPos max = center.offset(r, r, r);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            // 距离检查
            double dist = mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(pos));
            if (dist > range.get()) continue;

            BlockState state = mc.level.getBlockState(pos);
            if (state.isAir()) continue;

            // 模式过滤
            if (mode.get().equals("平面") && pos.getY() != center.getY()) continue;
            if (mode.get().equals("同类")) {
                if (targetBlock == null) {
                    targetBlock = state.getBlock(); // 锁定第一个碰到的方块类型
                } else if (state.getBlock() != targetBlock) {
                    continue;
                }
            }

            // 平地模式：只挖脚下平面以下
            if (flatten.get() && pos.getY() > center.getY()) continue;

            // 开始破坏
            if (mc.gameMode.continueDestroyBlock(pos, Direction.UP)) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                timer = delay.get().intValue();
            }
            return; // 每 tick 只处理一个方块
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targetBlock = null;
    }
}
