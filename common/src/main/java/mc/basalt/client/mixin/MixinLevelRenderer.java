package mc.basalt.client.mixin;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.events.Render3DEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        PoseStack matrixStack = new PoseStack();
        matrixStack.mulPose(positionMatrix);
        EventBus.post(new Render3DEvent(matrixStack, tickCounter.getGameTimeDeltaPartialTick(true), camera));
    }

    /** Fullbright Luminance 模式 + XRay：拦截逐顶点光照计算，返回最大光照值。 */
    @Inject(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I", at = @At("HEAD"), cancellable = true)
    private static void onGetLightColor(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (BasaltClient.FULLBRIGHT_ENABLED || BasaltClient.XRAY_ENABLED) {
            cir.setReturnValue(0xF000F0);
        }
    }
}
