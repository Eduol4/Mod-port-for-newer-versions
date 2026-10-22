package net.jennyellie.registry;

import net.jennyellie.JennyEllieMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JennyEllieMod.MODID);

    public static final RegistryObject<SoundEvent> JENNY_GIGGLE = reg("jenny.giggle");
    public static final RegistryObject<SoundEvent> JENNY_HAPPYOH = reg("jenny.happyoh");
    public static final RegistryObject<SoundEvent> JENNY_SIGH = reg("jenny.sigh");
    public static final RegistryObject<SoundEvent> JENNY_SADOH = reg("jenny.sadoh");

    public static final RegistryObject<SoundEvent> ELLIE_GIGGLE = reg("ellie.giggle");
    public static final RegistryObject<SoundEvent> ELLIE_HAPPYOH = reg("ellie.happyoh");
    public static final RegistryObject<SoundEvent> ELLIE_SIGH = reg("ellie.sigh");
    public static final RegistryObject<SoundEvent> ELLIE_SADOH = reg("ellie.sadoh");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(JennyEllieMod.MODID, name)));
    }
}
