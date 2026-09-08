package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("TAIL"))
    public void onUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
            float tickDelta, CallbackInfo ci) {
        if (BasaltClient.FREECAM_ENABLED) {
            this.setPosition(BasaltClient.FREECAM_X, BasaltClient.FREECAM_Y, BasaltClient.FREECAM_Z);
            this.setRotation(BasaltClient.FREECAM_YAW, BasaltClient.FREECAM_PITCH);
        }
    }
}
