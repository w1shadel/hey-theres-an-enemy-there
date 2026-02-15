package com.maxwell.spotmod.client.helper;
import com.maxwell.spotmod.client.PlayerAnimationRegister;
import com.maxwell.spotmod.SpotMod;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SpotMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AnimationPlayHelper {
    private static AnimationState localPlayerAnimationState = null;

    private static class AnimationState {
        int perspectiveTicks = 0;
        CameraType oldPerspective;
        boolean lockMovement = false;

        AnimationState(KeyframeAnimation anim, boolean lock) {
            this.perspectiveTicks = anim.stopTick;
            this.lockMovement = lock;
            this.oldPerspective = Minecraft.getInstance().options.getCameraType();
        }
    }
    public static void playAnimation(Player player, String animationName, boolean lock) {
        Minecraft mc = Minecraft.getInstance();
        if (!(player instanceof AbstractClientPlayer clientPlayer)) return;
        var data = PlayerAnimationAccess.getPlayerAssociatedData(clientPlayer);
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) data.get(PlayerAnimationRegister.ANIMATION_ID);
        if (layer == null) return;
        ResourceLocation id = new ResourceLocation(SpotMod.MODID, animationName);
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(id);
        if (anim == null) return;
        layer.setAnimation(new KeyframeAnimationPlayer(anim));
        if (player == mc.player) {
            localPlayerAnimationState = new AnimationState(anim, lock);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || localPlayerAnimationState == null) return;

        if (localPlayerAnimationState.perspectiveTicks > 0) {
            localPlayerAnimationState.perspectiveTicks--;
            if (localPlayerAnimationState.perspectiveTicks == 0) {
                Minecraft.getInstance().options.setCameraType(localPlayerAnimationState.oldPerspective);
                localPlayerAnimationState = null;
            }
        }
    }
}