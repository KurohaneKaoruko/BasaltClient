package mc.basalt.client.event.events;

import mc.basalt.client.event.Event;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

/**
 * 世界空间 3D 渲染事件（在世界渲染完成后触发）。
 * matrices 已应用相机旋转，绘制时坐标需减去相机位置（见 RenderUtils）。
 */
public class Render3DEvent extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;
    private final Camera camera;

    public Render3DEvent(MatrixStack matrices, float tickDelta, Camera camera) {
        this.matrices = matrices;
        this.tickDelta = tickDelta;
        this.camera = camera;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public float getTickDelta() {
        return tickDelta;
    }

    public Camera getCamera() {
        return camera;
    }
}
