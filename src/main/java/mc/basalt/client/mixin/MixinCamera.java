package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
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
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    public void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView,
            float tickDelta, CallbackInfo ci) {
        if (BasaltClient.FREECAM_ENABLED) {
            this.setPos(BasaltClient.FREECAM_X, BasaltClient.FREECAM_Y, BasaltClient.FREECAM_Z);
            this.setRotation(BasaltClient.FREECAM_YAW, BasaltClient.FREECAM_PITCH);
        }
    }
}
