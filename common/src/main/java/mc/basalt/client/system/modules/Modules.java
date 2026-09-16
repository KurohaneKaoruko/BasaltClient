package mc.basalt.client.system.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.KeyEvent;
import mc.basalt.client.system.modules.combat.AutoArmor;
import mc.basalt.client.system.modules.combat.AutoClicker;
import mc.basalt.client.system.modules.combat.AutoCrystal;
import mc.basalt.client.system.modules.combat.AutoTotem;
import mc.basalt.client.system.modules.combat.Criticals;
import mc.basalt.client.system.modules.combat.Killaura;
import mc.basalt.client.system.modules.combat.TriggerBot;
import mc.basalt.client.system.modules.combat.Velocity;
import mc.basalt.client.system.modules.misc.AntiAFK;
import mc.basalt.client.system.modules.misc.Timer;
import mc.basalt.client.system.modules.movement.EntityControl;
import mc.basalt.client.system.modules.movement.Flight;
import mc.basalt.client.system.modules.movement.Jesus;
import mc.basalt.client.system.modules.movement.NoFall;
import mc.basalt.client.system.modules.movement.SafeWalk;
import mc.basalt.client.system.modules.movement.Scaffold;
import mc.basalt.client.system.modules.movement.Speed;
import mc.basalt.client.system.modules.movement.Sprint;
import mc.basalt.client.system.modules.movement.Step;
import mc.basalt.client.system.modules.player.AutoEat;
import mc.basalt.client.system.modules.player.AutoRespawn;
import mc.basalt.client.system.modules.player.AutoTool;
import mc.basalt.client.system.modules.player.Blink;
import mc.basalt.client.system.modules.player.ChestStealer;
import mc.basalt.client.system.modules.player.FastPlace;
import mc.basalt.client.system.modules.render.ESP;
import mc.basalt.client.system.modules.render.Freecam;
import mc.basalt.client.system.modules.render.Fullbright;
import mc.basalt.client.system.modules.render.Nametags;
import mc.basalt.client.system.modules.render.NoWeather;
import mc.basalt.client.system.modules.render.Tracers;
import mc.basalt.client.system.modules.render.Zoom;
import mc.basalt.client.system.modules.render.XRay;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Modules {
    private static final Modules INSTANCE = new Modules();
    private final List<Module> modules = new ArrayList<>();
    private boolean initialized;

    public static Modules get() {
        return INSTANCE;
    }

    /** 模块列表是否已完成初始化（mixin 回调可能早于 init 调用）。 */
    public static boolean initialized() {
        return INSTANCE.initialized;
    }

    public void init() {
        // Register modules here
        add(new Flight());
        add(new Sprint());
        add(new Fullbright());

        // Combat
        add(new Killaura());
        add(new Velocity());
        add(new AutoTotem());
        add(new Criticals());
        add(new AutoArmor());
        add(new AutoCrystal());
        add(new AutoClicker());
        add(new TriggerBot());
        add(new mc.basalt.client.system.modules.combat.AimAssist());
        add(new mc.basalt.client.system.modules.combat.HitBoxes());
        add(new mc.basalt.client.system.modules.combat.Reach());

        // Movement
        add(new Speed());
        add(new NoFall());
        add(new Step());
        add(new Jesus());
        add(new Scaffold());
        add(new SafeWalk());
        add(new EntityControl());
        add(new mc.basalt.client.system.modules.movement.LongJump());
        add(new mc.basalt.client.system.modules.movement.NoSlow());

        // Misc
        add(new Timer());
        add(new AntiAFK());

        // Render
        add(new ESP());
        add(new Tracers());
        add(new Nametags());
        add(new Freecam());
        add(new XRay());
        add(new Zoom());
        add(new NoWeather());
        add(new mc.basalt.client.system.modules.render.NoRender());

        // Player
        add(new FastPlace());
        add(new ChestStealer());
        add(new Blink());
        add(new AutoEat());
        add(new AutoRespawn());
        add(new AutoTool());
        add(new mc.basalt.client.system.modules.player.AutoMine());
        add(new mc.basalt.client.system.modules.player.AntiHunger());

        // World
        add(new mc.basalt.client.system.modules.world.AutoShear());
        add(new mc.basalt.client.system.modules.world.AutoDoor());
        add(new mc.basalt.client.system.modules.world.AutoMount());
        add(new mc.basalt.client.system.modules.world.AutoNametag());
        add(new mc.basalt.client.system.modules.world.Nuker());
        add(new mc.basalt.client.system.modules.world.LiquidFiller());
        add(new mc.basalt.client.system.modules.world.BonemealAura());
        add(new mc.basalt.client.system.modules.world.AutoFish());
        add(new mc.basalt.client.system.modules.world.StashFinder());
        add(new mc.basalt.client.system.modules.world.AutoReplant());

        initialized = true;
        EventBus.subscribe(this);
    }

    public void add(Module module) {
        modules.add(module);
    }

    public List<Module> getAll() {
        return modules;
    }

    public List<Module> getByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        // 有界面打开时（聊天、GUI）不触发模块按键，避免打字误触发
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS) {
            for (Module module : modules) {
                if (module.getKey() == event.getKey()) {
                    module.toggle();
                }
            }
        }
    }
}
