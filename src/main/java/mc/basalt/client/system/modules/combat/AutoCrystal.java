package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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
        if (mc.player == null || mc.world == null)
            return;

        if (timer < delay.get()) {
            timer++;
            return;
        }
        timer = 0;

        // Break Crystals
        List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(EndCrystalEntity.class,
                new Box(mc.player.getBlockPos()).expand(range.get()),
                c -> true);

        if (!crystals.isEmpty()) {
            EndCrystalEntity target = crystals.stream()
                    .min(Comparator.comparingDouble(c -> mc.player.distanceTo(c)))
                    .orElse(null);

            if (target != null) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                return; // Prioritize breaking
            }
        }

        // Place Crystals
        // Simplified logic: Find obsidian/bedrock near target (or player if no target)
        // For simplicity, just place on nearest valid block in range

        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
                || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            Hand hand = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? Hand.MAIN_HAND : Hand.OFF_HAND;

            // Scan for placement
            BlockPos pPos = mc.player.getBlockPos();
            int r = (int) Math.ceil(range.get());

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = pPos.add(x, y, z);
                        if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.get() * range.get())
                            continue;

                        // Check if valid base (Obsidian or Bedrock)
                        if (mc.world.getBlockState(pos).isOf(net.minecraft.block.Blocks.OBSIDIAN) ||
                                mc.world.getBlockState(pos).isOf(net.minecraft.block.Blocks.BEDROCK)) {

                            // Check space above (Air)
                            if (mc.world.getBlockState(pos.up()).isAir()) {
                                // Place
                                mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(
                                        pos.toCenterPos().add(0, 1, 0), Direction.UP, pos, false));
                                mc.player.swingHand(hand);
                                return; // One per tick
                            }
                        }
                    }
                }
            }
        }
    }
}
