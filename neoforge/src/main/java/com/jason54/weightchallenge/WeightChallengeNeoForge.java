package com.jason54.weightchallenge;

import com.jason54.weightchallenge.command.WeightCommands;
import com.jason54.weightchallenge.network.WeightSyncPayload;
import com.jason54.weightchallenge.platform.WeightPlatform;
import com.jason54.weightchallenge.weight.WeightData;
import com.jason54.weightchallenge.weight.WeightLogic;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class WeightChallengeNeoForge {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<WeightData>> PLAYER_WEIGHT = ATTACHMENTS.register(
            "player_weight",
            () -> AttachmentType.builder(() -> new WeightData(com.jason54.weightchallenge.config.WeightConfig.get().defaultWeightKg))
                    .serialize(WeightData.MAP_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public WeightChallengeNeoForge(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(this::registerNetworking);

        WeightChallenge.bootstrap(
                new WeightPlatform.WeightAccess() {
                    @Override
                    public double getWeight(ServerPlayer player) {
                        return player.getData(PLAYER_WEIGHT.get()).getWeightKg();
                    }

                    @Override
                    public void setWeight(ServerPlayer player, double weightKg, boolean sync) {
                        player.getData(PLAYER_WEIGHT.get()).setWeightKg(weightKg);
                        if (sync) {
                            syncWeight(player);
                        }
                    }
                },
                FMLPaths.CONFIGDIR.get(),
                WeightChallengeNeoForge::syncWeight
        );

        NeoForge.EVENT_BUS.addListener(this::onCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onAttack);
        registerBlockBreakListener();
    }

    /**
     * 1.21.11: {@code BlockEvent.BreakEvent}; 26.x: {@code BreakBlockEvent}.
     * Avoids {@code BlockDropsEvent}, which skips creative / no-drop breaks.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerBlockBreakListener() {
        Class<? extends Event> eventClass;
        try {
            eventClass = (Class<? extends Event>) Class.forName(
                    "net.neoforged.neoforge.event.level.block.BreakBlockEvent");
        } catch (ClassNotFoundException ignored) {
            try {
                eventClass = (Class<? extends Event>) Class.forName(
                        "net.neoforged.neoforge.event.level.BlockEvent$BreakEvent");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("No NeoForge block-break event found", e);
            }
        }

        Method getPlayer;
        try {
            getPlayer = eventClass.getMethod("getPlayer");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Block-break event missing getPlayer()", e);
        }

        Consumer handler = event -> {
            try {
                Object player = getPlayer.invoke(event);
                if (player instanceof ServerPlayer serverPlayer && !serverPlayer.level().isClientSide()) {
                    WeightLogic.onBlockMined(serverPlayer);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to handle block break", e);
            }
        };
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, eventClass, handler);
    }

    private void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToClient(WeightSyncPayload.TYPE, WeightSyncPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> com.jason54.weightchallenge.client.WeightHud.setClientWeight(payload.weightKg()))
        );
    }

    private void onCommands(RegisterCommandsEvent event) {
        WeightCommands.register(event.getDispatcher());
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WeightLogic.onServerPlayerTick(serverPlayer);
        }
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WeightLogic.onPlayerLogin(serverPlayer);
        }
    }

    private void onAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WeightLogic.onAttack(serverPlayer);
        }
    }

    static void syncWeight(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new WeightSyncPayload(WeightPlatform.getWeight(player)));
    }
}
