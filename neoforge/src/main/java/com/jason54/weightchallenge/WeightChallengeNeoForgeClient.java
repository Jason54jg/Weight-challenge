package com.jason54.weightchallenge;

import com.jason54.weightchallenge.client.WeightHud;
import com.jason54.weightchallenge.config.WeightConfig;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class WeightChallengeNeoForgeClient {
    private WeightChallengeNeoForgeClient() {}

    static {
        WeightConfig.load(FMLPaths.CONFIGDIR.get().resolve("weightchallenge.json"));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player != null) {
            WeightHud.tick(Minecraft.getInstance().player);
        }
    }
}
