package com.maxwell.spotmod.server;


import com.maxwell.spotmod.misc.config.ModConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class C2SSpotKeyPressedPacket {

    public C2SSpotKeyPressedPacket() {
    }

    public C2SSpotKeyPressedPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || ServerSpottedManager.isPlayerOnCooldown(player)) {
                return;
            }

            double maxRange = ModConfig.Server.SPOT_RANGE.get();
            double fovAngle = ModConfig.Server.SPOT_FOV_ANGLE.get();
            Vec3 playerEyePos = player.getEyePosition();

            AABB searchBox = player.getBoundingBox().inflate(maxRange);
            List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchBox, (entity) ->
                    entity != player && !entity.isDeadOrDying() && player.distanceToSqr(entity) < maxRange * maxRange
            );

            if (nearbyEntities.isEmpty()) {
                return;
            }

            Optional<LivingEntity> bestTarget = nearbyEntities.stream()
                    .filter(entity -> {
                        // --- 視野角判定 ---
                        Vec3 playerLookVec = player.getViewVector(1.0F);
                        Vec3 directionToEntity = entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(playerEyePos).normalize();
                        double dotProduct = playerLookVec.dot(directionToEntity);
                        // 浮動小数点誤差対策
                        dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
                        double angleDeg = Math.toDegrees(Math.acos(dotProduct));
                        return angleDeg < fovAngle / 2.0;
                    })
                    .filter(entity -> {
                        // --- 遮蔽物判定 (レイキャスト) ---
                        Vec3 targetPos = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
                        HitResult hitResult = player.level().clip(new ClipContext(
                                playerEyePos,
                                targetPos,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                player
                        ));
                        return hitResult.getType() == HitResult.Type.MISS;
                    })
                    .min(Comparator.comparingDouble(entity -> {
                        // --- 画面中央への近さでソート ---
                        Vec3 playerLookVec = player.getViewVector(1.0F);
                        Vec3 directionToEntity = entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(playerEyePos).normalize();
                        return -playerLookVec.dot(directionToEntity); // 内積が大きいもの（角度が小さいもの）を優先
                    }));

            bestTarget.ifPresent(target -> {
                ServerSpottedManager.spotEntity(target, player);
            });
        });
    }
}