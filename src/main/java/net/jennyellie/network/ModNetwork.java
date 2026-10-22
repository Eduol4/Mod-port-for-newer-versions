package net.jennyellie.network;

import net.jennyellie.JennyEllieMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String VERSION = "1";

    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(JennyEllieMod.MODID, "main"),
                () -> VERSION, VERSION::equals, VERSION::equals);

        CHANNEL.registerMessage(0, CompanionActionPacket.class,
                CompanionActionPacket::encode,
                CompanionActionPacket::decode,
                CompanionActionPacket::handle);
    }
}
