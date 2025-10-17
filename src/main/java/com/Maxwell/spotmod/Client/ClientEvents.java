package com.Maxwell.spotmod.Client;

import com.Maxwell.spotmod.Misc.Config.Config;
import com.Maxwell.spotmod.Misc.EXRenderType;
import com.Maxwell.spotmod.Misc.Keybinds;
import com.Maxwell.spotmod.Misc.PacketHandler;
import com.Maxwell.spotmod.SpotMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import com.Maxwell.spotmod.Server.C2SSpotKeyPressedPacket;
import net.minecraft.client.Minecraft;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


@Mod.EventBusSubscriber(modid = SpotMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.SPOT_KEY);
    }
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().player != null && Keybinds.SPOT_KEY.consumeClick()) {
            PacketHandler.INSTANCE.sendToServer(new C2SSpotKeyPressedPacket());
            findBestTargetClientSide(Minecraft.getInstance().player).ifPresent(ClientSpottedManager::predictSpot);
        }
    }
    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<LivingEntity, ?> event) {
        if (ClientSpottedManager.isSpotted(event.getEntity().getUUID())) {
            renderSpotMarker(event.getEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
        }
    }
    private static class DamageIndicator {
        private final Vec3 sourcePos;
        private final float relativeAngle;
        private final long creationTime;
        public DamageIndicator(Vec3 sourcePos, float relativeAngle) {
            this.sourcePos = sourcePos;
            this.relativeAngle = relativeAngle;
            this.creationTime = System.currentTimeMillis();
        }
    }
    private static final List<DamageIndicator> activeIndicators = new CopyOnWriteArrayList<>();
    public static void addDamageIndicator(Vec3 attackerPos) {
        if (!Config.Client.ENABLE_2D_DAMAGE_INDICATOR.get() && !Config.Client.ENABLE_3D_DAMAGE_INDICATOR.get()) return;
        if (!Config.Server.ENABLE_ALL_INDICATORS.get()) return;
        if (Minecraft.getInstance().player == null) return;
        Player player = Minecraft.getInstance().player;
        float relativeAngle = calculateRelativeAngle(player, attackerPos);
        activeIndicators.add(new DamageIndicator(attackerPos, relativeAngle));
    }
    private static float calculateRelativeAngle(Player player, Vec3 attackerPos) {
        Vec3 vectorToAttacker = attackerPos.subtract(player.getEyePosition());
        float playerYaw = player.getYRot();
        float attackAngle = (float) Math.toDegrees(Mth.atan2(vectorToAttacker.z, vectorToAttacker.x)) - 90.0f;
        return Mth.wrapDegrees(attackAngle - playerYaw);
    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!Config.Client.ENABLE_3D_DAMAGE_INDICATOR.get() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || activeIndicators.isEmpty()) return;
        render3dDamageIndicators(event.getPoseStack());

    }
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            return;
        }
        if (activeIndicators.isEmpty() || !Config.Client.ENABLE_2D_DAMAGE_INDICATOR.get()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        PoseStack poseStack = event.getGuiGraphics().pose();
        long currentTime = System.currentTimeMillis();
        int duration = Config.Server.DAMAGE_INDICATOR_DURATION_TICKS.get() * 50;
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        float radius = Config.Client.DAMAGE_2D_INDICATOR_DISTANCE.get().floatValue();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        activeIndicators.removeIf(indicator -> (currentTime - indicator.creationTime) > duration);
        activeIndicators.forEach(indicator -> {
            long age = currentTime - indicator.creationTime;
            poseStack.pushPose();
            float angleRad = (float) Math.toRadians(indicator.relativeAngle);
            float indicatorX = centerX + (float) Math.sin(angleRad) * radius;
            float indicatorY = centerY - (float) Math.cos(angleRad) * radius;
            poseStack.translate(indicatorX, indicatorY, 0);
            poseStack.mulPose(Axis.ZP.rotation(angleRad));
            Matrix4f matrix = poseStack.last().pose();
            float alpha = 1.0F - ((float) age / duration);
            int a = (int) (Mth.clamp(alpha, 0, 1) * 200);
            int r = 255, g = 20, b = 20;
            float size = Config.Client.DAMAGE_2D_INDICATOR_SIZE.get().floatValue();
            Vec3 p1_top = new Vec3(0, -size, 0);
            Vec3 p2_left = new Vec3(-size * 0.7f, size * 0.7f, 0);
            Vec3 p3_right = new Vec3(size * 0.7f, size * 0.7f, 0);
            drawTriangle(bufferBuilder, matrix, p1_top, p2_left, p3_right, r, g, b, a);

            poseStack.popPose();
        });
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    private static Optional<LivingEntity> findBestTargetClientSide(Player player) {
        double maxRange = Config.Server.SPOT_RANGE.get();
        double fovAngle = Config.Server.SPOT_FOV_ANGLE.get();
        Vec3 playerEyePos = player.getEyePosition();
        AABB searchBox = player.getBoundingBox().inflate(maxRange);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchBox, e ->
                e != player && e.isAlive() && player.distanceToSqr(e) < maxRange * maxRange);
        return nearbyEntities.stream()
                .filter(entity -> {
                    Vec3 lookVec = player.getViewVector(1.0F);
                    Vec3 toEntityVec = entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(playerEyePos).normalize();
                    double dot = Math.max(-1.0, Math.min(1.0, lookVec.dot(toEntityVec)));
                    return Math.toDegrees(Math.acos(dot)) < fovAngle / 2.0;
                })
                .filter(entity -> {
                    Vec3 targetPos = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
                    HitResult result = player.level().clip(new ClipContext(playerEyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                    return result.getType() == HitResult.Type.MISS;
                })
                .min(Comparator.comparingDouble(entity -> {
                    Vec3 lookVec = player.getViewVector(1.0F);
                    Vec3 toEntityVec = entity.position().add(0, entity.getBbHeight() / 2.0, 0).subtract(playerEyePos).normalize();
                    return -lookVec.dot(toEntityVec);
                }));
    }
    private static void renderSpotMarker(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {
        poseStack.pushPose();
        poseStack.translate(0, entity.getBbHeight() + 0.5, 0);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        float entityYawRad = (float) Math.toRadians(180.0F - entity.getViewYRot(partialTicks));
        poseStack.mulPose(Axis.ZP.rotation(entityYawRad));
        poseStack.scale(0.025f, 0.025f, 0.025f);
        VertexConsumer vertexConsumer = buffer.getBuffer(EXRenderType.SPOT_MARKER_TRIANGLE);
        Matrix4f matrix = poseStack.last().pose();
        int r = 255, g = 50, b = 50, a = 200;
        float size = 10f;
        Vec3 p1_top = new Vec3(0, size, 0);
        Vec3 p2_left = new Vec3(-size / 1.5f, -size / 2.0f, 0);
        Vec3 p3_right = new Vec3(size / 1.5f, -size / 2.0f, 0);
        drawTriangle(vertexConsumer, matrix, p1_top, p2_left, p3_right, r, g, b, a);
        poseStack.popPose();
    }
    private static void render3dDamageIndicators(PoseStack poseStack) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        long currentTime = System.currentTimeMillis();
        int duration = Config.Server.DAMAGE_INDICATOR_DURATION_TICKS.get() * 50;

        VertexConsumer vertexConsumer = buffer.getBuffer(EXRenderType.SPOT_MARKER_TRIANGLE);

        activeIndicators.forEach(indicator -> {
            long age = currentTime - indicator.creationTime;
            if (age > duration) return;
            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            Vec3 offsetFromConfig = new Vec3(
                    Config.Client.DAMAGE_3D_INDICATOR_OFFSET_X.get(),
                    Config.Client.DAMAGE_3D_INDICATOR_OFFSET_Y.get(),
                    Config.Client.DAMAGE_3D_INDICATOR_OFFSET_Z.get()
            );
            Vec3 indicatorOriginPos = player.position().add(offsetFromConfig);
            Vec3 direction = indicator.sourcePos.subtract(indicatorOriginPos);
            float indicatorDistance = Config.Client.DAMAGE_3D_INDICATOR_DISTANCE.get().floatValue();
            Vec3 indicatorPos = indicatorOriginPos.add(direction.normalize().scale(indicatorDistance));
            poseStack.translate(indicatorPos.x, indicatorPos.y, indicatorPos.z);
            float yaw = (float) Mth.atan2(direction.x, direction.z);
            poseStack.mulPose(Axis.YP.rotation(yaw));
            double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            float pitch = (float) Mth.atan2(-direction.y, horizontalDistance);
            poseStack.mulPose(Axis.XP.rotation(pitch));
            Matrix4f matrix = poseStack.last().pose();
            float alpha = 1.0F - ((float) age / duration);
            int r = 255, g = 20, b = 20;
            int a = (int) (alpha * 200);
            float length = 0.5f;
            float width = 0.2f;
            Vec3 p1 = new Vec3(0, 0, length / 2); // 先端
            Vec3 p2 = new Vec3(-width, 0, -length / 2); // 左下
            Vec3 p3 = new Vec3(width, 0, -length / 2); // 右下
            drawTriangle(vertexConsumer, matrix, p1, p2, p3, r, g, b, a);
            poseStack.popPose();
        });
    }
    private static void drawTriangle(VertexConsumer c, Matrix4f m, Vec3 p1, Vec3 p2, Vec3 p3, int r, int g, int b, int a) {
        c.vertex(m, (float)p1.x, (float)p1.y, (float)p1.z).color(r, g, b, a).endVertex();
        c.vertex(m, (float)p2.x, (float)p2.y, (float)p2.z).color(r, g, b, a).endVertex();
        c.vertex(m, (float)p3.x, (float)p3.y, (float)p3.z).color(r, g, b, a).endVertex();
    }
}