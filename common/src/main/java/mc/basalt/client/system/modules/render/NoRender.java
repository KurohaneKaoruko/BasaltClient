package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.client.renderer.GameRenderer;

public class NoRender extends Module {
    private final BooleanSetting noHurtCam = new BooleanSetting("受伤抖动", true);
    private final BooleanSetting noPortal = new BooleanSetting("传送门特效", true);
    private final BooleanSetting noFire = new BooleanSetting("火焰覆盖", true);
    private final BooleanSetting noWater = new BooleanSetting("水下覆盖", true);
    private final BooleanSetting noPumpkin = new BooleanSetting("南瓜头覆盖", true);

    public NoRender() {
        super("NoRender", "无渲染", "移除各种屏幕覆盖效果。", Category.Render);
        addSetting(noHurtCam); addSetting(noPortal); addSetting(noFire); addSetting(noWater); addSetting(noPumpkin);
    }

    public boolean shouldCancelHurtCam() {
        return isEnabled() && noHurtCam.get();
    }

    public boolean shouldCancelPortal() {
        return isEnabled() && noPortal.get();
    }

    public boolean shouldCancelFire() {
        return isEnabled() && noFire.get();
    }

    public boolean shouldCancelWater() {
        return isEnabled() && noWater.get();
    }

    public boolean shouldCancelPumpkin() {
        return isEnabled() && noPumpkin.get();
    }
}
