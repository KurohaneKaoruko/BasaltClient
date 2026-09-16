package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitBoxes：扩大实体选取半径，攻击更容易命中。
 */
@Mixin(Entity.class)
public class MixinEntityHitbox {
    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    private void onGetPickRadius(CallbackInfoReturnable<Float> cir) {
        if (BasaltClient.HITBOXES_ENABLED) {
            cir.setReturnValue(BasaltClient.HITBOXES_VALUE);
        }
    }
}
