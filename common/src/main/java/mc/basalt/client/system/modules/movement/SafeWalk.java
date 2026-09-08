package mc.basalt.client.system.modules.movement;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("SafeWalk", "安全行走", "防止走到方块边缘掉落。", Category.Movement);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.SAFEWALK_ENABLED = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.SAFEWALK_ENABLED = false;
    }
}
