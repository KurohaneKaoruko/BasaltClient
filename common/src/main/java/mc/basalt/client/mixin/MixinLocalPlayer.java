package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * NoSlow：物品使用时的移动减速系数 0.2 → 1.0（不减速）。
 */
@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2f))
    private float modifySlowdown(float original) {
        if (BasaltClient.NOSLOW_ENABLED) {
            return 1.0f;
        }
        return original;
    }
}
