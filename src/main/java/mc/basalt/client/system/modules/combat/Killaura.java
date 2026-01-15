package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class Killaura extends Module {
    private final DoubleSetting range = new DoubleSetting("距离", 4.0, 1.0, 6.0, 0.1);
    private final BooleanSetting players = new BooleanSetting("攻击玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("攻击怪物", true);
    private final BooleanSetting autoDelay = new BooleanSetting("自动冷却", true);

    public Killaura() {
        super("Killaura", "杀戮光环", "自动攻击周围的实体。", Category.Combat);
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(autoDelay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != mc.player && entity.isAlive()) {
                if (entity instanceof PlayerEntity) {
                    if (!players.get()) continue;
                } else {
                    if (!mobs.get()) continue;
                }

                if (mc.player.distanceTo(entity) <= range.get()) {
                    if (autoDelay.get()) {
                        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) continue;
                    }

                    mc.interactionManager.attackEntity(mc.player, entity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    break; // Attack one at a time
                }
            }
        }
    }
}
