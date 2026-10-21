package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.EllieEntity;
import net.jennyellie.entity.JennyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JennyEllieMod.MODID);

    public static final RegistryObject<EntityType<JennyEntity>> JENNY = ENTITY_TYPES.register("jenny",
            () -> EntityType.Builder.of(JennyEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.85f)
                    .clientTrackingRange(10)
                    .build("jenny"));

    public static final RegistryObject<EntityType<EllieEntity>> ELLIE = ENTITY_TYPES.register("ellie",
            () -> EntityType.Builder.of(EllieEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.85f)
                    .clientTrackingRange(10)
                    .build("ellie"));
}
