package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Packet", "Packet", "Bucket", "Minecraft");

    public NoFall() {
        super("NoFall", "无摔落", "避免摔落伤害，支持多种模式。", Category.Movement);
        addSetting(mode);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            switch (mode.get()) {
                case "Packet" -> packet.onGround = true;
                case "Bucket" -> {
                    // 仅在即将落地时标记 onGround
                    if (mc.player.fallDistance > 3.0f) {
                        packet.onGround = true;
                    }
                }
                case "Minecraft" -> {
                    // 模拟原版：仅在水/蛛网/梯子中标记
                    if (mc.player.isInWater() || mc.player.onClimbable()
                        || mc.level.getBlockState(mc.player.blockPosition()).is(net.minecraft.tags.BlockTags.WOOL)) {
                        packet.onGround = true;
                    }
                }
            }
        }
    }
}
