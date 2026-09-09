package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoMine extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "智能", "智能", "持续");
    private final DoubleSetting range = new DoubleSetting("范围", 4.5, 1.0, 6.0, 0.1);
    private final BooleanSetting onlyOres = new BooleanSetting("仅矿物", true);
    private final BooleanSetting rotate = new BooleanSetting("旋转视角", true);
    private final BooleanSetting switchTool = new BooleanSetting("自动切工具", true);

    public AutoMine() {
        super("AutoMine", "自动挖掘", "自动挖掘准星指向的方块。", Category.Player);
        addSetting(mode); addSetting(range); addSetting(onlyOres); addSetting(rotate); addSetting(switchTool);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        var hitResult = mc.hitResult;
        if (!(hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit)) return;
        if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

        var pos = blockHit.getBlockPos();
        var state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        // 仅矿物模式
        if (onlyOres.get() && !isOre(state)) return;

        // 距离检查
        if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > range.get() * range.get()) return;

        // 自动切工具
        if (switchTool.get()) {
            int bestSlot = findBestTool(state);
            if (bestSlot != -1 && mc.player.getInventory().selected != bestSlot) {
                mc.player.getInventory().selected = bestSlot;
            }
        }

        // 持续挖掘
        mc.options.keyAttack.setDown(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options != null) {
            mc.options.keyAttack.setDown(false);
        }
    }

    private boolean isOre(net.minecraft.world.level.block.state.BlockState state) {
        var block = state.getBlock();
        var name = block.getDescriptionId();
        return name.contains("ore") || name.contains("mineral");
    }

    private int findBestTool(net.minecraft.world.level.block.state.BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
