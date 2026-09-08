package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class AutoCrystal extends Module {
    private final DoubleSetting range = new DoubleSetting("范围", 5.0, 1.0, 6.0, 0.1);
    private final DoubleSetting delay = new DoubleSetting("延迟", 2.0, 0.0, 20.0, 1.0);
    private int timer = 0;

    public AutoCrystal() {
        super("AutoCrystal", "自动水晶", "自动放置和破坏末影水晶。", Category.Combat);
        addSetting(range);
        addSetting(delay);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null)
            return;

        if (timer < delay.get()) {
            timer++;
            return;
        }
        timer = 0;

        // Break Crystals
        List<EndCrystal> crystals = mc.level.getEntitiesOfClass(EndCrystal.class,
                new AABB(mc.player.blockPosition()).inflate(range.get()),
                c -> true);

        if (!crystals.isEmpty()) {
            EndCrystal target = crystals.stream()
                    .min(Comparator.comparingDouble(c -> mc.player.distanceTo(c)))
                    .orElse(null);

            if (target != null) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                return; // Prioritize breaking
            }
        }

        // Place Crystals
        // Simplified logic: Find obsidian/bedrock near target (or player if no target)
        // For simplicity, just place on nearest valid block in range

        if (mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL
                || mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL) {
            InteractionHand hand = mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

            // Scan for placement
            BlockPos pPos = mc.player.blockPosition();
            int r = (int) Math.ceil(range.get());

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = pPos.offset(x, y, z);
                        if (mc.player.distanceToSqr(pos.getCenter()) > range.get() * range.get())
                            continue;

                        // Check if valid base (Obsidian or Bedrock)
                        if (mc.level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.OBSIDIAN) ||
                                mc.level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEDROCK)) {

                            // Check space above (Air)
                            if (mc.level.getBlockState(pos.above()).isAir()) {
                                // Place
                                mc.gameMode.useItemOn(mc.player, hand, new BlockHitResult(
                                        pos.getCenter().add(0, 1, 0), Direction.UP, pos, false));
                                mc.player.swing(hand);
                                return; // One per tick
                            }
                        }
                    }
                }
            }
        }
    }
}
