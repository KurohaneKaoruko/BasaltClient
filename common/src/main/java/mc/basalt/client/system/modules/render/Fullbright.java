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

public class Fullbright extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "Gamma", "Gamma", "药水", "Luminance");

    private Double originalGamma;

    public Fullbright() {
        super("Fullbright", "夜视", "无限夜视，三种模式。", Category.Render);
        addSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null) {
            originalGamma = mc.options.gamma().get();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        switch (mode.get()) {
            case "Gamma" -> {
                if (mc.options != null && originalGamma != null) {
                    mc.options.gamma().set(originalGamma);
                }
            }
            case "药水" -> {
                if (mc.player != null) {
                    mc.player.removeEffectNoUpdate(MobEffects.NIGHT_VISION);
                }
            }
            case "Luminance" -> {
                if (mc.levelRenderer != null) {
                    mc.levelRenderer.allChanged();
                }
            }
        }
        originalGamma = null;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        switch (mode.get()) {
            case "Gamma" -> {
                if (mc.options != null) {
                    mc.options.gamma().set(1.0);
                }
            }
            case "药水" -> {
                Holder<net.minecraft.world.effect.MobEffect> nv =
                        MobEffects.NIGHT_VISION;
                MobEffectInstance inst = mc.player.getEffect(nv);
                if (inst != null && inst.getDuration() < 420) {
                    inst.update(new MobEffectInstance(MobEffects.NIGHT_VISION, 420, 0, true, false));
                } else if (inst == null) {
                    mc.player.addEffect(new MobEffectInstance(nv, 420, 0, true, false));
                }
            }
            case "Luminance" -> {
                BasaltClient.FULLBRIGHT_ENABLED = true;
            }
        }
    }
}
