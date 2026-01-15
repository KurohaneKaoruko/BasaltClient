package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void updateColor(Args args) {
        if (BasaltClient.FULLBRIGHT_ENABLED) {
            args.set(2, 0xFFFFFFFF); // Force Full Brightness (White)
        }
    }
}
