package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "无摔落", "Prevents fall damage", Category.Movement);
    }

    @EventHandler
    public void onSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet = (PlayerMoveC2SPacket) event.getPacket();
            // We can't modify the packet directly easily without accessors or reflection if fields are private
            // But usually utility mods use Mixins to add setters to packets.
            // For now, let's assume we need to replace it or use a mixin accessor.
            // Or simpler: just send a ground packet?
            // Actually, in Fabric, we might need accessors. 
            // For this implementation, I will skip the logic requiring accessors and just print log or do nothing 
            // until we have accessors.
            // Wait, I can use reflection in the module if needed, or just assume I'll add accessors later.
            // Let's use `packet.onGround = true` via reflection for now.
            try {
                java.lang.reflect.Field field = PlayerMoveC2SPacket.class.getDeclaredField("onGround");
                field.setAccessible(true);
                field.setBoolean(packet, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
