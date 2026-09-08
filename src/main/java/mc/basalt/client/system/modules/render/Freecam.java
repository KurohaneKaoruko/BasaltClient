package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.client.input.Input;
import net.minecraft.util.math.MathHelper;

public class Freecam extends Module {
    private final DoubleSetting speed = new DoubleSetting("速度", 1.0, 0.1, 5.0, 0.1);

    /** 保存被替换的玩家输入，关闭时恢复。 */
    private Input originalInput;

    public Freecam() {
        super("Freecam", "自由视角", "相机脱离身体自由移动，身体停在原地。", Category.Render);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            initializeCamera();
        }
    }

    private void initializeCamera() {
        BasaltClient.FREECAM_X = mc.player.getX();
        BasaltClient.FREECAM_Y = mc.player.getEyeY();
        BasaltClient.FREECAM_Z = mc.player.getZ();
        BasaltClient.FREECAM_YAW = mc.player.getYaw();
        BasaltClient.FREECAM_PITCH = mc.player.getPitch();
        BasaltClient.FREECAM_ENABLED = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BasaltClient.FREECAM_ENABLED = false;
        if (mc.player != null && originalInput != null) {
            mc.player.input = originalInput;
        }
        originalInput = null;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (!BasaltClient.FREECAM_ENABLED) {
            // 延迟初始化（例如从配置加载时世界尚未就绪）
            initializeCamera();
        }

        // 冻结玩家输入，让身体停在原地
        if (mc.player.input != null && originalInput == null) {
            originalInput = mc.player.input;
        }
        if (originalInput != null) {
            mc.player.input = new Input();
        }
        mc.player.setVelocity(0, 0, 0);

        // 相机旋转跟随鼠标（鼠标仍作用于玩家视角）
        BasaltClient.FREECAM_YAW = mc.player.getYaw();
        BasaltClient.FREECAM_PITCH = mc.player.getPitch();

        float forward = 0;
        float strafe = 0;
        float vertical = 0;

        if (mc.options.forwardKey.isPressed())
            forward++;
        if (mc.options.backKey.isPressed())
            forward--;
        if (mc.options.leftKey.isPressed())
            strafe++;
        if (mc.options.rightKey.isPressed())
            strafe--;
        if (mc.options.jumpKey.isPressed())
            vertical++;
        if (mc.options.sneakKey.isPressed())
            vertical--;

        if (forward == 0 && strafe == 0 && vertical == 0) {
            return;
        }

        double s = speed.get();
        float yawRad = BasaltClient.FREECAM_YAW * ((float) Math.PI / 180F);
        float sin = MathHelper.sin(yawRad);
        float cos = MathHelper.cos(yawRad);

        double mx = (strafe * cos - forward * sin) * s;
        double mz = (forward * cos + strafe * sin) * s;

        BasaltClient.FREECAM_X += mx;
        BasaltClient.FREECAM_Z += mz;
        BasaltClient.FREECAM_Y += vertical * s;
    }
}
