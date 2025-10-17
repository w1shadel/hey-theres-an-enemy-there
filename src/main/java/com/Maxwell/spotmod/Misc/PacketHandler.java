package com.Maxwell.spotmod.Misc;

import com.Maxwell.spotmod.Client.S2CDamageIndicatorPacket;
import com.Maxwell.spotmod.Client.S2CSpotEntityPacket;
import com.Maxwell.spotmod.Server.C2SSpotKeyPressedPacket;
import com.Maxwell.spotmod.SpotMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Predicate;
@SuppressWarnings("removal")
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static boolean isRegistered = false; // ★★★ この行を追加 ★★★
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SpotMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            Predicate.isEqual(PROTOCOL_VERSION),
            Predicate.isEqual(PROTOCOL_VERSION)
    );

    public static void register() {
        if (isRegistered) return;
        int id = 0;
        INSTANCE.registerMessage(id++, C2SSpotKeyPressedPacket.class, C2SSpotKeyPressedPacket::toBytes, C2SSpotKeyPressedPacket::new, C2SSpotKeyPressedPacket::handle);
        INSTANCE.registerMessage(id++, S2CSpotEntityPacket.class, S2CSpotEntityPacket::toBytes, S2CSpotEntityPacket::new, S2CSpotEntityPacket::handle);
        INSTANCE.registerMessage(id++, S2CDamageIndicatorPacket.class, S2CDamageIndicatorPacket::encode, S2CDamageIndicatorPacket::decode, S2CDamageIndicatorPacket::handle);
      isRegistered = true;
    }
}