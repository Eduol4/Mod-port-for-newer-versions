package net.jennyellie.event;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Quando o jogador clica com o botão direito numa cama onde uma companheira está sentada,
 * ela se levanta imediatamente (liberando a cama), tal como ocorre com aldeões.
 */
@Mod.EventBusSubscriber(modid = JennyEllieMod.MODID)
public final class BedInteractHandler {

    private BedInteractHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return;
        }
        // Resolve a cabeceira (a posição que a companheira usa como referência da cama).
        BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.HEAD
                ? pos
                : pos.relative(state.getValue(BedBlock.FACING));

        AABB box = new AABB(headPos).inflate(2.0D);
        for (CompanionEntity c : level.getEntitiesOfClass(CompanionEntity.class, box)) {
            if (headPos.equals(c.getBedPos())) {
                c.standUpFromBed();
            }
        }
    }
}
