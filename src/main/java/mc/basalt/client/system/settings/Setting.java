package mc.basalt.client.system.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    private T value;
    private final T defaultValue;
    private final Supplier<Boolean> visible;
    private final Consumer<T> onChanged;

    public Setting(String name, T defaultValue, Supplier<Boolean> visible, Consumer<T> onChanged) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visible = visible;
        this.onChanged = onChanged;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        this.value = newValue;
        if (onChanged != null) {
            onChanged.accept(newValue);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visible == null || visible.get();
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
}
