package com.maxwell.spotmod.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CDamageIndicatorPacket(Vec3 attackerPos) {
    public static void encode(S2CDamageIndicatorPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.attackerPos.x);
        buf.writeDouble(msg.attackerPos.y);
        buf.writeDouble(msg.attackerPos.z);
    }

    public static S2CDamageIndicatorPacket decode(FriendlyByteBuf buf) {
        return new S2CDamageIndicatorPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public static void handle(S2CDamageIndicatorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientEvents.addDamageIndicator(msg.attackerPos));
        ctx.get().setPacketHandled(true);
    }
}