package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.render.XRay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.4 的 shouldRenderFace 签名为 (state, neighborState, direction)。
 * XRay：矿物方块的面渲染，其余方块的面隐藏。
 */
@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void shouldRenderFace(BlockState state, BlockState neighborState, Direction direction,
            CallbackInfoReturnable<Boolean> cir) {
        if (BasaltClient.XRAY_ENABLED) {
            cir.setReturnValue(XRay.isOre(state.getBlock()));
        }
    }
}
