package mc.basalt.client.event.events;

import mc.basalt.client.event.Event;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * 世界空间 3D 渲染事件（在世界渲染完成后触发）。
 * matrices 已应用相机旋转，绘制时坐标需减去相机位置（见 RenderUtils）。
 */
public class Render3DEvent extends Event {
    private final PoseStack matrices;
    private final float tickDelta;
    private final Camera camera;

    public Render3DEvent(PoseStack matrices, float tickDelta, Camera camera) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.camera = camera;
    }

    public PoseStack getMatrices() {
        return matrices;
    }

    public float getGameTimeDeltaPartialTick() {
        return tickDelta;
    }

    public Camera getCamera() {
        return camera;
    }
}
