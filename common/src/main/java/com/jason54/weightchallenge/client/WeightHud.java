package com.jason54.weightchallenge.client;

import com.jason54.weightchallenge.config.WeightConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Real-time weight HUD (action bar).
 */
public final class WeightHud {
    private static double clientWeightKg = WeightConfig.get().defaultWeightKg;

    private WeightHud() {}

    public static void setClientWeight(double weightKg) {
        clientWeightKg = weightKg;
    }

    public static void tick(LocalPlayer player) {
        WeightConfig config = WeightConfig.get();
        if (player.tickCount % Math.max(1, config.hudUpdateIntervalTicks) != 0) {
            return;
        }
        double goal = config.goalWeightKg;
        String mode = config.hudMode == null ? "ACTION_BAR" : config.hudMode.toUpperCase();
        Component message = Component.translatable(
                "weightchallenge.hud.weight",
                String.format("%.1f", clientWeightKg),
                String.format("%.0f", goal)
        );
        if ("OVERLAY".equals(mode)) {
            message = Component.literal("§6§l").append(message);
        }
        ClientMessageCompat.showActionBar(player, message);
    }
}
