package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Nametags extends Module {
    private final DoubleSetting scale = new DoubleSetting("缩放", 1.0, 0.5, 3.0, 0.1);

    public Nametags() {
        super("Nametags", "名牌显示", "放大并透过墙壁显示玩家名字。", Category.Render);
        addSetting(scale);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                 // Render Nametag Logic
                 // This usually involves calculating screen coordinates or using WorldRenderer to draw text at entity position
                 // but disabling depth test.
                 // For now, this is a placeholder for the logic structure.
            }
        }
    }
}
