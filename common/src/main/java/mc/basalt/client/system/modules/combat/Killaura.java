package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;

public class Killaura extends Module {
    private final ModeSetting rotation = new ModeSetting("旋转模式", "始终", "始终", "攻击时", "关闭");
    private final ModeSetting priority = new ModeSetting("目标优先", "最近", "最近", "最低血量", "最近视角", "最远");
    private final ModeSetting weapon = new ModeSetting("武器限制", "全部", "全部", "仅剑", "仅斧");
    private final ModeSetting shieldMode = new ModeSetting("盾牌处理", "忽略", "忽略", "破坏", "无");

    private final DoubleSetting range = new DoubleSetting("距离", 4.5, 1.0, 6.0, 0.1);
    private final DoubleSetting wallsRange = new DoubleSetting("穿墙距离", 3.5, 0.0, 6.0, 0.1);
    private final DoubleSetting fov = new DoubleSetting("视野角度", 360.0, 30.0, 360.0, 10.0);

    private final BooleanSetting players = new BooleanSetting("攻击玩家", true);
    private final BooleanSetting mobs = new BooleanSetting("攻击怪物", true);
    private final BooleanSetting animals = new BooleanSetting("攻击动物", false);
    private final BooleanSetting ignoreNamed = new BooleanSetting("忽略命名实体", false);
    private final BooleanSetting ignoreTamed = new BooleanSetting("忽略驯服的", false);
    private final BooleanSetting ignorePassive = new BooleanSetting("忽略被动怪物", true);
    private final BooleanSetting onlyOnClick = new BooleanSetting("仅按住攻击键", false);
    private final BooleanSetting onlyOnLook = new BooleanSetting("仅准星命中", false);
    private final BooleanSetting autoSwitch = new BooleanSetting("自动切换武器", false);
    private final BooleanSetting swapBack = new BooleanSetting("攻击后切回", false);
    private final BooleanSetting autoDelay = new BooleanSetting("自动冷却", true);
    private final BooleanSetting customDelay = new BooleanSetting("自定义延迟", false);
    private final DoubleSetting hitDelay = new DoubleSetting("攻击间隔tick", 11.0, 0.0, 40.0, 1.0);
    private final BooleanSetting pauseOnUse = new BooleanSetting("使用物品时暂停", false);
    private final BooleanSetting pauseOnLag = new BooleanSetting("延迟时暂停", false);

    private int hitTimer;
    private int switchTimer;
    private int previousSlot = -1;
    private boolean swapped;

    public Killaura() {
        super("Killaura", "杀戮光环", "自动攻击周围实体，支持多种模式与精细过滤。", Category.Combat);
        addSetting(rotation); addSetting(priority); addSetting(weapon); addSetting(shieldMode);
        addSetting(range); addSetting(wallsRange); addSetting(fov);
        addSetting(players); addSetting(mobs); addSetting(animals);
        addSetting(ignoreNamed); addSetting(ignoreTamed); addSetting(ignorePassive);
        addSetting(onlyOnClick); addSetting(onlyOnLook);
        addSetting(autoSwitch); addSetting(swapBack);
        addSetting(autoDelay); addSetting(customDelay); addSetting(hitDelay);
        addSetting(pauseOnUse); addSetting(pauseOnLag);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        previousSlot = -1;
        swapped = false;
        hitTimer = 0;
        switchTimer = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopAttacking();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (!mc.player.isAlive()) { stopAttacking(); return; }
        if (pauseOnUse.get() && (mc.player.isUsingItem() || mc.gameMode.isDestroying())) { stopAttacking(); return; }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) { stopAttacking(); return; }

        LivingEntity target = findTarget();
        if (target == null) { stopAttacking(); return; }

        // 自动切换武器
        if (autoSwitch.get()) {
            int weaponSlot = findWeapon(target);
            if (weaponSlot != -1 && mc.player.getInventory().selected != weaponSlot) {
                if (!swapped) {
                    previousSlot = mc.player.getInventory().selected;
                    swapped = true;
                }
                mc.player.getInventory().selected = weaponSlot;
                switchTimer = 1;
            }
        }

        // 武器检查
        if (!acceptableWeapon(mc.player.getMainHandItem())) { stopAttacking(); return; }

        // 旋转
        if (rotation.get().equals("始终") || (rotation.get().equals("攻击时") && delayCheck())) {
            lookAt(target);
        }

        if (delayCheck()) {
            attack(target);
        }
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!entityCheck(living)) continue;

            double score = switch (priority.get()) {
                case "最近" -> -mc.player.distanceToSqr(entity);
                case "最远" -> mc.player.distanceToSqr(entity);
                case "最低血量" -> -living.getHealth();
                case "最近视角" -> -getAngleTo(entity);
                default -> -mc.player.distanceToSqr(entity);
            };

            if (score > bestScore) {
                bestScore = score;
                best = living;
            }
        }

        return best;
    }

    private boolean entityCheck(LivingEntity entity) {
        if (entity == mc.player || !entity.isAlive() || entity.isDeadOrDying()) return false;

        // 距离检查（使用 AABB 最近点）
        AABB box = entity.getBoundingBox();
        double distSq = mc.player.distanceToSqr(
            net.minecraft.util.Mth.clamp(mc.player.getX(), box.minX, box.maxX),
            net.minecraft.util.Mth.clamp(mc.player.getY(), box.minY, box.maxY),
            net.minecraft.util.Mth.clamp(mc.player.getZ(), box.minZ, box.maxZ));
        if (distSq > range.get() * range.get()) return false;

        // 视野角检查
        if (fov.get() < 360.0 && getAngleTo(entity) > fov.get() / 2.0) return false;

        // 类型过滤
        if (entity instanceof Player player) {
            if (!players.get()) return false;
            if (player.isCreative()) return false;
            if (shieldMode.get().equals("忽略") && player.isBlocking()) return false;
        } else if (entity instanceof Monster) {
            if (!mobs.get()) return false;
        } else if (entity instanceof Animal) {
            if (!animals.get()) return false;
        } else {
            return false;
        }

        // 穿墙检查
        if (!canSee(entity) && mc.player.distanceToSqr(entity) > wallsRange.get() * wallsRange.get()) return false;

        // 杂项过滤
        if (ignoreNamed.get() && entity.hasCustomName()) return false;

        return true;
    }

    private boolean canSee(LivingEntity target) {
        Vec3 eye = mc.player.getEyePosition();
        Vec3 center = target.getBoundingBox().getCenter();
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, center,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }

    private double getAngleTo(Entity entity) {
        Vec3 center = entity.getBoundingBox().getCenter();
        double dx = center.x - mc.player.getX();
        double dy = center.y - mc.player.getEyeY();
        double dz = center.z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        float yawDiff = Math.abs(yaw - mc.player.getYRot());
        float pitchDiff = Math.abs(pitch - mc.player.getXRot());
        yawDiff = Math.min(yawDiff, 360 - yawDiff);
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }

    private boolean delayCheck() {
        if (switchTimer > 0) { switchTimer--; return false; }

        if (customDelay.get()) {
            if (hitTimer < hitDelay.get()) { hitTimer++; return false; }
            return true;
        }

        return mc.player.getAttackStrengthScale(0.5f) >= 1.0f;
    }

    private void attack(LivingEntity target) {
        if (rotation.get().equals("攻击时")) lookAt(target);
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        hitTimer = 0;
    }

    private void stopAttacking() {
        if (swapBack.get() && swapped && previousSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selected = previousSlot;
        }
        swapped = false;
        previousSlot = -1;
    }

    private void lookAt(LivingEntity target) {
        Vec3 center = target.getBoundingBox().getCenter();
        double dx = center.x - mc.player.getX();
        double dy = center.y - mc.player.getEyeY();
        double dz = center.z - mc.player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }

    private int findWeapon(LivingEntity target) {
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (acceptableWeapon(stack)) {
                // 盾牌破坏模式优先斧
                if (shouldShieldBreak(target) && stack.getItem() instanceof AxeItem) return i;
                if (bestSlot == -1) bestSlot = i;
            }
        }
        return bestSlot;
    }

    private boolean shouldShieldBreak(LivingEntity target) {
        return target instanceof Player player && player.isBlocking() && shieldMode.get().equals("破坏");
    }

    private boolean acceptableWeapon(ItemStack stack) {
        if (weapon.get().equals("全部")) return true;
        if (weapon.get().equals("仅剑")) return stack.getItem() instanceof SwordItem;
        if (weapon.get().equals("仅斧")) return stack.getItem() instanceof AxeItem;
        return true;
    }
}
