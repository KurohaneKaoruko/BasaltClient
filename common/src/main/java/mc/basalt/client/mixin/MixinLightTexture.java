package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightTexture.class)
public class MixinLightTexture {
    @ModifyArgs(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"))
    private void updateColor(Args args) {
        if (BasaltClient.FULLBRIGHT_ENABLED) {
            args.set(2, 0xFFFFFFFF); // 强制全亮度（白色）
        }
    }
}
