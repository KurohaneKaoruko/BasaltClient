package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.mixin.IMinecraftClientAccessor;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class AutoClicker extends Module {
    private final DoubleSetting minCps = new DoubleSetting("最小CPS", 7.0, 1.0, 20.0, 1.0);
    private final DoubleSetting maxCps = new DoubleSetting("最大CPS", 11.0, 1.0, 20.0, 1.0);
    private final BooleanSetting entitiesOnly = new BooleanSetting("仅攻击实体", true);

    private long nextClickTime;

    public AutoClicker() {
        super("AutoClicker", "自动连点", "按住攻击键时自动高速点击。", Category.Combat);
        addSetting(minCps);
        addSetting(maxCps);
        addSetting(entitiesOnly);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.currentScreen != null) {
            return;
        }

        if (!mc.options.attackKey.isPressed()) {
            nextClickTime = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextClickTime) {
            return;
        }

        if (entitiesOnly.get() && !(mc.crosshairTarget instanceof EntityHitResult)) {
            // 准星不在实体上时不点击，避免打断方块挖掘
            return;
        }

        ((IMinecraftClientAccessor) mc).invokeDoAttack();

        double lo = Math.min(minCps.get(), maxCps.get());
        double hi = Math.max(minCps.get(), maxCps.get());
        double cps = lo + ThreadLocalRandom.current().nextDouble(Math.max(0.1, hi - lo));
        nextClickTime = now + (long) (1000.0 / cps);
    }
}
