package mc.basalt.client.system.modules.combat;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.PacketEvent;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.BooleanSetting;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.system.settings.ModeSetting;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

public class Velocity extends Module {
    private final ModeSetting mode = new ModeSetting("模式", "百分比", "百分比", "取消", "反转");

    private final BooleanSetting knockback = new BooleanSetting("修改击退", true);
    private final DoubleSetting kbH = new DoubleSetting("击退水平%", 0.0, 0.0, 100.0, 5.0);
    private final DoubleSetting kbV = new DoubleSetting("击退垂直%", 0.0, 0.0, 100.0, 5.0);
    private final BooleanSetting explosions = new BooleanSetting("修改爆炸", true);
    private final DoubleSetting exH = new DoubleSetting("爆炸水平%", 0.0, 0.0, 100.0, 5.0);
    private final DoubleSetting exV = new DoubleSetting("爆炸垂直%", 0.0, 0.0, 100.0, 5.0);
    private final BooleanSetting liquids = new BooleanSetting("修改液体推力", false);
    private final DoubleSetting liH = new DoubleSetting("液体水平%", 0.0, 0.0, 100.0, 5.0);
    private final BooleanSetting entityPush = new BooleanSetting("修改实体推力", false);
    private final DoubleSetting epA = new DoubleSetting("实体推力%", 0.0, 0.0, 100.0, 5.0);
    private final BooleanSetting sinking = new BooleanSetting("防下沉", false);
    private final BooleanSetting fishing = new BooleanSetting("防钓鱼拉扯", false);

    public Velocity() {
        super("Velocity", "反击退", "自定义击退/爆炸/液体/实体推力，支持多种模式。", Category.Combat);
        addSetting(mode);
        addSetting(knockback); addSetting(kbH); addSetting(kbV);
        addSetting(explosions); addSetting(exH); addSetting(exV);
        addSetting(liquids); addSetting(liH);
        addSetting(entityPush); addSetting(epA);
        addSetting(sinking); addSetting(fishing);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (knockback.get() && event.getPacket() instanceof ClientboundSetEntityMotionPacket packet) {
            if (packet.getId() != mc.player.getId()) return;

            switch (mode.get()) {
                case "取消" -> event.cancel();
                case "百分比" -> {
                    event.cancel();
                    double vx = packet.getXa() * kbH.get() * 0.01;
                    double vy = packet.getYa() * kbV.get() * 0.01;
                    double vz = packet.getZa() * kbH.get() * 0.01;
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(vx, vy, vz));
                }
                case "反转" -> {
                    event.cancel();
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(
                        -packet.getXa(), -packet.getYa(), -packet.getZa()));
                }
            }
        }

        if (explosions.get() && event.getPacket() instanceof ClientboundExplodePacket explodePacket) {
            switch (mode.get()) {
                case "取消" -> event.cancel();
                case "百分比" -> {
                    var velOpt = explodePacket.playerKnockback();
                    if (velOpt.isPresent()) { Vec3 vel = velOpt.get();
                        event.cancel();
                        mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(
                            vel.x * exH.get() * 0.01, vel.y * exV.get() * 0.01, vel.z * exH.get() * 0.01));
                    }
                }
                case "反转" -> event.cancel();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (sinking.get() && !mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()) {
            if ((mc.player.isInWater() || mc.player.isInLava()) && mc.player.getDeltaMovement().y < 0) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
            }
        }
    }

    public double getLiquidHorizontal() {
        return liquids.get() ? liH.get() * 0.01 : 1.0;
    }

    public double getEntityPush() {
        return entityPush.get() ? epA.get() * 0.01 : 1.0;
    }
}
