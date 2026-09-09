package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;

public class LongJump extends Module {
    private final DoubleSetting boost = new DoubleSetting("跳跃力度", 1.5, 1.0, 5.0, 0.1);
    private final BooleanSetting autoJump = new BooleanSetting("自动跳跃", true);
    private final BooleanSetting inLiquids = new BooleanSetting("液体中生效", false);

    private boolean jumped;

    public LongJump() {
        super("LongJump", "长跳", "跳跃时大幅增加水平距离。", Category.Movement);
        addSetting(boost); addSetting(autoJump); addSetting(inLiquids);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        jumped = false;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        if (!inLiquids.get() && (mc.player.isInWater() || mc.player.isInLava())) return;

        if (mc.player.onGround()) {
            jumped = false;
            if (autoJump.get() && mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0) {
                mc.player.jumpFromGround();
                jumped = true;
            }
        } else if (!jumped && mc.options.keyJump.isDown() && mc.player.getDeltaMovement().y > 0) {
            jumped = true;
        }

        if (jumped && !mc.player.onGround()) {
            // 空中增加水平速度
            double curSpeed = Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x
                    + mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
            if (curSpeed > 0.1) {
                float yaw = mc.player.getYRot() * ((float) Math.PI / 180F);
                double target = curSpeed * boost.get();
                double vx = mc.player.getDeltaMovement().x / curSpeed * target;
                double vz = mc.player.getDeltaMovement().z / curSpeed * target;
                mc.player.setDeltaMovement(vx, mc.player.getDeltaMovement().y, vz);
            }
        }
    }
}
