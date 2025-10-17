package com.Maxwell.spotmod.Client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSpottedManager {
    private static final ConcurrentHashMap<UUID, Long> SPOTTED_ENTITIES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> PREDICTED_SPOTS = new ConcurrentHashMap<>();
    private static final int PREDICTION_LIFESPAN_TICKS = 20; // 予測が有効な最大時間 (1秒)
    public static void updateSpottedEntity(UUID entityUUID, int durationTicks) {
        if (durationTicks > 0) {
            long expirationTime = Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() + durationTicks;
            SPOTTED_ENTITIES.put(entityUUID, expirationTime);
        } else {
            SPOTTED_ENTITIES.remove(entityUUID);
        }
    }
    public static void predictSpot(LivingEntity entity) {
        if (entity == null) return;
        long expirationTime = Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() + PREDICTION_LIFESPAN_TICKS;
        PREDICTED_SPOTS.put(entity.getUUID(), expirationTime);
    }
    public static boolean isSpotted(UUID entityUUID) {
        if (SPOTTED_ENTITIES.containsKey(entityUUID)) {
            long expirationTime = SPOTTED_ENTITIES.get(entityUUID);
            if (Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() > expirationTime) {
                SPOTTED_ENTITIES.remove(entityUUID);
                return false;
            }
            PREDICTED_SPOTS.remove(entityUUID);
            return true;
        }
        if (PREDICTED_SPOTS.containsKey(entityUUID)) {
            long expirationTime = PREDICTED_SPOTS.get(entityUUID);
            if (Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() > expirationTime) {
                PREDICTED_SPOTS.remove(entityUUID);
                return false;
            }
            return true;
        }

        return false;
    }
}