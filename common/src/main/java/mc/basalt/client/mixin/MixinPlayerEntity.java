package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SafeWalk：让本地玩家始终应用“潜行悬崖边缘裁剪”逻辑（不掉下边缘）。
 * 1.21.1 中该逻辑位于 PlayerEntity#clipAtLedge（原版仅在潜行时返回 true）。
 */
@Mixin(Player.class)
public abstract class MixinPlayerEntity {
    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    private void onClipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        if (BasaltClient.SAFEWALK_ENABLED && (Object) this == BasaltClient.MC.player) {
            cir.setReturnValue(true);
        }
    }
}
