package mc.basalt.client.mixin;

import mc.basalt.client.system.commands.CommandManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 聊天命令拦截（加载器无关）：以 "." 开头的消息交给命令系统处理。
 */
@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (CommandManager.tryHandle(message)) {
            ci.cancel();
        }
    }
}
