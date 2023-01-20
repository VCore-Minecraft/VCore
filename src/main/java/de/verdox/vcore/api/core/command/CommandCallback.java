package de.verdox.vcore.api.core.command;

import de.verdox.vcore.api.core.PlatformDependentImplementation;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.VCoreServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @param <T> Platform plugin
 * @param <S> Command Sender implemented by platform
 * @param <P> Player class implemented by platform
 * @param <C> CommandSenderImplementation
 */
public abstract class CommandCallback<T, S, P, C extends CommandSenderInfo<T, S, P>> extends PlatformDependentImplementation<T> {
    protected final List<CommandCallbackType<T>> callbackTypes = new ArrayList<>();
    private final boolean isProxyNetwork;
    private final VCoreServer currentServer;
    private boolean restAsString;
    private BiConsumer<C, CommandParameters> providedArguments;
    private String neededPermission;
    private CommandExecutorType commandExecutorType;

    public CommandCallback(@NotNull T plugin, @NotNull String... commandPath) {
        super((plugin));
        Objects.requireNonNull(commandPath);

        for (String path : commandPath) {
            if (path.isEmpty())
                continue;
            int index = callbackTypes.size();
            callbackTypes.add(new CommandCallbackType.CommandPath<>(plugin, index, path));
        }

        isProxyNetwork = VCoreNetwork.getInstance().getPlatform().isConnectedToProxyNetwork();
        currentServer = VCoreNetwork.getInstance().getAsServer();
    }

    public String getSuggested(VCoreCommand<?, ?, ?, ?, ?> command) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("§7/§b").append(command.commandName).append(" ");
        for (CommandCallbackType<T> callbackInfo : callbackTypes)
            stringBuilder.append(callbackInfo.commandHelpPlaceholder()).append(" ");
        return stringBuilder.toString();
    }

    public String getNeededPermission() {
        return neededPermission;
    }

    protected abstract boolean hasSenderPermission(S sender, String permissionNode);

    protected abstract boolean sendMessage(S sender, String message);

    protected abstract boolean isSenderPlayer(S sender);

    protected abstract boolean isSenderConsole(S sender);

    protected abstract List<String> getOnlinePlayerNames();

    public CommandCallback<T, S, P, C> addCommandPath(@NotNull String commandPath) {
        if (commandPath.isEmpty())
            return this;
        int index = callbackTypes.size();
        callbackTypes.add(new CommandCallbackType.CommandPath<T>(plugin, index, commandPath));
        return this;
    }

    //TODO: Optional argument
    public CommandCallback<T, S, P, C> askFor(@NotNull String name, @NotNull CommandAskType commandAskType, @NotNull String... suggested) {
        return askFor(name, commandAskType, () -> Arrays.asList(suggested));
    }

    public CommandCallback<T, S, P, C> askFor(@NotNull String name, @NotNull CommandAskType commandAskType, @NotNull Supplier<List<String>> supplier) {
        int index = callbackTypes.size();
        callbackTypes.add(new CommandCallbackType.CommandAskParameter<>(plugin, index, name, commandAskType, supplier));
        if (commandAskType.equals(CommandAskType.REST_OF_INPUT))
            restAsString = true;
        return this;
    }

    public CommandCallback<T, S, P, C> withPermission(@NotNull String permission) {
        this.neededPermission = permission;
        return this;
    }

    public CommandCallback<T, S, P, C> setExecutor(@NotNull CommandExecutorType commandExecutorType) {
        this.commandExecutorType = commandExecutorType;
        return this;
    }

    public CommandCallback<T, S, P, C> commandCallback(@NotNull BiConsumer<C, CommandParameters> providedArguments) {
        this.providedArguments = providedArguments;
        return this;
    }

    public List<String> suggest(S sender, String[] args) {
        List<String> suggested = new ArrayList<>();
        // First check if command path is right
        for (int i = 0; i < args.length - 1; i++) {
            String argument = args[i];
            if (callbackTypes.size() <= i)
                return suggested;
            CommandCallbackType<T> info = callbackTypes.get(i);
            if (info instanceof CommandCallbackType.CommandPath<T>) {
                if (!argument.equalsIgnoreCase(((CommandCallbackType.CommandPath<Object>) info).getCommandPath()))
                    return suggested;
            }
        }
        int currentArgument = Math.max(args.length - 1, 0);
        if (callbackTypes.size() <= currentArgument)
            return suggested;
        if (neededPermission != null && !neededPermission.isEmpty() && !hasSenderPermission(sender, neededPermission))
            return suggested;
        String argument = currentArgument < args.length ? args[currentArgument] : "";
        CommandCallbackType<T> info = callbackTypes.get(currentArgument);

        return info.suggest(argument);
    }

    public CallbackResponse onCommand(S sender, String[] args) {
        if (args.length != callbackTypes.size() && !restAsString)
            return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, false);
        if (restAsString && args.length == 0)
            return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, false);
        if (this.providedArguments == null)
            return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, false);

        List<Object> providedArguments = new LinkedList<>();

        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            CommandCallbackType<T> info = callbackTypes.get(i);
            if (info instanceof CommandCallbackType.CommandPath<T> commandPathType) {
                if (!argument.equalsIgnoreCase(commandPathType.getCommandPath()))
                    return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, false);
            } else if (info instanceof CommandCallbackType.CommandAskParameter<T> commandAskParameter) {
                //TODO: Wenn die Platform zu einem Proxy Netzwerk gehört, können alle Spieler angezeigt werden. Sonst nur die auf diesem Gameserver
                if (commandAskParameter.getCommandAskType().equals(CommandAskType.PLAYER)) {
                    var foundPlayer = VCoreNetwork
                            .getInstance()
                            .getOnlinePlayers()
                            .join()
                            .parallelStream()
                            .filter(vCorePlayerDataReference -> isProxyNetwork || vCorePlayerDataReference
                                    .load()
                                    .join()
                                    .getter(vCorePlayer -> vCorePlayer.getCurrentServer().isServer(currentServer)))
                            .filter(reference -> reference.load().join().getter(vCorePlayer -> vCorePlayer
                                    .getName()
                                    .equals(argument))).findAny().orElse(null);

                    if (foundPlayer == null) {
                        sendMessage(sender, "§cPlayer not found§7!");
                        sendMessage(sender, "§7-> §e" + argument);
                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                    providedArguments.add(foundPlayer);
                } else if (commandAskParameter.getCommandAskType().equals(CommandAskType.STRING)) {
                    providedArguments.add(argument);
                } else if (commandAskParameter.getCommandAskType().equals(CommandAskType.REST_OF_INPUT)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = i; j < args.length; j++)
                        stringBuilder.append(args[j]).append(" ");
                    providedArguments.add(stringBuilder.toString());
                    // Stop the Command Parsing when the rest of the input is parsed as one string
                    break;
                } else if (commandAskParameter.getCommandAskType().equals(CommandAskType.BOOLEAN)) {
                    if (!argument.equalsIgnoreCase("true") && !argument.equalsIgnoreCase("false")) {
                        sendMessage(sender, "§cWrong input§7!");
                        sendMessage(sender, "§7-> §e" + argument);
                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                    providedArguments.add(Boolean.parseBoolean(argument));
                } else if (commandAskParameter.getCommandAskType().name().contains("NUMBER")) {

                    try {
                        double number = Double.parseDouble(argument);

                        if (commandAskParameter.getCommandAskType().equals(CommandAskType.NEGATIVE_NUMBER)) {
                            if (number >= 0) {
                                sendMessage(sender, "§cOnly negative numbers allowed§7!");
                                sendMessage(sender, "§7-> §e" + argument);
                                return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                            }
                        } else if (commandAskParameter
                                .getCommandAskType()
                                .equals(CommandAskType.NEGATIVE_NUMBER_AND_ZERO)) {
                            if (number > 0) {
                                sendMessage(sender, "§cOnly negative numbers and zero allowed§7!");
                                sendMessage(sender, "§7-> §e" + argument);
                                return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                            }

                        } else if (commandAskParameter.getCommandAskType().equals(CommandAskType.POSITIVE_NUMBER)) {
                            if (number <= 0) {
                                sendMessage(sender, "§cOnly positive numbers allowed§7!");
                                sendMessage(sender, "§7-> §e" + argument);
                                return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                            }
                        } else if (commandAskParameter
                                .getCommandAskType()
                                .equals(CommandAskType.POSITIVE_NUMBER_AND_ZERO)) {
                            if (number < 0) {
                                sendMessage(sender, "§cOnly positive numbers and zero allowed§7!");
                                sendMessage(sender, "§7-> §e" + argument);
                                return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                            }
                        }

                        providedArguments.add(number);
                    } catch (NumberFormatException e) {
                        sendMessage(sender, "§cWrong input type§7!");

                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                } else if (commandAskParameter.getCommandAskType().equals(CommandAskType.GAMESERVER)) {
                    var server = VCoreNetwork
                            .getInstance()
                            .getServers()
                            .join()
                            .parallelStream()
                            .filter(vCoreServer -> vCoreServer.getName().join().equals(argument))
                            .findAny()
                            .orElse(null);

                    if (server == null) {
                        sendMessage(sender, "§cServer not found§7!");
                        sendMessage(sender, "§7-> §e" + argument);
                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                    providedArguments.add(server);
                }
                else if(commandAskParameter.getCommandAskType().equals(CommandAskType.ENUM))
                    providedArguments.add(argument);
            }
        }
        if (commandExecutorType != null) {
            switch (commandExecutorType) {
                case PLAYER -> {
                    if (!isSenderPlayer(sender)) {
                        sendMessage(sender, "§cCommand can only be executed by a player§7!");
                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                }
                case CONSOLE -> {
                    if (!isSenderConsole(sender)) {
                        sendMessage(sender, "§cCommand can only be executed by the console§7!");
                        return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
                    }
                }
            }
        }
        if (neededPermission != null && !neededPermission.isEmpty()) {
            if (!hasSenderPermission(sender, neededPermission)) {
                sendMessage(sender, "§cNo permissions§7!");
                return new CallbackResponse(CallbackResponse.ResponseType.FAILURE, true);
            }
        }
        this.providedArguments.accept(instantiateSenderInfo(sender), new CommandParameters(providedArguments));
        return new CallbackResponse(CallbackResponse.ResponseType.SUCCESS, false);
    }

    protected abstract C instantiateSenderInfo(S sender);

    public record CallbackResponse(
            ResponseType responseType,
            boolean errorMessageSent) {

        public enum ResponseType {
            SUCCESS,
            FAILURE
        }
    }
}
