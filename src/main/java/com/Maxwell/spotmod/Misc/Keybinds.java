package com.Maxwell.spotmod.Misc;

import com.Maxwell.spotmod.SpotMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
@OnlyIn(Dist.CLIENT)
public class Keybinds {
    public static final String KEY_CATEGORY_SPOTMOD = "key.category.spot_mod";
    public static final String KEY_SPOT = "key.spot_mod.spot";
    public static final KeyMapping SPOT_KEY = new KeyMapping(
            KEY_SPOT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY_SPOTMOD
    );
    @Mod.EventBusSubscriber(modid = SpotMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModClientEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(Keybinds.SPOT_KEY);
        }
    }

}