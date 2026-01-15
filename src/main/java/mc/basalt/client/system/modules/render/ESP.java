package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ESP extends Module {
    private final BooleanSetting box = new BooleanSetting("方框", true);
    private final BooleanSetting tracers = new BooleanSetting("连线", false);

    public ESP() {
        super("ESP", "透视", "在实体周围绘制高亮框。", Category.Render);
        addSetting(box);
        addSetting(tracers);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                 if (box.get()) {
                     // Render Box Logic
                 }
                 if (tracers.get()) {
                     // Render Tracers Logic
                 }
            }
        }
    }
}
