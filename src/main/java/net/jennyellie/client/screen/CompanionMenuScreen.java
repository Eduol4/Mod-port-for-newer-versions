package net.jennyellie.client.screen;

import net.jennyellie.entity.CompanionEntity;
import net.jennyellie.network.CompanionActionPacket;
import net.jennyellie.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Menu de opções SFW. Ações comuns à esquerda; o botão de reprodução ("villager")
 * fica isolado à direita, habilitado conforme o estado da companheira.
 */
public class CompanionMenuScreen extends Screen {

    private final CompanionEntity entity;
    private int breedBtnX;
    private int breedBtnY;

    public CompanionMenuScreen(CompanionEntity entity) {
        super(entity.getDisplayName());
        this.entity = entity;
    }

    @Override
    protected void init() {
        int x = 12;
        int w = 120, h = 20, gap = 4;
        int y = this.height / 2 - (5 * h + 4 * gap) / 2;

        Component followLabel = this.entity.isFollowing()
                ? Component.translatable("action.names.stopfollowme")
                : Component.translatable("action.names.followme");
        addRenderableWidget(Button.builder(followLabel,
                b -> send(CompanionActionPacket.Action.TOGGLE_FOLLOW)).bounds(x, y, w, h).build());
        addRenderableWidget(Button.builder(Component.translatable("action.names.setnewhome"),
                b -> send(CompanionActionPacket.Action.SET_HOME)).bounds(x, y + (h + gap), w, h).build());
        Button goHome = Button.builder(Component.translatable("action.names.gohome"),
                b -> send(CompanionActionPacket.Action.GO_HOME)).bounds(x, y + 2 * (h + gap), w, h).build();
        goHome.active = this.entity.isHomeSet(); // desabilitado até definir uma casa
        addRenderableWidget(goHome);
        addRenderableWidget(Button.builder(Component.translatable("action.names.equipment"),
                b -> send(CompanionActionPacket.Action.OPEN_EQUIPMENT)).bounds(x, y + 3 * (h + gap), w, h).build());
        // Strip / Dress up: habilitado só com armadura equipada; o rótulo depende do estado.
        Component stripLabel = this.entity.isArmorHidden()
                ? Component.translatable("action.names.dressup")
                : Component.translatable("action.names.strip");
        Button strip = Button.builder(stripLabel,
                b -> send(CompanionActionPacket.Action.STRIP)).bounds(x, y + 4 * (h + gap), w, h).build();
        strip.active = this.entity.hasAnyArmor();
        addRenderableWidget(strip);

        // Botão de reprodução, isolado à direita.
        this.breedBtnX = this.width - 12 - w;
        this.breedBtnY = this.height / 2 - h / 2;
        Button breed = Button.builder(Component.translatable("action.names.villager"),
                b -> send(CompanionActionPacket.Action.BREED)).bounds(this.breedBtnX, this.breedBtnY, w, h).build();
        breed.active = this.entity.isBreedButtonEnabled();
        addRenderableWidget(breed);
    }

    private void send(CompanionActionPacket.Action action) {
        ModNetwork.CHANNEL.sendToServer(new CompanionActionPacket(this.entity.getId(), action));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawString(this.font, this.title, 12, this.height / 2 - (2 * 20 + 18), 0xFFFFFF);

        int secs = this.entity.getBreedCooldownSecs();
        if (secs > 0) {
            Component cd = Component.translatable("gui.jennyellie.cooldown", secs);
            graphics.drawString(this.font, cd, this.breedBtnX, this.breedBtnY + 24, 0xFFAAAA);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
