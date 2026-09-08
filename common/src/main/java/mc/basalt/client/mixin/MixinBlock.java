package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.modules.render.XRay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void shouldRenderFace(BlockState state, BlockGetter world, BlockPos pos, Direction side,
            BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
        if (BasaltClient.XRAY_ENABLED) {
            cir.setReturnValue(XRay.isOre(state.getBlock()));
        }
    }
}