package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class Velocity extends Module {
    private final BooleanSetting knockback = new BooleanSetting("取消击退", true);
    private final BooleanSetting explosion = new BooleanSetting("取消爆炸", true);

    public Velocity() {
        super("Velocity", "反击退", "减少或消除受到的击退效果。", Category.Combat);
        addSetting(knockback);
        addSetting(explosion);
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (mc.player == null) {
            return;
        }

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (knockback.get() && packet.getEntityId() == mc.player.getId()) {
                event.cancel();
            }
        } else if (event.getPacket() instanceof ExplosionS2CPacket) {
            if (explosion.get()) {
                event.cancel();
            }
        }
    }
}
