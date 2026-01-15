package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    private final List<Packet<?>> packets = new ArrayList<>();
    private OtherClientPlayerEntity fakePlayer;

    public Blink() {
        super("Blink", "瞬移", "暂停发包，实现瞬移效果。", Category.Player);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        packets.clear();
        if (mc.player != null) {
            fakePlayer = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
            fakePlayer.copyFrom(mc.player);
            fakePlayer.setHeadYaw(mc.player.getHeadYaw());
            mc.world.addEntity(fakePlayer);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.world != null && fakePlayer != null) {
            fakePlayer.discard();
            fakePlayer = null;
        }

        // Send packets
        if (mc.getNetworkHandler() != null) {
            for (Packet<?> packet : packets) {
                mc.getNetworkHandler().sendPacket(packet);
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
        if (packet instanceof PlayerMoveC2SPacket ||
                packet instanceof net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket ||
                packet instanceof net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket ||
                packet instanceof net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket) {

            event.cancel();
            packets.add(packet);
        }

        // Allow KeepAlive and Chat to pass through
        if (packet instanceof KeepAliveC2SPacket || packet instanceof ChatMessageC2SPacket) {
            return;
        }
    }
}
