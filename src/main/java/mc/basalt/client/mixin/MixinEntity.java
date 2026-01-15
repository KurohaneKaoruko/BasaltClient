package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;adjustMovementForSneaking(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectAdjustMovementForSneaking(Entity instance, Vec3d movement, MovementType type) {
        if (BasaltClient.SAFEWALK_ENABLED && instance == BasaltClient.MC.player) {
            // Force sneaking logic for movement adjustment
            return this.invokeAdjustMovementForSneaking(movement, type);
        }
        // Fallback to original logic (which checks isSneaking inside usually, wait)
        // Actually adjustMovementForSneaking in vanilla just DOES the calculation.
        // The check "if (this.isSneaking() || ...)" is done BEFORE calling it in
        // move().
        // So if we are here, it means vanilla called it.

        // Wait, looking at Entity.move source (1.21):
        // if (this.isSneaking() && (this instanceof PlayerEntity || ...)) {
        // vec3d = this.adjustMovementForSneaking(vec3d, movementType);
        // }

        // So we need to target the CONDITION or inject before it.
        // But Redirecting the call won't work if the call never happens.

        return this.invokeAdjustMovementForSneaking(movement, type);
    }

    // Shadow the method to call it? No, it's public/protected usually.
    // But we can't shadow if we are redirecting?
    // Actually, we need to inject into 'move' and force the call.

    // Easier way: Mixin isSneaking() ?
    // No, that affects rendering and other things.

    // Correct way for SafeWalk:
    // Inject at the point where vanilla checks for sneaking in move() and force it
    // to true for this check.
    // OR, since 1.21 might be different, let's look at how Meteor does it.
    // Meteor MixinEntity.java -> @Redirect(method = "move", ... target =
    // "isSneaking")

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    private boolean redirectIsSneaking(Entity instance) {
        if (BasaltClient.SAFEWALK_ENABLED && instance == BasaltClient.MC.player) {
            return true;
        }
        return instance.isSneaking();
    }

    @org.spongepowered.asm.mixin.Shadow
    public abstract Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type);

    public Vec3d invokeAdjustMovementForSneaking(Vec3d movement, MovementType type) {
        return adjustMovementForSneaking(movement, type);
    }
}
