package mc.basalt.client.system.modules.movement;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.ModeSetting;

public class NoSlow extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "严格", "严格", "宽松");

    public NoSlow() {
        super("NoSlow", "无减速", "进食/拉弓/格挡时移动不减速。", Category.Movement);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.NOSLOW_ENABLED = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.NOSLOW_ENABLED = false;
    }
}
