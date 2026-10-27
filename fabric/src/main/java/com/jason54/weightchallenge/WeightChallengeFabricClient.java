package com.jason54.weightchallenge;

import com.jason54.weightchallenge.client.WeightHud;
import com.jason54.weightchallenge.config.WeightConfig;
import com.jason54.weightchallenge.network.WeightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class WeightChallengeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WeightConfig.load(FabricLoader.getInstance().getConfigDir().resolve("weightchallenge.json"));

        ClientPlayNetworking.registerGlobalReceiver(WeightSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> WeightHud.setClientWeight(payload.weightKg()))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                WeightHud.tick(client.player);
            }
        });
    }
}
