package com.Maxwell.spotmod;

import com.Maxwell.spotmod.Misc.Config.Config;
import com.Maxwell.spotmod.Server.ServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SpotMod.MODID)
public class SpotMod {
    public static final String MODID = "spotmod";

    public SpotMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.Server.SPEC, "spotmod-server.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC, "spotmod-client.toml");
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
    }
}