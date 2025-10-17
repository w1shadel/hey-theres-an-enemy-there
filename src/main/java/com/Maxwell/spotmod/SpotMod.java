package com.Maxwell.spotmod;

import com.Maxwell.spotmod.Misc.Config.ModConfig;
import com.Maxwell.spotmod.Server.ServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(SpotMod.MODID)
public class SpotMod {
    public static final String MODID = "spotmod";

    public SpotMod() {
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, ModConfig.Server.SPEC, "spotmod-server.toml");
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ModConfig.Client.SPEC, "spotmod-client.toml");
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }
}