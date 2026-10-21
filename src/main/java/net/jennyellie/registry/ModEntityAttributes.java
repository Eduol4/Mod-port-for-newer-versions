package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.EllieEntity;
import net.jennyellie.entity.JennyEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JennyEllieMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.JENNY.get(), JennyEntity.createAttributes().build());
        event.put(ModEntities.ELLIE.get(), EllieEntity.createAttributes().build());
    }
}
