package mc.basalt.client.mixin;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.events.Render3DEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRender(DeltaTracker tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        // positionMatrix 即本帧的世界模型视图矩阵（含相机旋转），
        // 与全局投影矩阵配合即可在模块中绘制世界空间几何体。
        PoseStack matrixStack = new PoseStack();
        matrixStack.mulPose(positionMatrix);
        EventBus.post(new Render3DEvent(matrixStack, tickCounter.getGameTimeDeltaPartialTick(true), camera));
    }
}
