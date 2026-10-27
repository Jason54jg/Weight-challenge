package com.jason54.weightchallenge.weight;

import com.jason54.weightchallenge.config.WeightConfig;
import com.jason54.weightchallenge.platform.WeightPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Règles de perte / gain de poids et effets gameplay.
 */
public final class WeightLogic {
    private static final Map<UUID, Boolean> PREVIOUS_SNEAK = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> MINING_THIS_TICK = new ConcurrentHashMap<>();

    private WeightLogic() {}

    public static void onPlayerLogin(ServerPlayer player) {
        double weight = WeightPlatform.getWeight(player);
        if (weight <= 0.0D) {
            weight = WeightConfig.get().defaultWeightKg;
            WeightPlatform.setWeight(player, weight, true);
        } else {
            applyEffects(player);
            WeightPlatform.syncToClient(player);
        }
    }

    public static void onServerPlayerTick(ServerPlayer player) {
        WeightConfig config = WeightConfig.get();
        UUID id = player.getUUID();

        boolean sneaking = player.isShiftKeyDown();
        Boolean wasSneaking = PREVIOUS_SNEAK.get(id);
        if (wasSneaking != null && !wasSneaking && sneaking) {
            adjustWeight(player, -config.lossPerSneakAction);
        }
        PREVIOUS_SNEAK.put(id, sneaking);

        if (player.tickCount % 20 != 0) {
            applyEffects(player);
            return;
        }

        double delta = 0.0D;
        Vec3 velocity = player.getDeltaMovement();
        boolean horizontalMove = velocity.horizontalDistanceSqr() > 0.0008D;
        boolean swimming = player.isInWater() && (player.isSwimming() || horizontalMove);
        boolean mining = Boolean.TRUE.equals(MINING_THIS_TICK.remove(id));

        if (swimming) {
            delta -= config.lossPerSecondSwimming;
        } else if (mining) {
            delta -= config.lossPerSecondMining;
        } else if (horizontalMove) {
            if (player.isSprinting() && canSprint(player)) {
                delta -= config.lossPerSecondSprinting;
            } else if (horizontalMove) {
                delta -= config.lossPerSecondWalking;
            }
        } else {
            delta += config.gainPerSecondIdle;
        }

        if (delta != 0.0D) {
            adjustWeight(player, delta);
        }
        applyEffects(player);
    }

    public static void onJump(ServerPlayer player) {
        adjustWeight(player, -WeightConfig.get().lossPerJump);
    }

    public static void onAttack(ServerPlayer player) {
        WeightConfig config = WeightConfig.get();
        double span = config.attackLossMaxKg - config.attackLossMinKg;
        double loss = config.attackLossMinKg + ThreadLocalRandom.current().nextDouble() * Math.max(0.0D, span);
        adjustWeight(player, -loss);
    }

    public static void onBlockMined(ServerPlayer player) {
        MINING_THIS_TICK.put(player.getUUID(), true);
    }

    public static void onFoodEaten(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        double gain = WeightConfig.get().foodGainFor(id.toString());
        adjustWeight(player, gain);
    }

    public static float modifyFallDamage(ServerPlayer player, float damage) {
        WeightConfig config = WeightConfig.get();
        if (!config.enableHeavyFallBonus) {
            return damage;
        }
        double weight = WeightPlatform.getWeight(player);
        if (weight >= config.heavyFallThresholdKg) {
            return damage * (float) config.heavyFallDamageMultiplier;
        }
        return damage;
    }

    public static void resetWeight(ServerPlayer player) {
        WeightPlatform.setWeight(player, WeightConfig.get().defaultWeightKg, true);
        applyEffects(player);
    }

    public static void applyEffects(ServerPlayer player) {
        WeightConfig config = WeightConfig.get();
        double weight = WeightPlatform.getWeight(player);

        if (weight > config.sprintDisableThresholdKg && player.isSprinting()) {
            player.setSprinting(false);
        }

        if (player.getAttribute(Attributes.SCALE) != null) {
            double scale = WeightScale.scaleForWeight(weight);
            player.getAttribute(Attributes.SCALE).setBaseValue(scale);
        }
    }

    public static boolean canSprint(ServerPlayer player) {
        return WeightPlatform.getWeight(player) <= WeightConfig.get().sprintDisableThresholdKg;
    }

    private static void adjustWeight(ServerPlayer player, double deltaKg) {
        WeightConfig config = WeightConfig.get();
        double next = config.clampWeight(WeightPlatform.getWeight(player) + deltaKg);
        WeightPlatform.setWeight(player, next, true);
        applyEffects(player);
    }

    public static double getWeight(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return WeightPlatform.getWeight(serverPlayer);
        }
        return WeightConfig.get().defaultWeightKg;
    }
}
