package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.4 光照贴图由着色器计算，全亮改为直接让 getBrightness 返回最大值。
 */
@Mixin(LightTexture.class)
public class MixinLightTexture {
    @Inject(method = "getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(DimensionType type, int blockLight, CallbackInfoReturnable<Float> cir) {
        if (BasaltClient.FULLBRIGHT_ENABLED) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness2(float alphaDarkness, int blockLight, CallbackInfoReturnable<Float> cir) {
        if (BasaltClient.FULLBRIGHT_ENABLED) {
            cir.setReturnValue(1.0f);
        }
    }
}
