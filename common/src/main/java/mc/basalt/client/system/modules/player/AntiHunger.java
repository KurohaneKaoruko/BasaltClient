package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class AntiHunger extends Module {
    public AntiHunger() {
        super("AntiHunger", "防饥饿", "减少服务端的饥饿值消耗（拦截疾跑状态包）。", Category.Player);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;

        // 拦截疾跑状态切换包（服务端据此计算 exhaustion）
        if (event.getPacket() instanceof ServerboundPlayerCommandPacket packet) {
            var action = packet.getAction();
            if (action == ServerboundPlayerCommandPacket.Action.START_SPRINTING
                    || action == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
                // 仅当玩家真的在移动时放行，静止时拦截
                if (mc.player.input.forwardImpulse == 0 && mc.player.input.leftImpulse == 0) {
                    event.cancel();
                }
            }
        }
    }
}
