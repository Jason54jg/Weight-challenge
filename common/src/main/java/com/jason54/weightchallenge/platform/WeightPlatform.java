package com.jason54.weightchallenge.platform;

import com.jason54.weightchallenge.config.WeightConfig;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Pont loader (Fabric / NeoForge) vers le code commun.
 */
public final class WeightPlatform {
    private static Path configDirectory = Path.of("config");
    private static Consumer<ServerPlayer> syncCallback = player -> {};
    private static WeightAccess access = new WeightAccess() {
        @Override
        public double getWeight(ServerPlayer player) {
            return WeightConfig.get().defaultWeightKg;
        }

        @Override
        public void setWeight(ServerPlayer player, double weightKg, boolean sync) {
        }
    };

    public interface WeightAccess {
        double getWeight(ServerPlayer player);

        void setWeight(ServerPlayer player, double weightKg, boolean sync);
    }

    public static void init(Path configDir, WeightAccess weightAccess, Consumer<ServerPlayer> sync) {
        configDirectory = configDir;
        access = weightAccess;
        syncCallback = sync;
        WeightConfig.load(configDirectory.resolve("weightchallenge.json"));
    }

    public static Path configDirectory() {
        return configDirectory;
    }

    public static double getWeight(ServerPlayer player) {
        return access.getWeight(player);
    }

    public static void setWeight(ServerPlayer player, double weightKg, boolean sync) {
        access.setWeight(player, weightKg, sync);
    }

    public static void syncToClient(ServerPlayer player) {
        syncCallback.accept(player);
    }

    public static void reloadConfig() {
        WeightConfig.load(configDirectory().resolve("weightchallenge.json"));
    }
}
