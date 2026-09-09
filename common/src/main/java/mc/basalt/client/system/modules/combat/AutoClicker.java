package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.mixin.IMinecraftAccessor;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.world.phys.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class AutoClicker extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "攻击", "攻击", "使用", "双击");

    private final BooleanSetting jitter = new BooleanSetting("随机抖动", true);
    private final DoubleSetting minCps = new DoubleSetting("最小CPS", 7.0, 1.0, 20.0, 1.0);
    private final DoubleSetting maxCps = new DoubleSetting("最大CPS", 11.0, 1.0, 20.0, 1.0);
    private final BooleanSetting entitiesOnly = new BooleanSetting("仅攻击实体", true);
    private final BooleanSetting breakBlocks = new BooleanSetting("挖掘方块", false);
    private final BooleanSetting pauseOnUse = new BooleanSetting("使用物品时暂停", true);

    private long nextClickTime;

    public AutoClicker() {
        super("AutoClicker", "自动连点", "按住对应按键时自动连点，支持攻击/使用/双击。", Category.Combat);
        addSetting(mode); addSetting(jitter); addSetting(minCps); addSetting(maxCps);
        addSetting(entitiesOnly); addSetting(breakBlocks); addSetting(pauseOnUse);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.screen != null) return;
        if (pauseOnUse.get() && mc.player.isUsingItem()) return;

        boolean shouldAttack = mc.options.keyAttack.isDown();
        boolean shouldUse = mc.options.keyUse.isDown();

        boolean active = switch (mode.get()) {
            case "攻击" -> shouldAttack;
            case "使用" -> shouldUse;
            case "双击" -> shouldAttack || shouldUse;
            default -> false;
        };

        if (!active) {
            nextClickTime = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextClickTime) return;

        // 模式检查
        if (mode.get().equals("攻击") || (mode.get().equals("双击") && shouldAttack)) {
            if (entitiesOnly.get() && !(mc.hitResult instanceof EntityHitResult)) return;
            if (!breakBlocks.get() && mc.gameMode != null && mc.gameMode.isDestroying()) return;
            ((IMinecraftAccessor) mc).invokeStartAttack();
        } else if (mode.get().equals("使用") || (mode.get().equals("双击") && shouldUse)) {
            // 模拟右键
            mc.options.keyUse.setDown(false);
            mc.options.keyUse.setDown(true);
        }

        // 计算下次点击时间（含随机抖动）
        double lo = Math.min(minCps.get(), maxCps.get());
        double hi = Math.max(minCps.get(), maxCps.get());
        double cps = jitter.get()
            ? lo + ThreadLocalRandom.current().nextDouble(Math.max(0.1, hi - lo))
            : lo;
        nextClickTime = now + (long) (1000.0 / cps);
    }
}
