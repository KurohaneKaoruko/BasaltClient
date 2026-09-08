package mc.basalt.client.system.modules.misc;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.util.Hand;

public class AntiAFK extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "挥手", "挥手", "旋转", "跳跃");
    private final DoubleSetting interval = new DoubleSetting("间隔(秒)", 10.0, 3.0, 60.0, 1.0);

    private long nextActionTime;
    private boolean restoreRotation;
    private float savedYaw;

    public AntiAFK() {
        super("AntiAFK", "防挂机", "定时产生动作避免被防挂机系统踢出。", Category.Misc);
        addSetting(mode);
        addSetting(interval);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        nextActionTime = System.currentTimeMillis() + (long) (interval.get() * 1000);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.currentScreen != null) {
            return;
        }

        if (restoreRotation) {
            // 旋转模式：下一 tick 恢复原视角，仅制造一次视角抖动
            mc.player.setYaw(savedYaw);
            restoreRotation = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) {
            return;
        }
        nextActionTime = now + (long) (interval.get() * 1000);

        switch (mode.get()) {
            case "旋转" -> {
                savedYaw = mc.player.getYaw();
                mc.player.setYaw(savedYaw + 1.0f);
                restoreRotation = true;
            }
            case "跳跃" -> {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
            default -> mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
