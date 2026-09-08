package mc.basalt.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 全局禁用原版毛玻璃：1.21.2+ 的模糊后处理为帧末延迟合成，
 * 会覆盖本客户端自绘的界面内容（层级冲突），且自带界面均不依赖毛玻璃。
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "processBlurEffect", at = @At("HEAD"), cancellable = true)
    private void onProcessBlurEffect(CallbackInfo ci) {
        ci.cancel();
    }
}
