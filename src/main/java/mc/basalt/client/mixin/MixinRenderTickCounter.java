package mc.basalt.client.mixin;

import mc.basalt.client.BasaltClient;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {
    @Shadow
    public float lastFrameDuration;

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;prevTimeMillis:J", opcode = org.objectweb.asm.Opcodes.PUTFIELD, ordinal = 0), method = "beginRenderTick")
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        lastFrameDuration *= BasaltClient.TIMER;
    }
}
