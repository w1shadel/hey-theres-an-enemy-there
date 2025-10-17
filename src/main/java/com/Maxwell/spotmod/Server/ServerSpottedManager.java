package com.Maxwell.spotmod.Server;


import com.Maxwell.spotmod.Client.S2CSpotEntityPacket;
import com.Maxwell.spotmod.Misc.Config.ModConfig;
import com.Maxwell.spotmod.Misc.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class ServerSpottedManager {
    private static final ConcurrentHashMap<UUID, Integer> SPOTTED_ENTITIES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> PLAYER_COOLDOWNS = new ConcurrentHashMap<>();
    public static void spotEntity(LivingEntity entity, ServerPlayer spotter) {
        if (entity == null || entity.isDeadOrDying()) {
            return;
        }
        int durationTicks = ModConfig.Server.SPOT_DURATION_SECONDS.get() * 20;
        int cooldownTicks = ModConfig.Server.SPOT_COOLDOWN_TICKS.get();
        SPOTTED_ENTITIES.put(entity.getUUID(), durationTicks);
        PLAYER_COOLDOWNS.put(spotter.getUUID(), cooldownTicks);
        S2CSpotEntityPacket spotPacket = new S2CSpotEntityPacket(entity.getUUID(), durationTicks);
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), spotPacket);
    }
    public static void tick() {
        if (SPOTTED_ENTITIES.isEmpty() || !ModConfig.Server.SPOT_ENABLE.get()) {
            return;
        }
        SPOTTED_ENTITIES.keySet().forEach(uuid -> {
            int newDuration = SPOTTED_ENTITIES.get(uuid) - 1;
            if (newDuration <= 0) {
                SPOTTED_ENTITIES.remove(uuid);
            } else {
                SPOTTED_ENTITIES.put(uuid, newDuration);
            }
        });
        if (!PLAYER_COOLDOWNS.isEmpty()) {
            PLAYER_COOLDOWNS.keySet().forEach(uuid -> {
                int newCooldown = PLAYER_COOLDOWNS.get(uuid) - 1;
                if (newCooldown <= 0) {
                    PLAYER_COOLDOWNS.remove(uuid);
                } else {
                    PLAYER_COOLDOWNS.put(uuid, newCooldown);
                }
            });
        }
    }
    public static boolean isPlayerOnCooldown(Player player) {
        return PLAYER_COOLDOWNS.containsKey(player.getUUID());
    }

}