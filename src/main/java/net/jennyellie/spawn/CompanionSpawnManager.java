package net.jennyellie.spawn;

import java.util.List;
import java.util.UUID;
import net.jennyellie.Config;
import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.CompanionEntity;
import net.jennyellie.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Spawn natural de Jenny/Ellie perto de aldeões, com limite de 1 de cada por mundo.
 * A vaga só libera quando a correspondente morre. Não afeta spawn por ovo/comando.
 */
@Mod.EventBusSubscriber(modid = JennyEllieMod.MODID)
public class CompanionSpawnManager {

    private static final int CHECK_INTERVAL = 200; // ~10s
    private static int counter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Config.NATURAL_SPAWN_ENABLED.get()) {
            return;
        }
        if (++counter < CHECK_INTERVAL) {
            return;
        }
        counter = 0;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        CompanionSpawnData data = CompanionSpawnData.get(server);
        boolean needJenny = !data.hasJenny();
        boolean needEllie = !data.hasEllie();
        if (!needJenny && !needEllie) {
            return;
        }

        int radius = Config.SPAWN_SEARCH_RADIUS.get();
        double chance = Config.SPAWN_CHANCE.get();

        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                AABB box = player.getBoundingBox().inflate(radius);
                List<Villager> villagers = level.getEntitiesOfClass(Villager.class, box);
                if (villagers.isEmpty() || level.random.nextDouble() > chance) {
                    continue;
                }

                boolean spawnJenny;
                if (needJenny && needEllie) {
                    spawnJenny = level.random.nextBoolean();
                } else {
                    spawnJenny = needJenny;
                }
                EntityType<? extends CompanionEntity> type =
                        spawnJenny ? ModEntities.JENNY.get() : ModEntities.ELLIE.get();

                Villager villager = villagers.get(level.random.nextInt(villagers.size()));
                BlockPos pos = findSpawnPos(level, villager.blockPosition());
                if (pos == null) {
                    continue;
                }

                CompanionEntity mob = type.create(level);
                if (mob == null) {
                    continue;
                }
                mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                        level.random.nextFloat() * 360.0F, 0.0F);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
                level.addFreshEntity(mob);

                if (spawnJenny) {
                    data.setJenny(mob.getUUID());
                } else {
                    data.setEllie(mob.getUUID());
                }
                return; // no máximo um spawn por verificação
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof CompanionEntity companion)) {
            return;
        }
        MinecraftServer server = companion.getServer();
        if (server == null) {
            return;
        }
        CompanionSpawnData data = CompanionSpawnData.get(server);
        UUID id = companion.getUUID();
        if (id.equals(data.getJennyId())) {
            data.clearJenny();
        }
        if (id.equals(data.getEllieId())) {
            data.clearEllie();
        }
    }

    /** Procura um ponto válido na superfície perto da origem (aldeão). */
    private static BlockPos findSpawnPos(ServerLevel level, BlockPos origin) {
        for (int i = 0; i < 12; i++) {
            int dx = level.random.nextInt(13) - 6;
            int dz = level.random.nextInt(13) - 6;
            BlockPos surface = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.offset(dx, 0, dz));
            BlockPos below = surface.below();
            if (level.getBlockState(below).blocksMotion()
                    && level.getBlockState(surface).getCollisionShape(level, surface).isEmpty()
                    && level.getBlockState(surface.above()).getCollisionShape(level, surface.above()).isEmpty()
                    && level.getFluidState(surface).isEmpty()) {
                return surface;
            }
        }
        return null;
    }
}
