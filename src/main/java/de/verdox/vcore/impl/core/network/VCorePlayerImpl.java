package de.verdox.vcore.impl.core.network;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.messages.updates.*;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.pipeline.annotations.DataStorageIdentifier;
import de.verdox.vpipeline.api.pipeline.annotations.PipelineDataProperties;
import de.verdox.vpipeline.api.pipeline.core.Pipeline;
import de.verdox.vpipeline.api.pipeline.datatypes.PipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import de.verdox.vpipeline.api.pipeline.enums.DataContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@DataStorageIdentifier(identifier = "VCorePlayer")
@PipelineDataProperties(dataContext = DataContext.CACHE_ONLY)
public class VCorePlayerImpl extends PipelineData implements VCorePlayer {
    private transient final MessagingService messagingService;
    private ServerLocation currentServerLocation;
    private String displayName;

    public VCorePlayerImpl(@NotNull Pipeline pipeline, @NotNull UUID objectUUID) {
        super(pipeline, objectUUID);
        messagingService = VCoreNetwork.getInstance().getMessagingService();
    }

    //TODO: OnQuit and onConnect -> VCoreOfflinePlayer Data saven und loaden.

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

    @Override
    public void teleport(GameLocation gameLocation) {
        VCoreNetwork
                .getInstance()
                .getMessagingService()
                .sendInstruction(TeleportPlayerUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID(), gameLocation));
    }

    @Override
    public void teleport(ServerLocation serverLocation) {
        if (getCurrentServer().isServer(serverLocation.server())) {
            teleport(serverLocation.gameLocation());
            return;
        }
        if (VCoreNetwork.getInstance().getPlatform().isConnectedToProxyNetwork()) {
            messagingService
                    .sendInstruction(SwitchServerUpdate.class, booleanInstruction -> booleanInstruction.withData(serverLocation
                            .server()
                            .remoteParticipantReference()
                            .uuid(), serverLocation.server().getName().join()))
                    .askForValue(aBoolean -> aBoolean)
                    .thenRun(() -> teleport(serverLocation.gameLocation()));
        }
    }

    @Override
    public void teleportTo(DataReference<VCorePlayer> target) {
        target.load()
              .whenComplete((vCorePlayerPipelineLock, throwable) -> {
                  var targetServerLocation = vCorePlayerPipelineLock.getter(VCorePlayer::getServerLocation);
                  teleport(targetServerLocation);
              });
    }

    @Override
    public void sendMessage(String message, PlayerMessageType playerMessageType) {
        messagingService.sendInstruction(SendMessageUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID(), playerMessageType, message));
    }

    @Override
    public void setHealth(double health) {
        messagingService.sendInstruction(SetHealthUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID(), health));
    }

    @Override
    public void setFood(int food) {
        messagingService.sendInstruction(SetFoodUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID(), food));
    }

    @Override
    public void clearInventory() {
        messagingService.sendInstruction(ClearInventoryUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID()));
    }

    @Override
    public void kickPlayer() {
        messagingService.sendInstruction(KickPlayerUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID()));
    }

    @Override
    public void killPlayer() {
        messagingService.sendInstruction(KillPlayerUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID()));
    }

    @Override
    public void setGameMode(PlayerGameMode playerGameMode) {
        messagingService.sendInstruction(SetGameModeUpdate.class, booleanInstruction -> booleanInstruction.withData(getObjectUUID(), playerGameMode));
    }
}
