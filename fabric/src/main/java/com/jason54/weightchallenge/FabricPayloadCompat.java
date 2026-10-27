package com.jason54.weightchallenge;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Method;

/**
 * Compat Fabric networking : {@code playS2C} (1.21.11) / {@code clientboundPlay} (26.x).
 */
public final class FabricPayloadCompat {
    private FabricPayloadCompat() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerClientboundPlay(CustomPacketPayload.Type<?> type, StreamCodec codec) {
        try {
            Method registryMethod;
            try {
                registryMethod = PayloadTypeRegistry.class.getMethod("clientboundPlay");
            } catch (NoSuchMethodException ignored) {
                registryMethod = PayloadTypeRegistry.class.getMethod("playS2C");
            }
            Object registry = registryMethod.invoke(null);
            Method register = registry.getClass().getMethod("register", CustomPacketPayload.Type.class, StreamCodec.class);
            register.invoke(registry, type, codec);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register clientbound payload", e);
        }
    }
}
