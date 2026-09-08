package mc.basalt.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * 世界空间 3D 渲染工具：方框、填充、连线。
 * 传入的矩阵必须已应用相机旋转（见 Render3DEvent），坐标为相对相机位置的世界偏移。
 */
public final class RenderUtils {
    private RenderUtils() {
    }

    /** 进入 3D 渲染状态（透视、无深度写入）。 */
    public static void begin3D() {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.lineWidth(1.5f);
    }

    /** 恢复默认渲染状态。 */
    public static void end3D() {
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /** 绘制方框轮廓（12 条边）。box 为世界坐标。 */
    public static void drawBoxOutline(MatrixStack matrices, Camera camera, Box box, int argb) {
        Vec3d camPos = camera.getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        double x1 = box.minX - camPos.x;
        double y1 = box.minY - camPos.y;
        double z1 = box.minZ - camPos.z;
        double x2 = box.maxX - camPos.x;
        double y2 = box.maxY - camPos.y;
        double z2 = box.maxZ - camPos.z;

        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        // 底面
        line(buffer, matrix, x1, y1, z1, x2, y1, z1, argb);
        line(buffer, matrix, x2, y1, z1, x2, y1, z2, argb);
        line(buffer, matrix, x2, y1, z2, x1, y1, z2, argb);
        line(buffer, matrix, x1, y1, z2, x1, y1, z1, argb);
        // 顶面
        line(buffer, matrix, x1, y2, z1, x2, y2, z1, argb);
        line(buffer, matrix, x2, y2, z1, x2, y2, z2, argb);
        line(buffer, matrix, x2, y2, z2, x1, y2, z2, argb);
        line(buffer, matrix, x1, y2, z2, x1, y2, z1, argb);
        // 竖边
        line(buffer, matrix, x1, y1, z1, x1, y2, z1, argb);
        line(buffer, matrix, x2, y1, z1, x2, y2, z1, argb);
        line(buffer, matrix, x2, y1, z2, x2, y2, z2, argb);
        line(buffer, matrix, x1, y1, z2, x1, y2, z2, argb);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    /** 绘制半透明填充方框（6 个面）。box 为世界坐标。 */
    public static void drawBoxFill(MatrixStack matrices, Camera camera, Box box, int argb) {
        Vec3d camPos = camera.getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float x1 = (float) (box.minX - camPos.x);
        float y1 = (float) (box.minY - camPos.y);
        float z1 = (float) (box.minZ - camPos.z);
        float x2 = (float) (box.maxX - camPos.x);
        float y2 = (float) (box.maxY - camPos.y);
        float z2 = (float) (box.maxZ - camPos.z);

        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // 上面 / 下面
        quad(buffer, matrix,
                x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, argb);
        quad(buffer, matrix,
                x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, argb);
        // 北面 / 南面
        quad(buffer, matrix,
                x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, argb);
        quad(buffer, matrix,
                x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, argb);
        // 西面 / 东面
        quad(buffer, matrix,
                x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, argb);
        quad(buffer, matrix,
                x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, argb);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    /** 绘制一条线段（世界坐标）。 */
    public static void drawLine(MatrixStack matrices, Camera camera, Vec3d from, Vec3d to, int argb) {
        Vec3d camPos = camera.getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        line(buffer, matrix,
                from.x - camPos.x, from.y - camPos.y, from.z - camPos.z,
                to.x - camPos.x, to.y - camPos.y, to.z - camPos.z, argb);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void line(BufferBuilder buffer, Matrix4f matrix,
            double x1, double y1, double z1, double x2, double y2, double z2, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4, int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
        buffer.vertex(matrix, x4, y4, z4).color(r, g, b, a);
    }
}
