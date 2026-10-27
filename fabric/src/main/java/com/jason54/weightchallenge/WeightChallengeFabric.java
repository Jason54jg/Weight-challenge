package com.jason54.weightchallenge;

import com.jason54.weightchallenge.command.WeightCommands;
import com.jason54.weightchallenge.network.WeightSyncPayload;
import com.jason54.weightchallenge.platform.WeightPlatform;
import com.jason54.weightchallenge.weight.WeightData;
import com.jason54.weightchallenge.weight.WeightLogic;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class WeightChallengeFabric implements ModInitializer {
    public static AttachmentType<WeightData> PLAYER_WEIGHT;

    @Override
    public void onInitialize() {
        FabricPayloadCompat.registerClientboundPlay(WeightSyncPayload.TYPE, WeightSyncPayload.STREAM_CODEC);

        WeightChallenge.bootstrap(
                new WeightPlatform.WeightAccess() {
                    @Override
                    public double getWeight(ServerPlayer player) {
                        return player.getAttachedOrCreate(PLAYER_WEIGHT).getWeightKg();
                    }

                    @Override
                    public void setWeight(ServerPlayer player, double weightKg, boolean sync) {
                        player.getAttachedOrCreate(PLAYER_WEIGHT).setWeightKg(weightKg);
                        if (sync) {
                            syncWeight(player);
                        }
                    }
                },
                FabricLoader.getInstance().getConfigDir(),
                WeightChallengeFabric::syncWeight
        );

        PLAYER_WEIGHT = AttachmentRegistry.create(
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "player_weight"),
                builder -> builder
                        .initializer(() -> new WeightData(com.jason54.weightchallenge.config.WeightConfig.get().defaultWeightKg))
                        .persistent(WeightData.CODEC)
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> WeightCommands.register(dispatcher));

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                WeightLogic.onAttack(serverPlayer);
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                WeightLogic.onBlockMined(serverPlayer);
            }
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                WeightLogic.onServerPlayerTick(player);
            }
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                WeightLogic.onPlayerLogin(handler.getPlayer())
        );
    }

    static void syncWeight(ServerPlayer player) {
        ServerPlayNetworking.send(player, new WeightSyncPayload(WeightPlatform.getWeight(player)));
    }
}
