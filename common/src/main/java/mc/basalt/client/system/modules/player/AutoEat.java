package mc.basalt.client.system.modules.player;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public class AutoEat extends Module {
    private static final Set<Item> UNSAFE_FOODS = Set.of(
            Items.ROTTEN_FLESH,
            Items.SPIDER_EYE,
            Items.POISONOUS_POTATO,
            Items.PUFFERFISH,
            Items.CHICKEN,
            Items.SUSPICIOUS_STEW);

    private final DoubleSetting hunger = new DoubleSetting("饥饿阈值", 14.0, 1.0, 19.0, 1.0);
    private final BooleanSetting safeFoodOnly = new BooleanSetting("仅安全食物", true);

    private boolean eating;
    private int previousSlot = -1;

    public AutoEat() {
        super("AutoEat", "自动进食", "饥饿值过低时自动吃快捷栏中的食物。", Category.Player);
        addSetting(hunger);
        addSetting(safeFoodOnly);
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) {
            stopEating();
            return;
        }

        boolean needFood = mc.player.getFoodData().getFoodLevel() <= hunger.get();

        if (eating) {
            if (!needFood || mc.screen != null) {
                stopEating();
                return;
            }

            if (!mc.player.isUsingItem()) {
                int slot = findFoodSlot();
                if (slot == -1) {
                    stopEating();
                    return;
                }
                mc.player.getInventory().selected = slot;
            }
            mc.options.keyUse.setDown(true);
        } else if (needFood && mc.screen == null) {
            int slot = findFoodSlot();
            if (slot == -1) {
                return;
            }
            previousSlot = mc.player.getInventory().selected;
            eating = true;
            mc.player.getInventory().selected = slot;
            mc.options.keyUse.setDown(true);
        }
    }

    private void stopEating() {
        if (!eating) {
            return;
        }
        eating = false;
        mc.options.keyUse.setDown(false);
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = previousSlot;
        }
        previousSlot = -1;
    }

    @Override
    public void onDisable() {
        stopEating();
    }

    private int findFoodSlot() {
        int bestSlot = -1;
        int bestNutrition = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) {
                continue;
            }
            if (safeFoodOnly.get() && UNSAFE_FOODS.contains(stack.getItem())) {
                continue;
            }
            if (food.nutrition() > bestNutrition) {
                bestNutrition = food.nutrition();
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
