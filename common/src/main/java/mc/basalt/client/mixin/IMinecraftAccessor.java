package mc.basalt.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IMinecraftAccessor {
    @Accessor("missTime")
    void setMissTime(int itemUseCooldown);

    @Accessor("missTime")
    int getMissTime();

    @Invoker("startAttack")
    boolean invokeStartAttack();
}
