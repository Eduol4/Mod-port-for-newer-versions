package net.jennyellie.client.screen;

import net.jennyellie.entity.CompanionEntity;
import net.jennyellie.network.CompanionActionPacket;
import net.jennyellie.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Menu de opções SFW, no lado esquerdo da tela (como no mod original).
 * As ações NSFW que ficavam à direita foram removidas.
 */
public class CompanionMenuScreen extends Screen {

    private final CompanionEntity entity;

    public CompanionMenuScreen(CompanionEntity entity) {
        super(entity.getDisplayName());
        this.entity = entity;
    }

    @Override
    protected void init() {
        int x = 12;
        int w = 120, h = 20, gap = 4;
        int y = this.height / 2 - (2 * h + (int) (1.5 * gap));

        addRenderableWidget(Button.builder(Component.translatable("action.names.followme"),
                b -> send(CompanionActionPacket.Action.FOLLOW)).bounds(x, y, w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("action.names.stopfollowme"),
                b -> send(CompanionActionPacket.Action.STOP)).bounds(x, y + (h + gap), w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("action.names.setnewhome"),
                b -> send(CompanionActionPacket.Action.SET_HOME)).bounds(x, y + 2 * (h + gap), w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("action.names.gohome"),
                b -> send(CompanionActionPacket.Action.GO_HOME)).bounds(x, y + 3 * (h + gap), w, h).build());
    }

    private void send(CompanionActionPacket.Action action) {
        ModNetwork.CHANNEL.sendToServer(new CompanionActionPacket(this.entity.getId(), action));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawString(this.font, this.title, 12, this.height / 2 - (2 * 20 + 18), 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
