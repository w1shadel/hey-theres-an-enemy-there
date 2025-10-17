package com.Maxwell.spotmod.Misc;

import com.Maxwell.spotmod.Misc.Config.ModConfig;
import com.Maxwell.spotmod.Server.S2CSyncConfigPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        new ModCommands(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    public ModCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> rootCommand = Commands.literal("spotmod");
        rootCommand
                .then(Commands.literal("spot")
                        .requires(source -> source.hasPermission(2)) // OPレベル2以上
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setServerBoolean(context.getSource(), "spot_enable", BoolArgumentType.getBool(context, "enabled")))
                        )
                )
                .then(Commands.literal("all_indicators")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setServerBoolean(context.getSource(), "all_indicators", BoolArgumentType.getBool(context, "enabled")))
                        )
                )
                .then(Commands.literal("duration")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                .executes(context -> setServerInteger(context.getSource(), "duration", IntegerArgumentType.getInteger(context, "ticks")))
                        )
                );

        LiteralArgumentBuilder<CommandSourceStack> indicatorCommand = Commands.literal("indicator")
                .requires(source -> true); // 権限不要

        indicatorCommand.then(Commands.literal("2d")
                .then(Commands.literal("enable")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setClientBoolean(context.getSource(), "2d_enable", BoolArgumentType.getBool(context, "enabled")))
                        )
                )
                .then(Commands.literal("distance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                .executes(context -> setClientDouble(context.getSource(), "2d_distance", DoubleArgumentType.getDouble(context, "value")))
                        )
                )
                .then(Commands.literal("size")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                .executes(context -> setClientDouble(context.getSource(), "2d_size", DoubleArgumentType.getDouble(context, "value")))
                        )
                )
        );


        indicatorCommand.then(Commands.literal("3d")
                .then(Commands.literal("enable")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> setClientBoolean(context.getSource(), "3d_enable", BoolArgumentType.getBool(context, "enabled")))
                        )
                )
                .then(Commands.literal("distance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0))
                                .executes(context -> setClientDouble(context.getSource(), "3d_distance", DoubleArgumentType.getDouble(context, "value")))
                        )
                )
                .then(Commands.literal("size")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1))
                                .executes(context -> setClientDouble(context.getSource(), "3d_size", DoubleArgumentType.getDouble(context, "value")))
                        )
                )
                .then(Commands.literal("offset")
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .executes(context -> setClientVec3(context.getSource(), "3d_offset", Vec3Argument.getVec3(context, "pos")))
                        )
                )
        );


        rootCommand.then(indicatorCommand);
        dispatcher.register(rootCommand);
    }


    private int setServerBoolean(CommandSourceStack source, String type, boolean enabled) {
        String status = enabled ? "enabled" : "disabled";
        switch (type) {
            case "spot_enable" -> {
                ModConfig.Server.SPOT_ENABLE.clearCache();
                ModConfig.Server.SPOT_ENABLE.set(enabled);
                source.sendSuccess(() -> Component.literal("Spotting has been globally " + status), true);
            }
            case "all_indicators" -> {
                ModConfig.Server.ENABLE_ALL_INDICATORS.clearCache();
                ModConfig.Server.ENABLE_ALL_INDICATORS.set(enabled);
                source.sendSuccess(() -> Component.literal("All damage indicators have been globally " + status), true);
                int duration = ModConfig.Server.DAMAGE_INDICATOR_DURATION_TICKS.get();
                S2CSyncConfigPacket packet = new S2CSyncConfigPacket(enabled, duration);
                source.getServer().getPlayerList().getPlayers().forEach(player -> {
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
                });
            }
            default -> {
                source.sendFailure(Component.literal("Invalid server boolean type."));
                return 0;
            }
        }
        ModConfig.Server.SPEC.save();
        return 1;
    }

    private int setServerInteger(CommandSourceStack source, String type, int value) {
        if ("duration".equals(type)) {
            ModConfig.Server.DAMAGE_INDICATOR_DURATION_TICKS.clearCache();
            ModConfig.Server.DAMAGE_INDICATOR_DURATION_TICKS.set(value);
            source.sendSuccess(() -> Component.literal("Damage indicator duration set to " + value + " ticks."), true);
        } else {
            source.sendFailure(Component.literal("Invalid server integer type."));
            return 0;
        }
        boolean allows = ModConfig.Server.ENABLE_ALL_INDICATORS.get();
        S2CSyncConfigPacket packet = new S2CSyncConfigPacket(allows, value); // "value" は duration の新しい値
        source.getServer().getPlayerList().getPlayers().forEach(player -> {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
        });
        ModConfig.Server.SPEC.save();
        return 1;
    }


    private int setClientBoolean(CommandSourceStack source, String type, boolean enabled) {
        String status = enabled ? "enabled" : "disabled";
        switch (type) {
            case "2d_enable" -> {
                ModConfig.Client.ENABLE_2D_DAMAGE_INDICATOR.clearCache();
                ModConfig.Client.ENABLE_2D_DAMAGE_INDICATOR.set(enabled);
                source.sendSuccess(() -> Component.literal("2D Damage Indicator has been " + status + "."), true);
            }
            case "3d_enable" -> {
                ModConfig.Client.ENABLE_3D_DAMAGE_INDICATOR.clearCache();
                ModConfig.Client.ENABLE_3D_DAMAGE_INDICATOR.set(enabled);
                source.sendSuccess(() -> Component.literal("3D Damage Indicator has been " + status + "."), true);
            }
            default -> {
                source.sendFailure(Component.literal("Invalid client boolean type."));
                return 0;
            }
        }
        ModConfig.Client.SPEC.save();
        return 1;
    }

    private int setClientDouble(CommandSourceStack source, String type, double value) {
        switch (type) {
            case "2d_distance" -> {
                ModConfig.Client.DAMAGE_2D_INDICATOR_DISTANCE.clearCache();
                ModConfig.Client.DAMAGE_2D_INDICATOR_DISTANCE.set(value);
                source.sendSuccess(() -> Component.literal("2D indicator distance set to " + value), true);
            }
            case "2d_size" -> {
                ModConfig.Client.DAMAGE_2D_INDICATOR_SIZE.clearCache();
                ModConfig.Client.DAMAGE_2D_INDICATOR_SIZE.set(value);
                source.sendSuccess(() -> Component.literal("2D indicator size set to " + value), true);
            }
            case "3d_distance" -> {
                ModConfig.Client.DAMAGE_3D_INDICATOR_DISTANCE.clearCache();
                ModConfig.Client.DAMAGE_3D_INDICATOR_DISTANCE.set(value);
                source.sendSuccess(() -> Component.literal("3D indicator distance set to " + value), true);
            }
            case "3d_size" -> {
                ModConfig.Client.DAMAGE_3D_INDICATOR_SIZE.clearCache();
                ModConfig.Client.DAMAGE_3D_INDICATOR_SIZE.set(value);
                source.sendSuccess(() -> Component.literal("3D indicator size set to " + value), true);
            }
            default -> {
                source.sendFailure(Component.literal("Invalid client double type."));
                return 0;
            }
        }
        ModConfig.Client.SPEC.save();
        return 1;
    }

    private int setClientVec3(CommandSourceStack source, String type, Vec3 pos) {
        if ("3d_offset".equals(type)) {
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_X.clearCache();
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_Y.clearCache();
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_Z.clearCache();
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_X.set(pos.x());
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_Y.set(pos.y());
            ModConfig.Client.DAMAGE_3D_INDICATOR_OFFSET_Z.set(pos.z());
            source.sendSuccess(() -> Component.literal(String.format("3D indicator offset set to (%.2f, %.2f, %.2f)", pos.x(), pos.y(), pos.z())), true);
        } else {
            source.sendFailure(Component.literal("Invalid client vec3 type."));
            return 0;
        }
        ModConfig.Client.SPEC.save();
        return 1;
    }
}