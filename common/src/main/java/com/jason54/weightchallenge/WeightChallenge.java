package com.jason54.weightchallenge;

import com.jason54.weightchallenge.platform.WeightPlatform;

/**
 * Point d'entrée commun appelé par Fabric et NeoForge.
 */
public final class WeightChallenge {
    private WeightChallenge() {}

    public static void bootstrap(WeightPlatform.WeightAccess access, java.nio.file.Path configDir, java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> sync) {
        WeightPlatform.init(configDir, access, sync);
        Constants.LOG.info("Weight Challenge initialise.");
    }
}
