package com.maxwell.spotmod.server;

import com.maxwell.spotmod.client.S2CDamageIndicatorPacket;
import com.maxwell.spotmod.misc.config.ModConfig;
import com.maxwell.spotmod.misc.PacketHandler;
import com.maxwell.spotmod.SpotMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = SpotMod.MODID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ServerSpottedManager.tick();
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) attacker = event.getSource().getDirectEntity();
        if (attacker == null) return;
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CDamageIndicatorPacket(attacker.position()));
    }
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean allows = ModConfig.Server.ENABLE_ALL_INDICATORS.get();
            int duration = ModConfig.Server.DAMAGE_INDICATOR_DURATION_TICKS.get();
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new S2CSyncConfigPacket(allows, duration));
        }
    }
}