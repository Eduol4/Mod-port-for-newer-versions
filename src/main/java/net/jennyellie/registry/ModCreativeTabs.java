package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JennyEllieMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.jennyellie.tab"))
                    .icon(() -> new ItemStack(ModItems.JENNY_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.JENNY_SPAWN_EGG.get());
                        output.accept(ModItems.ELLIE_SPAWN_EGG.get());
                    })
                    .build());
}
