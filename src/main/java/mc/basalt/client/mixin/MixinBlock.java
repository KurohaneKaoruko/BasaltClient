package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side,
            BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        if (BasaltClient.XRAY_ENABLED) {
            // Check if block is an ore/visible block
            // Accessing XRay instance requires modules init
            if (XRay.isOre(state.getBlock())) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    // We also need to make non-ores transparent to see through them
    // This is handled by shouldDrawSide returning false for hidden blocks,
    // but we also need to ensure occlusion culling doesn't hide ores.
}
