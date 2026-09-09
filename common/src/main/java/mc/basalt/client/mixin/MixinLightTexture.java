package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright：跳过 updateLightTexture（阻止暗光照贴图生成）。
 * turnOnLightLayer 正常运行（绑定光照贴图纹理），配合 getLightColor 拦截提供最大光照。
 */
@Mixin(LightTexture.class)
public class MixinLightTexture {
    @Inject(method = "updateLightTexture", at = @At("HEAD"), cancellable = true)
    private void onUpdate(CallbackInfo ci) {
        if (BasaltClient.FULLBRIGHT_ENABLED) {
            ci.cancel();
        }
    }
}