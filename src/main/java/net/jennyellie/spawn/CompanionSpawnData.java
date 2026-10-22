package net.jennyellie.spawn;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Guarda, por mundo (no armazenamento da dimensão principal), qual Jenny e qual Ellie
 * ocupam a "vaga" de spawn natural. Enquanto a vaga está ocupada, não nasce outra.
 * A vaga é liberada quando a entidade correspondente morre.
 */
public class CompanionSpawnData extends SavedData {

    private static final String NAME = "jennyellie_spawns";

    @Nullable
    private UUID jennyId;
    @Nullable
    private UUID ellieId;

    public static CompanionSpawnData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(CompanionSpawnData::load, CompanionSpawnData::new, NAME);
    }

    public static CompanionSpawnData load(CompoundTag tag) {
        CompanionSpawnData data = new CompanionSpawnData();
        if (tag.hasUUID("Jenny")) {
            data.jennyId = tag.getUUID("Jenny");
        }
        if (tag.hasUUID("Ellie")) {
            data.ellieId = tag.getUUID("Ellie");
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (jennyId != null) {
            tag.putUUID("Jenny", jennyId);
        }
        if (ellieId != null) {
            tag.putUUID("Ellie", ellieId);
        }
        return tag;
    }

    public boolean hasJenny() {
        return jennyId != null;
    }

    public boolean hasEllie() {
        return ellieId != null;
    }

    @Nullable
    public UUID getJennyId() {
        return jennyId;
    }

    @Nullable
    public UUID getEllieId() {
        return ellieId;
    }

    public void setJenny(UUID id) {
        this.jennyId = id;
        setDirty();
    }

    public void setEllie(UUID id) {
        this.ellieId = id;
        setDirty();
    }

    public void clearJenny() {
        this.jennyId = null;
        setDirty();
    }

    public void clearEllie() {
        this.ellieId = null;
        setDirty();
    }
}
