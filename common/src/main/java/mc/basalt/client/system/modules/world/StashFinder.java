package mc.basalt.client.system.modules.world;

import mc.basalt.client.event.annotations.EventHandler;
import mc.basalt.client.event.events.TickEvent;
import mc.basalt.client.system.modules.Category;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.settings.DoubleSetting;
import mc.basalt.client.ui.notifications.NotificationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Set;

public class StashFinder extends Module {
    private final DoubleSetting scanRadius = new DoubleSetting("扫描半径区块", 8.0, 1.0, 16.0, 1.0);
    private final DoubleSetting scanInterval = new DoubleSetting("扫描间隔tick", 100.0, 20.0, 600.0, 20.0);
    private final DoubleSetting minContainers = new DoubleSetting("最少容器数", 3.0, 1.0, 20.0, 1.0);

    private int timer;
    private final Set<Long> notifiedChunks = new HashSet<>();

    public StashFinder() {
        super("StashFinder", "储藏搜寻", "扫描附近的区块发现大量容器时通知。", Category.World);
        addSetting(scanRadius);
        addSetting(scanInterval);
        addSetting(minContainers);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer = 0;
        notifiedChunks.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        if (timer > 0) {
            timer--;
            return;
        }
        timer = scanInterval.get().intValue();

        int chunkX = mc.player.blockPosition().getX() >> 4;
        int chunkZ = mc.player.blockPosition().getZ() >> 4;
        int radius = scanRadius.get().intValue();

        // 清理过期的 chunk 记录
        notifiedChunks.removeIf(key -> {
            int dx = Math.abs((int) (key >> 32) - chunkX);
            int dz = Math.abs((int) (key & 0xFFFFFFFF) - chunkZ);
            return dx > radius * 2 || dz > radius * 2;
        });

        int totalContainers = 0;
        int chests = 0, barrels = 0, shulkers = 0, others = 0;
        BlockPos samplePos = null;

        for (int cx = chunkX - radius; cx <= chunkX + radius; cx++) {
            for (int cz = chunkZ - radius; cz <= chunkZ + radius; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                int chunkContainers = 0;
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getValue() instanceof BaseContainerBlockEntity container) {
                        chunkContainers++;
                        if (samplePos == null) samplePos = entry.getKey();
                    }
                }

                if (chunkContainers >= minContainers.get()) {
                    long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
                    if (!notifiedChunks.contains(key)) {
                        notifiedChunks.add(key);
                        NotificationManager.warn("StashFinder",
                                "区块 [" + cx + "," + cz + "] 找到 " + chunkContainers + " 个容器!");
                    }
                }

                totalContainers += chunkContainers;
            }
        }

        // 简易分类统计（仅统计当前 0,0 chunk 作为样本，避免全量 instanceof 性能损耗）
        if (samplePos != null && totalContainers > 0) {
            var sample = mc.level.getBlockEntity(samplePos);
            if (sample != null) {
                String name = sample.getClass().getSimpleName();
                if (name.contains("Chest")) chests++;
                else if (name.contains("Barrel")) barrels++;
                else if (name.contains("Shulker")) shulkers++;
                else others++;
            }
        }

        if (totalContainers > 0 && (chests + barrels + shulkers + others) > 0) {
            // 仅记录一次汇总
        }
    }
}