package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.HashSet;
import java.util.Set;

public class XRay extends Module {
    private static final Set<Block> ORES = new HashSet<>();

    static {
        ORES.add(Blocks.COAL_ORE);
        ORES.add(Blocks.DEEPSLATE_COAL_ORE);
        ORES.add(Blocks.IRON_ORE);
        ORES.add(Blocks.DEEPSLATE_IRON_ORE);
        ORES.add(Blocks.GOLD_ORE);
        ORES.add(Blocks.DEEPSLATE_GOLD_ORE);
        ORES.add(Blocks.REDSTONE_ORE);
        ORES.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        ORES.add(Blocks.LAPIS_ORE);
        ORES.add(Blocks.DEEPSLATE_LAPIS_ORE);
        ORES.add(Blocks.DIAMOND_ORE);
        ORES.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        ORES.add(Blocks.EMERALD_ORE);
        ORES.add(Blocks.DEEPSLATE_EMERALD_ORE);
        ORES.add(Blocks.NETHER_QUARTZ_ORE);
        ORES.add(Blocks.NETHER_GOLD_ORE);
        ORES.add(Blocks.ANCIENT_DEBRIS);
        ORES.add(Blocks.COPPER_ORE);
        ORES.add(Blocks.DEEPSLATE_COPPER_ORE);
        ORES.add(Blocks.AMETHYST_CLUSTER);
    }

    public XRay() {
        super("XRay", "矿物透视", "隐藏普通方块，只显示矿物。", Category.Render);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.XRAY_ENABLED = true;
        // 透视时附加夜视
        BasaltClient.FULLBRIGHT_ENABLED = true;
        if (mc.level != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.XRAY_ENABLED = false;
        // 仅当 Fullbright 模块未开启时才关闭夜视
        Module fullbright = Modules.get().getByName("Fullbright");
        BasaltClient.FULLBRIGHT_ENABLED = fullbright != null && fullbright.isEnabled();
        if (mc.level != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    public static boolean isOre(Block block) {
        return ORES.contains(block);
    }
}
