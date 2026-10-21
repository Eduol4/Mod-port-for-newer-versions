package net.jennyellie.client;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.client.renderer.EllieRenderer;
import net.jennyellie.client.renderer.JennyRenderer;
import net.jennyellie.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JennyEllieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.JENNY.get(), JennyRenderer::new);
        event.registerEntityRenderer(ModEntities.ELLIE.get(), EllieRenderer::new);
    }
}
