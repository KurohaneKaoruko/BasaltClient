package mc.basalt.client.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.class)
public interface MixinOptionInstance {
    @Accessor("value")
    <T> T basalt$getValueRaw();

    @Accessor("value")
    <T> void basalt$setValueRaw(T value);
}
