package mc.basalt.client.system.modules.movement;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Step extends Module {
    private final DoubleSetting height = new DoubleSetting("高度", 1.0, 0.5, 2.5, 0.5);
    private final ModeSetting mode = new ModeSetting("模式", "Vanilla", "Vanilla");
    private double originalBaseValue;
    private boolean capturedOriginal;

    public Step() {
        super("Step", "自动跳跃", "无需跳跃即可走上高处。", Category.Movement);
        addSetting(height);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        capturedOriginal = false;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null)
            return;

        if (mode.get().equals("Vanilla")) {
            AttributeInstance instance = mc.player.getAttribute(Attributes.STEP_HEIGHT);
            if (instance == null)
                return;

            if (!capturedOriginal) {
                originalBaseValue = instance.getBaseValue();
                capturedOriginal = true;
            }

            instance.setBaseValue(height.get());
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
