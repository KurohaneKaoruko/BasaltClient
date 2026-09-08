package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "无摔落", "避免摔落伤害。", Category.Movement);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            // onGround 字段已通过 access widener 变为可写
            packet.onGround = true;
        }
    }
}
