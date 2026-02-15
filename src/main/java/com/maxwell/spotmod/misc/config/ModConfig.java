package com.maxwell.spotmod.misc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {

    public static class Server {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;

        public static final ForgeConfigSpec.DoubleValue SPOT_RANGE;
        public static final ForgeConfigSpec.DoubleValue SPOT_FOV_ANGLE;
        public static final ForgeConfigSpec.IntValue SPOT_DURATION_SECONDS;
        public static final ForgeConfigSpec.IntValue SPOT_COOLDOWN_TICKS;
        public static final ForgeConfigSpec.BooleanValue SPOT_ENABLE;
        public static final ForgeConfigSpec.BooleanValue ENABLE_ALL_INDICATORS;
        public static final ForgeConfigSpec.IntValue DAMAGE_INDICATOR_DURATION_TICKS;

        static {
            BUILDER.push("Spot Mod - Server Settings");
            BUILDER.pop();
            BUILDER.push("Spot Mod - Spot Settings");
            SPOT_RANGE = BUILDER
                    .comment("Change the spotting range")
                    .defineInRange("Spot_Range", 64.0, 1.0, 256.0);
            SPOT_FOV_ANGLE = BUILDER
                    .comment("At what angle can a spot be created?")
                    .defineInRange("Spot_Fov_Angle", 90.0, 1.0, 180.0);
            SPOT_DURATION_SECONDS = BUILDER
                    .comment("Change the duration of the spot")
                    .defineInRange("Spot_Duration_Seconds", 10, 1, 300);
            SPOT_COOLDOWN_TICKS = BUILDER
                    .comment("Change the cooldown of the spot")
                    .defineInRange("Spot_Cooldown_Ticks", 10, 0, 200);
            SPOT_ENABLE = BUILDER
                    .comment("Enable spot (true or false)")
                    .define("Enable_Spot", false);
            BUILDER.pop();
            BUILDER.push("Spot Mod - Server Damage Indicators Settings");
            ENABLE_ALL_INDICATORS = BUILDER
                    .comment("When True, all indicators are allowed.(true or false)")
                    .define("Enable_all_damageIndicator", true);

            DAMAGE_INDICATOR_DURATION_TICKS = BUILDER
                    .comment("Sets how long the damage indicator will remain present.")
                    .defineInRange("Damage_Indicator_Duration_Ticks", 20, 0, 9999);
            SPEC = BUILDER.build();
        }
    }

    public static class Client {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec SPEC;
        public static final ForgeConfigSpec.BooleanValue ENABLE_3D_DAMAGE_INDICATOR;
        public static final ForgeConfigSpec.BooleanValue ENABLE_2D_DAMAGE_INDICATOR;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_3D_INDICATOR_DISTANCE;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_3D_INDICATOR_OFFSET_X;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_3D_INDICATOR_OFFSET_Y;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_3D_INDICATOR_OFFSET_Z;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_3D_INDICATOR_SIZE;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_2D_INDICATOR_DISTANCE;
        public static final ForgeConfigSpec.DoubleValue DAMAGE_2D_INDICATOR_SIZE;
        public static final ForgeConfigSpec.BooleanValue ENABLE_SPOT_ANIMATION_HAND;
        static {
            BUILDER.push("Spot Mod - Client Settings");

            BUILDER.pop();
            BUILDER.push("3D Damage Indicator");
            ENABLE_3D_DAMAGE_INDICATOR = BUILDER
                    .comment("Enable 3D Damage Indicator(true or false)")
                    .define("Enable_3D_Damage_Indicator", false);

            DAMAGE_3D_INDICATOR_DISTANCE = BUILDER
                    .comment("Distance of the 3D Damage Indicator from the player (in blocks).")
                    .defineInRange("Damage_3D_Indicator_Distance", 1.5f, 0f, 10f);

            DAMAGE_3D_INDICATOR_OFFSET_X = BUILDER
                    .comment("Offset of the 3D damage indicator's origin point on the X-axis.")
                    .defineInRange("Damage_3D_Indicator_OffsetX", 0.0, -10.0, 10.0);

            DAMAGE_3D_INDICATOR_OFFSET_Y = BUILDER
                    .comment("Offset of the 3D damage indicator's origin point on the Y-axis. (Default is player's center height)")
                    .defineInRange("Damage_3D_Indicator_OffsetY", 0.8, -10.0, 10.0);

            DAMAGE_3D_INDICATOR_OFFSET_Z = BUILDER
                    .comment("Offset of the 3D damage indicator's origin point on the Z-axis.")
                    .defineInRange("Damage_3D_Indicator_OffsetZ", 0.0, -10.0, 10.0);

            DAMAGE_3D_INDICATOR_SIZE = BUILDER
                    .comment("Determines the size (length) of the 3D damage indicator arrow.")
                    .defineInRange("Damage_3D_Indicator_Size", 0.5, 0.1, 5.0);
            BUILDER.pop();
            BUILDER.push("2D Damage Indicator");
            ENABLE_2D_DAMAGE_INDICATOR = BUILDER
                    .comment("Enable 2D Damage Indicator(true or false)")
                    .define("Enable_2D_Damage_Indicator", true);

            DAMAGE_2D_INDICATOR_DISTANCE = BUILDER
                    .comment("Determines how far away the indicator is from the crosshairs.(The lower the number, the closer you are to the crosshair.)")
                    .defineInRange("Damage_2D_Indicator_Distance", 60.0f, 0f, 150f);

            DAMAGE_2D_INDICATOR_SIZE = BUILDER
                    .comment("Determines the size of the damage indicator.")
                    .defineInRange("Damage_2D_Indicator_Size", 8.0f, 0f, 60f);
            ENABLE_SPOT_ANIMATION_HAND = BUILDER
                    .comment("The arms will be animated when spotting. This is initially set to false as it can cause strange behavior when used while crouching.")
                    .define("Enable_Spot_Animation_Hand",false);
            BUILDER.pop();
            SPEC = BUILDER.build();
        }
    }
}