package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTickCounter {
    @Shadow
    private float lastFrameDuration;

    @Inject(
            method = "beginRenderTick(J)I",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;prevTimeMillis:J",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0
            )
    )
    private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        // 放大帧时长 → 加快 tick 增量累计（Timer 变速齿轮）
        lastFrameDuration *= BasaltClient.TIMER;
    }
}
