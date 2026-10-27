package com.jason54.weightchallenge.command;

import com.jason54.weightchallenge.config.WeightConfig;
import com.jason54.weightchallenge.platform.WeightPlatform;
import com.jason54.weightchallenge.weight.WeightLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Admin commands: /weight set|add|reset|get
 */
public final class WeightCommands {
    private WeightCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("weight")
                        .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("kg", DoubleArgumentType.doubleArg(0))
                                                                        .executes(ctx -> {
                                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                                            double kg = WeightConfig.get().clampWeight(DoubleArgumentType.getDouble(ctx, "kg"));
                                                                            WeightPlatform.setWeight(target, kg, true);
                                                                            WeightLogic.applyEffects(target);
                                                                            String name = target.getName().getString();
                                                                            String formatted = String.format("%.1f", kg);
                                                                            ctx.getSource().sendSuccess(
                                                                                    () -> Component.translatable("weightchallenge.command.set", name, formatted),
                                                                                    true
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("delta", DoubleArgumentType.doubleArg())
                                                                        .executes(ctx -> {
                                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                                            double delta = DoubleArgumentType.getDouble(ctx, "delta");
                                                                            double next = WeightConfig.get().clampWeight(WeightPlatform.getWeight(target) + delta);
                                                                            WeightPlatform.setWeight(target, next, true);
                                                                            WeightLogic.applyEffects(target);
                                                                            String name = target.getName().getString();
                                                                            String formatted = String.format("%.1f", next);
                                                                            ctx.getSource().sendSuccess(
                                                                                    () -> Component.translatable("weightchallenge.command.add", name, formatted),
                                                                                    true
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("reset")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> {
                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                            WeightLogic.resetWeight(target);
                                                            String name = target.getName().getString();
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable("weightchallenge.command.reset", name),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("get")
                                        .executes(ctx -> sendGet(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> sendGet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                        )
                        )
        );
    }

    private static int sendGet(CommandSourceStack source, ServerPlayer target) {
        double weight = WeightPlatform.getWeight(target);
        String name = target.getName().getString();
        String formattedWeight = String.format("%.1f", weight);
        String formattedGoal = String.format("%.0f", WeightConfig.get().goalWeightKg);
        source.sendSuccess(
                () -> Component.translatable("weightchallenge.command.get", name, formattedWeight, formattedGoal),
                false
        );
        return 1;
    }
}
