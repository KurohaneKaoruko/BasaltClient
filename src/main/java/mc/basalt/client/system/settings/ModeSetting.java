package mc.basalt.client.system.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String[] modes, Supplier<Boolean> visible, Consumer<String> onChanged) {
        super(name, defaultMode, visible, onChanged);
        this.modes = modes;
        
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(defaultMode)) {
                this.index = i;
                break;
            }
        }
    }
    
    public ModeSetting(String name, String defaultMode, String... modes) {
        this(name, defaultMode, modes, null, null);
    }

    public void cycle() {
        index = (index + 1) % modes.length;
        set(modes[index]);
    }
    
    @Override
    public void set(String newValue) {
        super.set(newValue);
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(newValue)) {
                this.index = i;
                break;
            }
        }
    }
}
