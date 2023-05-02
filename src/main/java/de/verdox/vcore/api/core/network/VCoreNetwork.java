package de.verdox.vcore.api.core.network;

import com.google.gson.GsonBuilder;
import de.verdox.vcore.api.core.network.data.VCoreOfflinePlayer;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vcore.api.core.network.messages.updates.*;
import de.verdox.vcore.api.core.network.platform.Platform;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vcore.impl.core.network.VCoreOfflinePlayerImpl;
import de.verdox.vcore.impl.core.network.VCorePlayerImpl;
import de.verdox.vpipeline.api.NetworkParticipant;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.pipeline.core.Pipeline;
import de.verdox.vpipeline.api.pipeline.core.PipelineLock;
import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class VCoreNetwork {
    private static VCoreNetwork instance;
    private final Platform platform;
    private final Pipeline pipeline;
    private final MessagingService messagingService;
    private final NetworkParticipant networkParticipant;
    private final Set<Class<? extends IPipelineData>> playerDataClasses = new HashSet<>();

    public static void startNetwork(@NotNull Platform platform, @NotNull NetworkConfig networkConfig, @NotNull ScheduledExecutorService service, @Nullable Consumer<GsonBuilder> gsonBuilderConsumer) {
        Objects.requireNonNull(platform);
        Objects.requireNonNull(networkConfig);
        try {
            if (instance != null) {

                throw new IllegalStateException("Network can only be instantiated once");
            }
            instance = new VCoreNetwork(platform, networkConfig.constructParticipant(service, gsonBuilderConsumer));
        } catch (Throwable e) {
            e.printStackTrace();
            platform.shutdown();
        }
    }

    public void registerPlayerDataClass(Class<? extends IPipelineData> type) {
        playerDataClasses.add(type);
    }

    public Set<Class<? extends IPipelineData>> getPlayerDataClasses() {
        return playerDataClasses;
    }

    public static void startNetwork(@NotNull Platform platform, @NotNull NetworkConfig networkConfig, @NotNull ScheduledExecutorService service) {
        startNetwork(platform, networkConfig, service, null);
    }

    private VCoreNetwork(Platform platform, NetworkParticipant networkParticipant) {
        this.platform = platform;
        this.pipeline = networkParticipant.pipeline();
        this.messagingService = networkParticipant.messagingService();
        this.networkParticipant = networkParticipant;

        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4000, TeleportPlayerUpdate.class, () -> new TeleportPlayerUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4001, SwitchServerUpdate.class, () -> new SwitchServerUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4002, BroadcastMessageUpdate.class, () -> new BroadcastMessageUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4003, SetFoodUpdate.class, () -> new SetFoodUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4004, SetHealthUpdate.class, () -> new SetHealthUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4005, ClearInventoryUpdate.class, () -> new ClearInventoryUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4006, KickPlayerUpdate.class, () -> new KickPlayerUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4007, KillPlayerUpdate.class, () -> new KillPlayerUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4008, SetGameModeUpdate.class, () -> new SetGameModeUpdate(UUID.randomUUID()));
        this.messagingService
                .getMessageFactory()
                .registerInstructionType(4009, SendMessageUpdate.class, () -> new SendMessageUpdate(UUID.randomUUID()));

        this.pipeline.getDataRegistry().registerType(VCorePlayerImpl.class);
        this.pipeline.getDataRegistry().registerType(VCoreOfflinePlayerImpl.class);

        this.pipeline.loadAllData(VCorePlayerImpl.class);
        this.pipeline.loadAllData(VCoreOfflinePlayerImpl.class);
    }

    public CompletableFuture<PipelineLock<VCorePlayer>> getPlayer(UUID uuid) {
        return this.pipeline.load(VCorePlayerImpl.class, uuid);
    }

    public DataReference<VCorePlayer> getPlayerReference(UUID uuid) {
        return this.pipeline.createDataReference(VCorePlayerImpl.class, uuid);
    }

    public CompletableFuture<Void> addPlayerToCache(UUID uuid, String displayName, @Nullable ServerLocation serverLocation) {
        var future = new CompletableFuture<Void>();
        this.pipeline.loadOrCreate(VCorePlayerImpl.class, uuid).whenComplete((vCorePlayerPipelineLock, throwable) -> {
            vCorePlayerPipelineLock.performWriteOperation(vCorePlayer -> {
                vCorePlayer.setDisplayName(displayName);
                if (serverLocation != null)
                    vCorePlayer.setCurrentServerLocation(serverLocation);
            });
            future.complete(null);
        });
        return future;
    }

    public void broadCastMessage(PlayerMessageType playerMessageType, String message) {
        messagingService.sendInstruction(BroadcastMessageUpdate.class, broadcastMessageUpdate -> {
            broadcastMessageUpdate.playerMessageType = playerMessageType;
            broadcastMessageUpdate.message = message;
        });
    }

    public CompletableFuture<Boolean> removePlayerFromCache(UUID uuid) {
        return pipeline.delete(VCorePlayerImpl.class, uuid);
    }

    public VCoreServer getAsServer() {
        return getServer(networkParticipant.getIdentifier());
    }

    public VCoreServer getServer(@NotNull String serverName) {
        Objects.requireNonNull(serverName);
        return new VCoreServer(networkParticipant.getOnlineNetworkClient(serverName));
    }

    public CompletableFuture<Set<DataReference<VCorePlayer>>> getOnlinePlayers() {
        return this.pipeline.loadAllData(VCorePlayerImpl.class);
    }

    public CompletableFuture<Set<DataReference<VCoreOfflinePlayer>>> getOfflinePlayers() {
        return this.pipeline.loadAllData(VCoreOfflinePlayerImpl.class);
    }

    public CompletableFuture<Set<VCoreServer>> getServers() {
        return networkParticipant
                .getOnlineNetworkClients()
                .thenApply(dataReferences -> dataReferences.stream().map(VCoreServer::new).collect(Collectors.toSet()));
    }

    public Platform getPlatform() {
        return platform;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public MessagingService getMessagingService() {
        return messagingService;
    }

    public NetworkParticipant getNetworkParticipant() {
        return networkParticipant;
    }

    public static VCoreNetwork getInstance() {
        if (instance == null)
            throw new IllegalStateException("VCoreNetwork was not started yet");
        return instance;
    }
}
