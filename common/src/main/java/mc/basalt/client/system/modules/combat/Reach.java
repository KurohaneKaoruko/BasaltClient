package mc.basalt.client.system.modules.combat;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;

public class Reach extends Module {
    private final DoubleSetting distance = new DoubleSetting("攻击距离", 4.5, 3.0, 6.0, 0.1);
    private final BooleanSetting onlyCombat = new BooleanSetting("仅攻击实体", true);

    public Reach() {
        super("Reach", "攻击距离", "扩展攻击与交互距离。", Category.Combat);
        addSetting(distance);
        addSetting(onlyCombat);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.REACH_VALUE = distance.get();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.REACH_VALUE = -1.0;
    }
}
