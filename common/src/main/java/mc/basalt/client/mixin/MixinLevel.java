package mc.basalt.client.mixin;

import mc.basalt.client.system.modules.render.NoWeather;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoWeather：客户端强制雨/雷等级为 0，服务端同步包不会覆盖。
 */
@Mixin(Level.class)
public class MixinLevel {
    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void onGetRainLevel(float delta, CallbackInfoReturnable<Float> cir) {
        if (NoWeather.isActive()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void onGetThunderLevel(float delta, CallbackInfoReturnable<Float> cir) {
        if (NoWeather.isActive()) {
            cir.setReturnValue(0.0f);
        }
    }
}
