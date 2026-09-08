package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    private final List<Packet<?>> packets = new ArrayList<>();
    private RemotePlayer fakePlayer;

    public Blink() {
        super("Blink", "瞬移", "暂停发包，实现瞬移效果。", Category.Player);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        packets.clear();
        if (mc.player != null) {
            fakePlayer = new RemotePlayer(mc.level, mc.player.getGameProfile());
            fakePlayer.restoreFrom(mc.player);
            fakePlayer.setYHeadRot(mc.player.getYHeadRot());
            mc.level.addEntity(fakePlayer);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.level != null && fakePlayer != null) {
            fakePlayer.discard();
            fakePlayer = null;
        }

        // Send packets
        if (mc.getConnection() != null) {
            for (Packet<?> packet : packets) {
                mc.getConnection().send(packet);
            }
        }
        packets.clear();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null)
            return;

        Packet<?> packet = event.getPacket();

        // Filter packets
        if (packet instanceof ServerboundMovePlayerPacket ||
                packet instanceof net.minecraft.network.protocol.game.ServerboundUseItemPacket ||
                packet instanceof net.minecraft.network.protocol.game.ServerboundUseItemOnPacket ||
                packet instanceof net.minecraft.network.protocol.game.ServerboundInteractPacket) {

            event.cancel();
            packets.add(packet);
        }

        // Allow KeepAlive and Chat to pass through
        if (packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundChatPacket) {
            return;
        }
    }
}
