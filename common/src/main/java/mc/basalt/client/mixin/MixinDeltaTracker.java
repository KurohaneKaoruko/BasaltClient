package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
public class MixinDeltaTracker {
    @Shadow
    private float deltaTicks;

    @Inject(
            method = "advanceGameTime(J)I",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/DeltaTracker$Timer;lastMs:J",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0
            )
    )
    private void onAdvanceGameTime(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        // 放大本帧 tick 增量（Timer 变速齿轮）
        deltaTicks *= BasaltClient.TIMER;
    }
}
