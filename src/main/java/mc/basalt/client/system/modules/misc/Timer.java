package mc.basalt.client.system.modules.misc;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class Timer extends Module {
    private final DoubleSetting speed = new DoubleSetting("速度", 1.1, 0.1, 10.0, 0.1);

    public Timer() {
        super("Timer", "变速齿轮", "改变游戏运行速度。", Category.Misc);
        addSetting(speed);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        BasaltClient.TIMER = speed.get().floatValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.TIMER = 1.0f;
    }
}
