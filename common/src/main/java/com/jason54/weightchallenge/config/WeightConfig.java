package com.jason54.weightchallenge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.jason54.weightchallenge.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration chargée depuis {@code config/weightchallenge.json}.
 * Toutes les valeurs de gameplay sont modifiables sans recompiler le mod.
 */
public final class WeightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static WeightConfig INSTANCE = WeightConfig.defaults();

    @SerializedName("defaultWeightKg")
    public double defaultWeightKg = 450.0D;

    @SerializedName("goalWeightKg")
    public double goalWeightKg = 90.0D;

    @SerializedName("minimumWeightKg")
    public double minimumWeightKg = 40.0D;

    @SerializedName("maximumWeightKg")
    public double maximumWeightKg = 600.0D;

    @SerializedName("sprintDisableThresholdKg")
    public double sprintDisableThresholdKg = 180.0D;

    @SerializedName("scaleAtMinWeight")
    public double scaleAtMinWeight = 0.85D;

    @SerializedName("scaleAtMaxWeight")
    public double scaleAtMaxWeight = 2.5D;

    @SerializedName("scaleReferenceMinKg")
    public double scaleReferenceMinKg = 90.0D;

    @SerializedName("scaleReferenceMaxKg")
    public double scaleReferenceMaxKg = 450.0D;

    @SerializedName("lossPerSecondWalking")
    public double lossPerSecondWalking = 0.1D;

    @SerializedName("lossPerSecondSprinting")
    public double lossPerSecondSprinting = 0.1D;

    @SerializedName("lossPerSecondSwimming")
    public double lossPerSecondSwimming = 0.15D;

    @SerializedName("lossPerSecondMining")
    public double lossPerSecondMining = 0.08D;

    @SerializedName("lossPerSneakAction")
    public double lossPerSneakAction = 0.2D;

    @SerializedName("lossPerJump")
    public double lossPerJump = 0.5D;

    @SerializedName("gainPerSecondIdle")
    public double gainPerSecondIdle = 0.1D;

    @SerializedName("attackLossMinKg")
    public double attackLossMinKg = 0.05D;

    @SerializedName("attackLossMaxKg")
    public double attackLossMaxKg = 0.6D;

    @SerializedName("enableHeavyFallBonus")
    public boolean enableHeavyFallBonus = true;

    @SerializedName("heavyFallThresholdKg")
    public double heavyFallThresholdKg = 200.0D;

    @SerializedName("heavyFallDamageMultiplier")
    public double heavyFallDamageMultiplier = 1.5D;

    /**
     * ACTION_BAR ou OVERLAY (overlay utilise aussi l'action bar avec un style plus visible).
     */
    @SerializedName("hudMode")
    public String hudMode = "ACTION_BAR";

    @SerializedName("hudUpdateIntervalTicks")
    public int hudUpdateIntervalTicks = 10;

    @SerializedName("defaultFoodWeightGainKg")
    public double defaultFoodWeightGainKg = 0.5D;

    @SerializedName("foodWeightGain")
    public Map<String, Double> foodWeightGain = defaultFoodMap();

    public static WeightConfig get() {
        return INSTANCE;
    }

    public static WeightConfig defaults() {
        WeightConfig config = new WeightConfig();
        config.foodWeightGain = defaultFoodMap();
        return config;
    }

    private static Map<String, Double> defaultFoodMap() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("minecraft:melon_slice", 0.3D);
        map.put("minecraft:apple", 0.4D);
        map.put("minecraft:bread", 0.5D);
        map.put("minecraft:cooked_beef", 6.0D);
        map.put("minecraft:cooked_porkchop", 5.0D);
        map.put("minecraft:cooked_chicken", 4.0D);
        map.put("minecraft:cooked_mutton", 4.5D);
        map.put("minecraft:cooked_rabbit", 3.5D);
        map.put("minecraft:golden_carrot", 2.0D);
        map.put("minecraft:golden_apple", 3.0D);
        map.put("minecraft:enchanted_golden_apple", 8.0D);
        return map;
    }

    public static void load(Path configPath) {
        if (!Files.exists(configPath)) {
            INSTANCE = defaults();
            save(configPath);
            Constants.LOG.info("Fichier de config cree : {}", configPath);
            return;
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            WeightConfig loaded = GSON.fromJson(reader, WeightConfig.class);
            INSTANCE = loaded != null ? loaded : defaults();
            if (INSTANCE.foodWeightGain == null || INSTANCE.foodWeightGain.isEmpty()) {
                INSTANCE.foodWeightGain = defaultFoodMap();
            }
            Constants.LOG.info("Configuration Weight Challenge chargee.");
        } catch (IOException exception) {
            Constants.LOG.error("Impossible de lire {}, utilisation des valeurs par defaut.", configPath, exception);
            INSTANCE = defaults();
        }
    }

    public static void save(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(get(), writer);
            }
        } catch (IOException exception) {
            Constants.LOG.error("Impossible d'ecrire {}", configPath, exception);
        }
    }

    public double foodGainFor(String itemId) {
        return foodWeightGain.getOrDefault(itemId, defaultFoodWeightGainKg);
    }

    public double clampWeight(double weightKg) {
        return Math.max(minimumWeightKg, Math.min(maximumWeightKg, weightKg));
    }
}
