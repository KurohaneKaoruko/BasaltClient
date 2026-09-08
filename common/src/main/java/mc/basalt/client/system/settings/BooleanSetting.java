package mc.basalt.client.system.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue, Supplier<Boolean> visible, Consumer<Boolean> onChanged) {
        super(name, defaultValue, visible, onChanged);
    }
    
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue, null, null);
    }

    public void toggle() {
        set(!get());
    }
}
