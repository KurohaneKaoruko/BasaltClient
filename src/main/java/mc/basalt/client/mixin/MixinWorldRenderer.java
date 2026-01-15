package mc.basalt.client.mixin;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.events.Render3DEvent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        // We create a new MatrixStack for rendering in the world
        MatrixStack matrixStack = new MatrixStack();
        // Note: In a real implementation, we might need to apply the camera rotation/position to the matrix stack
        // or pass the matrices directly. For simplicity, we pass an empty stack and modules handle projection if needed,
        // or we rely on the fact that we are in the render cycle.
        // Actually, Render3D usually needs the matrices.
        // But for standard "render in world" events, we often just pass the partial ticks.
        
        EventBus.post(new Render3DEvent(matrixStack, tickCounter.getTickDelta(true)));
    }
}
