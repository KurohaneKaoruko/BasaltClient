package mc.basalt.client.system.modules.combat;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;

public class HitBoxes extends Module {
    private final DoubleSetting size = new DoubleSetting("扩大量", 0.3, 0.1, 1.5, 0.05);

    public HitBoxes() {
        super("HitBoxes", "碰撞箱扩大", "扩大实体判定箱，更容易命中。", Category.Combat);
        addSetting(size);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.HITBOXES_ENABLED = true;
        BasaltClient.HITBOXES_VALUE = size.get().floatValue();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.HITBOXES_ENABLED = false;
    }
}
