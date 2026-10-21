package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JennyEllieMod.MODID);

    // Cores herdadas do registro original (primary, secondary)
    public static final RegistryObject<Item> JENNY_SPAWN_EGG = ITEMS.register("jenny_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.JENNY, 0x322000, 0xC11D45, new Item.Properties()));

    public static final RegistryObject<Item> ELLIE_SPAWN_EGG = ITEMS.register("ellie_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ELLIE, 0x161616, 0x980000, new Item.Properties()));
}
