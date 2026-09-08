package mc.basalt.client.system.modules.render;

import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;

public class NoWeather extends Module {
    public NoWeather() {
        super("NoWeather", "无天气", "客户端强制清除雨雪天气效果。", Category.Render);
    }

    /** 供 MixinLevel 查询（渲染线程调用，模块此时必已初始化）。 */
    public static boolean isActive() {
        if (!Modules.initialized()) {
            return false;
        }
        Module module = Modules.get().getByName("NoWeather");
        return module != null && module.isEnabled();
    }
}
