package com.Maxwell.spotmod.Client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CSpotEntityPacket {

    private final UUID entityUUID;
    private final int durationTicks;
    public S2CSpotEntityPacket(UUID entityUUID, int durationTicks) {
        this.entityUUID = entityUUID;
        this.durationTicks = durationTicks;
    }
    public S2CSpotEntityPacket(FriendlyByteBuf buf) {
        this.entityUUID = buf.readUUID();
        this.durationTicks = buf.readInt();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityUUID);
        buf.writeInt(durationTicks);
    }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientSpottedManager.updateSpottedEntity(this.entityUUID, this.durationTicks));
    }
}