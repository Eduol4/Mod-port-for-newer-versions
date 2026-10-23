package net.jennyellie.client;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.client.renderer.EllieRenderer;
import net.jennyellie.client.renderer.JennyRenderer;
import net.jennyellie.client.screen.CompanionEquipmentScreen;
import net.jennyellie.registry.ModEntities;
import net.jennyellie.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = JennyEllieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.JENNY.get(), JennyRenderer::new);
        event.registerEntityRenderer(ModEntities.ELLIE.get(), EllieRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenus.EQUIPMENT.get(), CompanionEquipmentScreen::new));
    }
}
