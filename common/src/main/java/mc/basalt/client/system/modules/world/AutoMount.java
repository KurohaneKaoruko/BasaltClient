package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class AutoMount extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 3.0, 1.0, 6.0, 0.1);
    private final BooleanSetting horses = new BooleanSetting("马类", true);
    private final BooleanSetting pigs = new BooleanSetting("猪(有鞍)", true);
    private final BooleanSetting striders = new BooleanSetting("炽足兽", true);
    private final BooleanSetting inWater = new BooleanSetting("水中生效", false);

    private int delay;

    public AutoMount() {
        super("AutoMount", "自动骑乘", "靠近可骑乘生物时自动上骑。", Category.World);
        addSetting(range);
        addSetting(horses);
        addSetting(pigs);
        addSetting(striders);
        addSetting(inWater);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.player.isPassenger()) return;
        if (!inWater.get() && (mc.player.isInWater() || mc.player.isInLava())) return;

        if (delay > 0) {
            delay--;
            return;
        }

        for (var entity : mc.level.entitiesForRendering()) {
            if (mc.player.distanceToSqr(entity) > range.get() * range.get()) continue;
            if (entity.isVehicle()) continue; // 已被骑

            boolean target = false;
            if (horses.get() && entity instanceof AbstractHorse horse) {
                target = horse.isTamed() || horse.isSaddled();
            } else if (pigs.get() && entity instanceof Pig pig) {
                target = pig.isSaddled();
            } else if (striders.get() && entity instanceof Strider strider) {
                target = strider.isSaddled();
            }
            if (!target) continue;

            if (mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND).consumesAction()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                delay = 10;
                return;
            }
        }
    }
}
