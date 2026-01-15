package mc.basalt.client.system.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mc.basalt.client.event.EventBus;
import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.KeyEvent;
import mc.basalt.client.system.modules.combat.AutoArmor;
import mc.basalt.client.system.modules.combat.AutoCrystal;
import mc.basalt.client.system.modules.combat.AutoTotem;
import mc.basalt.client.system.modules.combat.Criticals;
import mc.basalt.client.system.modules.combat.Killaura;
import mc.basalt.client.system.modules.combat.Velocity;
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
import mc.basalt.client.system.modules.player.Blink;
import mc.basalt.client.system.modules.player.ChestStealer;
import mc.basalt.client.system.modules.player.FastPlace;
import mc.basalt.client.system.modules.render.ESP;
import mc.basalt.client.system.modules.render.Freecam;
import mc.basalt.client.system.modules.render.Fullbright;
import mc.basalt.client.system.modules.render.Nametags;
import mc.basalt.client.system.modules.render.Tracers;
import mc.basalt.client.system.modules.render.XRay;
import org.lwjgl.glfw.GLFW;

public class Modules {
    private static final Modules INSTANCE = new Modules();
    private final List<Module> modules = new ArrayList<>();

    public static Modules get() {
        return INSTANCE;
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

        // Movement
        add(new Speed());
        add(new NoFall());
        add(new Step());
        add(new Jesus());
        add(new Scaffold());
        add(new SafeWalk());
        add(new EntityControl());

        // Misc
        add(new Timer());

        // Render
        add(new ESP());
        add(new Tracers());
        add(new Nametags());
        add(new Freecam());
        add(new XRay());

        // Player
        add(new FastPlace());
        add(new ChestStealer());
        add(new Blink());

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
        if (event.getAction() == GLFW.GLFW_PRESS) {
            for (Module module : modules) {
                if (module.getKey() == event.getKey()) {
                    module.toggle();
                }
            }
        }
    }
}
