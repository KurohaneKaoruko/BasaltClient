package mc.basalt.client.system.modules.render;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class Fullbright extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Gamma", "Gamma", "药水", "Luminance");

    public Fullbright() {
        super("Fullbright", "夜视", "无限夜视效果，支持三种模式。", Category.Render);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        applyEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        applyDisable();
    }

    private void applyEnable() {
        switch (mode.get()) {
            case "Gamma" -> {
                // gamma 拉满
                if (mc.options != null) {
                    mc.options.gamma().set(1.0);
                }
            }
            case "药水" -> {
                // 不需要额外初始化，tick 中持续刷新
            }
            case "Luminance" -> {
                // 触发区块重建使新光照生效
                if (mc.levelRenderer != null) {
                    mc.levelRenderer.allChanged();
                }
            }
        }
    }

    private void applyDisable() {
        switch (mode.get()) {
            case "Gamma" -> {
                if (mc.options != null) {
                    mc.options.gamma().set(1.0); // 恢复原版默认
                }
            }
            case "药水" -> {
                if (mc.player != null) {
                    mc.player.removeEffect(MobEffects.NIGHT_VISION.value());
                }
            }
            case "Luminance" -> {
                if (mc.levelRenderer != null) {
                    mc.levelRenderer.allChanged();
                }
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        switch (mode.get()) {
            case "Gamma" -> {
                // 持续应用，防止重置
                if (mc.options != null) {
                    mc.options.gamma().set(1.0);
                }
            }
            case "药水" -> {
                Holder<net.minecraft.world.effect.MobEffect> nv =
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value());
                MobEffectInstance inst = mc.player.getEffect(nv);
                if (inst != null && inst.getDuration() < 420) {
                    inst.setDuration(420);
                } else if (inst == null) {
                    mc.player.addEffect(new MobEffectInstance(nv, 420, 0, true, false));
                }
            }
            case "Luminance" -> {
                // 光照值由 MixinLevelRenderer 的 getLightColor 拦截处理
            }
        }
    }
}
