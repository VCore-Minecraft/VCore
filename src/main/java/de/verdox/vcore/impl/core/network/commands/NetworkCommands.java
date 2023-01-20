package de.verdox.vcore.impl.core.network.commands;

import de.verdox.vcore.api.core.command.CommandAskType;
import de.verdox.vcore.api.core.command.CommandExecutorType;
import de.verdox.vcore.api.core.command.VCoreCommand;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vcore.api.core.network.messages.updates.BroadcastMessageUpdate;
import de.verdox.vcore.api.core.network.messages.updates.ClearInventoryUpdate;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;

import java.util.Arrays;

public class NetworkCommands {

    public static void setupBroadcastCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .askFor("message", CommandAskType.REST_OF_INPUT)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var message = commandParameters.getObject(0, String.class);
                        VCoreNetwork
                                .getInstance()
                                .getMessagingService()
                                .sendInstruction(BroadcastMessageUpdate.class, booleanInstruction -> booleanInstruction.withData(PlayerMessageType.CHAT, message));
                    });
    }

    public static void setupClearInventoryCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> commandSenderInfo
                            .getAsVCorePlayer()
                            .load()
                            .join()
                            .performReadOperation(VCorePlayer::clearInventory));

        vCoreCommand.addCommandCallback("")
                    .askFor("player", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);

                        target.load().join().performReadOperation(VCorePlayer::clearInventory);
                    });
    }

    public static void setupKickCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .askFor("player", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);

                        target.load().join().performReadOperation(VCorePlayer::kickPlayer);
                    });
    }

    public static void setupKillCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .askFor("player", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);
                        target.load().join().performReadOperation(VCorePlayer::killPlayer);
                    });
    }

    public static void setupMessageCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .askFor("player", CommandAskType.PLAYER)
                    .askFor("message", CommandAskType.REST_OF_INPUT)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);
                        var message = commandParameters.getObject(1, String.class);

                        target
                                .load()
                                .join()
                                .performReadOperation(vCorePlayer -> vCorePlayer.sendMessage(message, PlayerMessageType.CHAT));
                    });
    }

    public static void setupGameModeCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        var gamemodeSuggestion = Arrays
                .stream(PlayerGameMode.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .toList();

        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .askFor("gameMode", CommandAskType.ENUM, () -> gamemodeSuggestion)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var gameMode = commandParameters.getEnum(0, PlayerGameMode.class);
                        commandSenderInfo
                                .getAsVCorePlayer()
                                .load()
                                .join()
                                .performWriteOperation(vCorePlayer -> vCorePlayer.setGameMode(gameMode));
                    });

        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .askFor("gameMode", CommandAskType.ENUM, () -> gamemodeSuggestion)
                    .askFor("player", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var gameMode = commandParameters.getEnum(0, PlayerGameMode.class);
                        var target = commandParameters.getReference(1, VCorePlayer.class);
                        target
                                .load()
                                .join()
                                .performWriteOperation(vCorePlayer -> vCorePlayer.setGameMode(gameMode));
                    });
    }

    public static void setupFeedCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> commandSenderInfo
                            .getAsVCorePlayer()
                            .load()
                            .join()
                            .performWriteOperation(vCorePlayer -> vCorePlayer.setFood(20)));

        vCoreCommand.addCommandCallback("")
                    .askFor("target", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);
                        target.load()
                              .join()
                              .performWriteOperation(vCorePlayer -> vCorePlayer.setFood(20));
                    });
    }

    public static void setupHealCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand.addCommandCallback("")
                    .setExecutor(CommandExecutorType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> commandSenderInfo
                            .getAsVCorePlayer()
                            .load()
                            .join()
                            .performWriteOperation(vCorePlayer -> vCorePlayer.setHealth(20)));

        vCoreCommand.addCommandCallback("")
                    .askFor("target", CommandAskType.PLAYER)
                    .commandCallback((commandSenderInfo, commandParameters) -> {
                        var target = commandParameters.getReference(0, VCorePlayer.class);
                        target.load()
                              .join()
                              .performWriteOperation(vCorePlayer -> vCorePlayer.setHealth(20));
                    });
    }

    public static void setupTeleportCommand(VCoreCommand<?, ?, ?, ?, ?> vCoreCommand) {
        vCoreCommand
                .addCommandCallback("")
                .withPermission("vcore.teleport")
                .askFor("target", CommandAskType.PLAYER)
                .askFor("x", CommandAskType.NUMBER, "1", "2", "3")
                .askFor("y", CommandAskType.NUMBER, "1", "2", "3")
                .askFor("z", CommandAskType.NUMBER, "1", "2", "3")
                .commandCallback((commandSenderInfo, commandParameters) -> {
                    var reference = (DataReference<VCorePlayer>) commandParameters.getReference(0, VCorePlayer.class);
                    var x = commandParameters.getObject(1, Double.class);
                    var y = commandParameters.getObject(2, Double.class);
                    var z = commandParameters.getObject(3, Double.class);
                    reference.load().join().performWriteOperation(vCorePlayer -> vCorePlayer.teleport(x, y, z));
                });

        vCoreCommand
                .addCommandCallback("")
                .withPermission("vcore.teleport")
                .setExecutor(CommandExecutorType.PLAYER)
                .askFor("target", CommandAskType.PLAYER)
                .commandCallback((commandSenderInfo, commandParameters) -> {
                    var sender = commandSenderInfo.getAsVCorePlayer();
                    var reference = (DataReference<VCorePlayer>) commandParameters.getReference(0, VCorePlayer.class);
                    sender.load().join().performWriteOperation(vCorePlayer -> {
                        vCorePlayer.teleport(reference.load().join().getter(VCorePlayer::getServerLocation));
                    });
                });

        vCoreCommand
                .addCommandCallback("")
                .withPermission("vcore.teleport")
                .askFor("x", CommandAskType.NUMBER, "1", "2", "3")
                .askFor("y", CommandAskType.NUMBER, "1", "2", "3")
                .askFor("z", CommandAskType.NUMBER, "1", "2", "3")
                .commandCallback((commandSenderInfo, commandParameters) -> {
                    var target = commandSenderInfo.getAsVCorePlayer();
                    var x = commandParameters.getObject(0, Double.class);
                    var y = commandParameters.getObject(1, Double.class);
                    var z = commandParameters.getObject(2, Double.class);
                    target.load().join().performWriteOperation(vCorePlayer -> vCorePlayer.teleport(x, y, z));
                });
    }


}
