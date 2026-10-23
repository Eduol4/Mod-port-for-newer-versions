package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.menu.CompanionEquipmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, JennyEllieMod.MODID);

    public static final RegistryObject<MenuType<CompanionEquipmentMenu>> EQUIPMENT =
            MENUS.register("equipment",
                    () -> IForgeMenuType.create(CompanionEquipmentMenu::fromNetwork));
}
