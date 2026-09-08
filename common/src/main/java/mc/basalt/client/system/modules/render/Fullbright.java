package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", "夜视", "无限夜视效果。", Category.Render);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BasaltClient.FULLBRIGHT_ENABLED = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.FULLBRIGHT_ENABLED = false;
    }
}
