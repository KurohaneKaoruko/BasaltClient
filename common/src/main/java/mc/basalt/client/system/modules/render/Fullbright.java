package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.mixin.MixinOptionInstance;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class Fullbright extends Module {
    /** 超高亮度值（绕过原版验证，实际可生效任意值）。 */
    private final DoubleSetting brightness = new DoubleSetting("亮度", 10000.0, 1000.0, 100000.0, 1000.0);

    private Double originalGamma;

    public Fullbright() {
        super("Fullbright", "夜视", "无限夜视效果。", Category.Render);
        addSetting(brightness);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null) {
            originalGamma = mc.options.gamma().get();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.options != null) {
            // 直接写入内部字段绕过验证
            ((MixinOptionInstance) (Object) mc.options.gamma()).basalt$setValueRaw(brightness.get());
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        restoreGamma();
    }

    public void restoreGamma() {
        if (mc.options != null && originalGamma != null) {
            mc.options.gamma().set(originalGamma);
        }
        originalGamma = null;
    }
}
