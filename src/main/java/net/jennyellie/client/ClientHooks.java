package net.jennyellie.client;

import net.jennyellie.client.screen.CompanionMenuScreen;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.client.Minecraft;

/** Código client-only chamado via DistExecutor a partir da entidade. */
public class ClientHooks {
    public static void openCompanionMenu(CompanionEntity entity) {
        Minecraft.getInstance().setScreen(new CompanionMenuScreen(entity));
    }
}
