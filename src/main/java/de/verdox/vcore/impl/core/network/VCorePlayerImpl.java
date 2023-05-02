package de.verdox.vcore.impl.core.network;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vcore.api.core.network.messages.updates.*;
import de.verdox.vcore.api.core.network.platform.types.*;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.ResponseCollector;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import de.verdox.vpipeline.api.pipeline.annotations.DataStorageIdentifier;
import de.verdox.vpipeline.api.pipeline.annotations.PipelineDataProperties;
import de.verdox.vpipeline.api.pipeline.core.Pipeline;
import de.verdox.vpipeline.api.pipeline.datatypes.PipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import de.verdox.vpipeline.api.pipeline.enums.DataContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@DataStorageIdentifier(identifier = "VCorePlayer")
@PipelineDataProperties(dataContext = DataContext.CACHE_ONLY)
public class VCorePlayerImpl extends PipelineData implements VCorePlayer {
    private transient final MessagingService messagingService;
    public ServerLocation currentServerLocation;
    public String displayName;


    public VCorePlayerImpl(@NotNull Pipeline pipeline, @NotNull UUID objectUUID) {
        super(pipeline, objectUUID);
        messagingService = VCoreNetwork.getInstance().getMessagingService();
    }

    //TODO: OnQuit and onConnect -> VCoreOfflinePlayer Data saven und loaden.


    @Override
    public void onSync(String dataBeforeSync) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public VCoreServer getCurrentServer() {
        return currentServerLocation.server();
    }

    @Override
    public GameLocation getGameLocation() {
        return currentServerLocation.gameLocation();
    }

    @Override
    public ServerLocation getServerLocation() {
        return currentServerLocation;
    }

    @Override
    public String getName() {
        return displayName;
    }

    public VCorePlayerImpl setCurrentServerLocation(ServerLocation currentServerLocation) {
        this.currentServerLocation = currentServerLocation;
        return this;
    }

    public VCorePlayerImpl setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    private CompletableFuture<Boolean> completeFuture(ResponseCollector<Update.UpdateCompletion> responseCollector) {
        var future = new CompletableFuture<Boolean>();
        responseCollector.whenResponseReceived((updateCompletion, throwable) -> {
            if (updateCompletion.equals(Update.UpdateCompletion.DONE))
                future.complete(true);
            else if (updateCompletion.equals(Update.UpdateCompletion.CANCELLED))
                future.complete(false);
        });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> teleport(GameLocation gameLocation, TeleportCause teleportCause) {
        Objects.requireNonNull(gameLocation);
        Objects.requireNonNull(teleportCause);

        return completeFuture(VCoreNetwork
                .getInstance()
                .getMessagingService()
                .sendInstruction(TeleportPlayerUpdate.class, teleportPlayerUpdate -> {
                    teleportPlayerUpdate.playerUUID = getObjectUUID();
                    teleportPlayerUpdate.teleportCause = teleportCause;
                    teleportPlayerUpdate.gameLocation = gameLocation;
                }));
    }

    //TODO: Alles mit Futures machen, man richtig drauf waiten kann.

    @Override
    public CompletableFuture<Boolean> teleport(ServerLocation serverLocation, TeleportCause teleportCause) {
        if (getCurrentServer().isServer(serverLocation.server())) {
            return teleport(serverLocation.gameLocation(), teleportCause);
        }
        var future = new CompletableFuture<Boolean>();
        if (VCoreNetwork.getInstance().getPlatform().isConnectedToProxyNetwork()) {
            messagingService.sendInstruction(SwitchServerUpdate.class, switchServerUpdate -> {
                                switchServerUpdate.playerUUID = getObjectUUID();
                                switchServerUpdate.serverName = serverLocation.server().getName().join();
                            }).askForValue(updateCompletion -> updateCompletion.equals(Update.UpdateCompletion.DONE))
                            .thenRun(() -> {
                                teleport(serverLocation.gameLocation(), teleportCause).whenComplete((aBoolean, throwable) -> future.complete(aBoolean));
                            });
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> teleportTo(DataReference<VCorePlayer> target, TeleportCause teleportCause) {
        var future = new CompletableFuture<Boolean>();
        target.load()
              .whenComplete((vCorePlayerPipelineLock, throwable) -> {
                  var targetServerLocation = vCorePlayerPipelineLock.getter(VCorePlayer::getServerLocation);
                  teleport(targetServerLocation, teleportCause).whenComplete((aBoolean, throwable1) -> future.complete(aBoolean));
              });
        return future;
    }

    @Override
    public CompletableFuture<Boolean> switchServer(VCoreServer vCoreServer) {
        if (!VCoreNetwork.getInstance().getPlatform().isConnectedToProxyNetwork())
            return CompletableFuture.completedFuture(false);
        return completeFuture(messagingService.sendInstruction(SwitchServerUpdate.class, switchServerUpdate -> {
            switchServerUpdate.playerUUID = getObjectUUID();
            switchServerUpdate.serverName = vCoreServer.getName().join();
        }));
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(String message, PlayerMessageType playerMessageType) {
        return completeFuture(messagingService.sendInstruction(SendMessageUpdate.class, sendMessageUpdate -> {
            sendMessageUpdate.playerUUID = getObjectUUID();
            sendMessageUpdate.message = message;
            sendMessageUpdate.messageType = playerMessageType;
        }));
    }

    @Override
    public CompletableFuture<Boolean> setHealth(double health) {
        return completeFuture(messagingService.sendInstruction(SetHealthUpdate.class, setHealthUpdate -> {
            setHealthUpdate.playerUUID = getObjectUUID();
            setHealthUpdate.health = health;
        }));
    }

    @Override
    public CompletableFuture<Boolean> setFood(int food) {
        return completeFuture(messagingService.sendInstruction(SetFoodUpdate.class, setFoodUpdate -> {
            setFoodUpdate.playerUUID = getObjectUUID();
            setFoodUpdate.foodValue = food;
        }));
    }

    @Override
    public CompletableFuture<Boolean> clearInventory() {
        return completeFuture(messagingService.sendInstruction(ClearInventoryUpdate.class, clearInventoryUpdate -> clearInventoryUpdate.playerUUID = getObjectUUID()));
    }

    @Override
    public CompletableFuture<Boolean> kickPlayer() {
        return completeFuture(messagingService.sendInstruction(KickPlayerUpdate.class, kickPlayerUpdate -> kickPlayerUpdate.playerUUID = getObjectUUID()));
    }

    @Override
    public CompletableFuture<Boolean> killPlayer() {
        return completeFuture(messagingService.sendInstruction(KillPlayerUpdate.class, killPlayerUpdate -> killPlayerUpdate.playerUUID = getObjectUUID()));
    }

    @Override
    public CompletableFuture<Boolean> setGameMode(PlayerGameMode playerGameMode) {
        return completeFuture(messagingService.sendInstruction(SetGameModeUpdate.class, setGameModeUpdate -> {
            setGameModeUpdate.playerUUID = getObjectUUID();
            setGameModeUpdate.playerGameMode = playerGameMode;
        }));
    }

    @Override
    public String toString() {
        return "VCorePlayerImpl{" +
                "messagingService=" + messagingService +
                ", currentServerLocation=" + currentServerLocation +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
