package mc.basalt.client.system.modules.render;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.Render3DEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class Tracers extends Module {
    public Tracers() {
        super("Tracers", "连线", "Draws lines to entities", Category.Render);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        // Similar to ESP
    }
}
