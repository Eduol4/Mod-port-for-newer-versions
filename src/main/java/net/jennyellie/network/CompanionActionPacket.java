package net.jennyellie.network;

import java.util.function.Supplier;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

/** Pacote cliente -> servidor com a ação escolhida no menu de opções. */
public class CompanionActionPacket {

    public enum Action { FOLLOW, STOP, SET_HOME, GO_HOME, TOGGLE_FOLLOW }

    private final int entityId;
    private final Action action;

    public CompanionActionPacket(int entityId, Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public static void encode(CompanionActionPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeEnum(msg.action);
    }

    public static CompanionActionPacket decode(FriendlyByteBuf buf) {
        return new CompanionActionPacket(buf.readVarInt(), buf.readEnum(Action.class));
    }

    public static void handle(CompanionActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            Entity e = player.level().getEntity(msg.entityId);
            if (!(e instanceof CompanionEntity companion)) {
                return;
            }
            // Evita comandar à distância / por pacotes forjados, ou antes de domesticar.
            if (!companion.isTame() || companion.distanceToSqr(player) > 12.0D * 12.0D) {
                return;
            }
            switch (msg.action) {
                case TOGGLE_FOLLOW -> {
                    if (companion.isFollowing()) {
                        companion.commandStopFollow();
                        companion.onCommandFeedback(player, Action.STOP);
                    } else {
                        companion.commandFollow(player);
                        companion.onCommandFeedback(player, Action.FOLLOW);
                    }
                }
                case FOLLOW -> {
                    companion.commandFollow(player);
                    companion.onCommandFeedback(player, Action.FOLLOW);
                }
                case STOP -> {
                    companion.commandStopFollow();
                    companion.onCommandFeedback(player, Action.STOP);
                }
                case SET_HOME -> {
                    companion.commandSetHome();
                    companion.onCommandFeedback(player, Action.SET_HOME);
                }
                case GO_HOME -> {
                    companion.commandGoHome();
                    companion.onCommandFeedback(player, Action.GO_HOME);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
