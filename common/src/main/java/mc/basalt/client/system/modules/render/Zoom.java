package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class Zoom extends Module {
    private final DoubleSetting fov = new DoubleSetting("缩放FOV", 30.0, 5.0, 100.0, 5.0);

    private Integer originalFov;

    public Zoom() {
        super("Zoom", "缩放", "开启后临时降低视野角度实现望远镜效果。", Category.Render);
        addSetting(fov);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null && mc.options != null) {
            originalFov = mc.options.fov().get();
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.options == null) {
            return;
        }
        // 持续应用（防止其他逻辑恢复 FOV）
        mc.options.fov().set(fov.get().intValue());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options != null && originalFov != null) {
            mc.options.fov().set(originalFov);
        }
        originalFov = null;
    }
}
