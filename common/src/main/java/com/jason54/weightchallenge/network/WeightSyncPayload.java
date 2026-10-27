package com.jason54.weightchallenge.network;

import com.jason54.weightchallenge.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet serveur → client pour synchroniser le poids affiché dans le HUD.
 */
public record WeightSyncPayload(double weightKg) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weight_sync");
    public static final Type<WeightSyncPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, WeightSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeDouble(payload.weightKg),
            buf -> new WeightSyncPayload(buf.readDouble())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
