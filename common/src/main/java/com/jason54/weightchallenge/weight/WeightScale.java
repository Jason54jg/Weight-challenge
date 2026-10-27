package com.jason54.weightchallenge.weight;

import com.jason54.weightchallenge.config.WeightConfig;

/**
 * Calcule l'échelle du joueur (attribut {@code generic.scale}) à partir du poids.
 */
public final class WeightScale {
    private WeightScale() {}

    public static double scaleForWeight(double weightKg) {
        WeightConfig config = WeightConfig.get();
        double minKg = config.scaleReferenceMinKg;
        double maxKg = config.scaleReferenceMaxKg;
        double t = (weightKg - minKg) / (maxKg - minKg);
        t = Math.max(0.0D, Math.min(1.0D, t));
        return config.scaleAtMinWeight + t * (config.scaleAtMaxWeight - config.scaleAtMinWeight);
    }
}
