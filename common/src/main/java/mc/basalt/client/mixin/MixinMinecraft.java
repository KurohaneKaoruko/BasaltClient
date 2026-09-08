package mc.basalt.client.mixin;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.config.ConfigManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        EventBus.post(new TickEvent.Pre());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        EventBus.post(new TickEvent.Post());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        // 退出游戏时保存配置（加载器无关）
        ConfigManager.save();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void onDestroy(CallbackInfo ci) {
        // 兜底：部分退出路径不经过 stop()，destroy() 时再保存一次
        ConfigManager.save();
    }
}
