package mc.basalt.client.system.modules.render;

import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;

public class Fullbright extends Module {
    private Double oldGamma;

    public Fullbright() {
        super("Fullbright", "夜视", "让你在黑暗中也能看清。", Category.Render);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            oldGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(100.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && oldGamma != null) {
            mc.options.getGamma().setValue(oldGamma);
        }
    }
}
