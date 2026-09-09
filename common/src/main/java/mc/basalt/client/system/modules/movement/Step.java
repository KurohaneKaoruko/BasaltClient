package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Step extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla", "Packet");
    private final DoubleSetting height = new DoubleSetting("高度", 1.0, 0.5, 2.5, 0.5);
    private final BooleanSetting entities = new BooleanSetting("对实体生效", false);
    private final BooleanSetting inLiquids = new BooleanSetting("液体中生效", false);

    private double originalBaseValue;
    private boolean capturedOriginal;

    public Step() {
        super("Step", "自动上台阶", "无需跳跃走上高处，支持多种模式。", Category.Movement);
        addSetting(mode); addSetting(height); addSetting(entities); addSetting(inLiquids);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        capturedOriginal = false;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (!inLiquids.get() && (mc.player.isInWater() || mc.player.isInLava())) return;

        if (mode.get().equals("Vanilla")) {
            AttributeInstance instance = mc.player.getAttribute(Attributes.STEP_HEIGHT);
            if (instance == null) return;

            if (!capturedOriginal) {
                originalBaseValue = instance.getBaseValue();
                capturedOriginal = true;
            }
            instance.setBaseValue(height.get());
        } else if (mode.get().equals("Packet")) {
            // Packet 模式：在即将碰撞时发送步骤包
            if (mc.player.onGround() && mc.player.horizontalCollision) {
                mc.player.setOnGround(false); // 触发步骤
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, height.get() * 0.42,
                    mc.player.getDeltaMovement().z);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null && capturedOriginal) {
            AttributeInstance instance = mc.player.getAttribute(Attributes.STEP_HEIGHT);
            if (instance != null) {
                instance.setBaseValue(originalBaseValue);
            }
        }
    }
}
