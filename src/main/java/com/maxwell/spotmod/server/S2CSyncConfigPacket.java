package com.maxwell.spotmod.server;

import com.maxwell.spotmod.client.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CSyncConfigPacket {

    private final boolean indicatorsAllowed;
    private final int indicatorDurationTicks;


    public S2CSyncConfigPacket(boolean indicatorsAllowed, int indicatorDurationTicks) {
        this.indicatorsAllowed = indicatorsAllowed;
        this.indicatorDurationTicks = indicatorDurationTicks; // 追加
    }

    // パケットをバイトデータに変換するメソッド
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.indicatorsAllowed);
        buf.writeInt(this.indicatorDurationTicks); // 追加
    }
    public S2CSyncConfigPacket(FriendlyByteBuf buf) {
        this.indicatorsAllowed = buf.readBoolean();
        this.indicatorDurationTicks = buf.readInt(); // 追加
    }

    // handleメソッドを修正
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientEvents.updateServerConfig(this.indicatorsAllowed, this.indicatorDurationTicks);
        });
        return true;
    }
}