package mc.basalt.client.system.settings;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public DoubleSetting(String name, double defaultValue, double min, double max, double increment, Supplier<Boolean> visible, Consumer<Double> onChanged) {
        super(name, defaultValue, visible, onChanged);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }
    
    public DoubleSetting(String name, double defaultValue, double min, double max, double increment) {
        this(name, defaultValue, min, max, increment, null, null);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    @Override
    public void set(Double newValue) {
        double clamped = Math.max(min, Math.min(max, newValue));
        if (increment > 0) {
            clamped = Math.round(clamped / increment) * increment;
            clamped = Math.max(min, Math.min(max, clamped));
        }
        super.set(clamped);
    }
}
