package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
    private final DoubleSetting horizontal = new DoubleSetting("水平", 0.0, 0.0, 100.0, 1.0);
    private final DoubleSetting vertical = new DoubleSetting("垂直", 0.0, 0.0, 100.0, 1.0);

    public Velocity() {
        super("Velocity", "反击退", "减少或消除受到的击退效果。", Category.Combat);
        addSetting(horizontal);
        addSetting(vertical);
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (mc.player == null)
            return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.getPacket();
            if (packet.getEntityId() == mc.player.getId()) {
                if (horizontal.get() == 0.0 && vertical.get() == 0.0) {
                    event.cancel();
                }
            }
        } else if (event.getPacket() instanceof ExplosionS2CPacket) {
            if (horizontal.get() == 0.0 && vertical.get() == 0.0) {
                event.cancel();
            }
        }
    }
}
