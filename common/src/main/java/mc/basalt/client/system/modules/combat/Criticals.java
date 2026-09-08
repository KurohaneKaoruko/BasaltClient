package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class Criticals extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Packet", "Packet", "Jump");

    public Criticals() {
        super("Criticals", "刀刀暴击", "攻击时自动产生暴击效果。", Category.Combat);
        addSetting(mode);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.getConnection() == null)
            return;

        if (event.getPacket() instanceof ServerboundInteractPacket) {
            // Note: Ideally we check if it's an ATTACK action, but that requires accessors
            // in 1.21.
            // Assuming most interactions that matter for crits are attacks.

            if (!mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava())
                return;

            if (mode.get().equals("Packet")) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();

                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
            } else if (mode.get().equals("Jump")) {
                mc.player.jumpFromGround();
            }
        }
    }
}
