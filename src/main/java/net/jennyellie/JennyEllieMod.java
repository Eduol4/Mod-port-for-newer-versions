package net.jennyellie;

import com.mojang.logging.LogUtils;
import net.jennyellie.network.ModNetwork;
import net.jennyellie.registry.ModCreativeTabs;
import net.jennyellie.registry.ModEntities;
import net.jennyellie.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JennyEllieMod.MODID)
public class JennyEllieMod {
    public static final String MODID = "jennyellie";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JennyEllieMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::commonSetup);

        LOGGER.info("Jenny & Ellie Companions (SFW) carregado.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
