package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reach：扩展原版实体交互距离（视线拾取与攻击判定）。
 */
@Mixin(GameRenderer.class)
public class MixinGameRendererPick {
    @Redirect(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;entityInteractionRange()D"))
    private double modifyEntityRange(LocalPlayer player) {
        if (BasaltClient.REACH_VALUE > 0) {
            return BasaltClient.REACH_VALUE;
        }
        return player.entityInteractionRange();
    }
}
