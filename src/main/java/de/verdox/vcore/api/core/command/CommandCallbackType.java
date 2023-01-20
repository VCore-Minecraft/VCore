package de.verdox.vcore.api.core.command;

import de.verdox.vcore.api.core.PlatformDependentImplementation;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vcore.impl.util.MojangAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @param <T> Platform plugin
 */
public abstract class CommandCallbackType<T> extends PlatformDependentImplementation<T> {
    protected final int index;

    CommandCallbackType(T plugin, int index) {
        super(plugin);
        this.index = index;
    }

    @Nullable
    public abstract List<String> suggest(String argument);

    public abstract String commandHelpPlaceholder();

    public static class CommandPath<T> extends CommandCallbackType<T> {
        protected final String commandPath;

        CommandPath(T plugin, int index, String commandPath) {
            super(plugin, index);
            this.commandPath = commandPath;
        }

        @Override
        public @Nullable List<String> suggest(String argument) {
            if (commandPath.contains(argument))
                return List.of(commandPath);
            return null;
        }

        @Override
        public String commandHelpPlaceholder() {
            return commandPath;
        }

        public String getCommandPath() {
            return commandPath;
        }

        @Override
        public String toString() {
            return commandPath;
        }
    }

    public static class CommandAskParameter<T> extends CommandCallbackType<T> {
        private final String parameterName;
        private final CommandAskType commandAskType;
        private final Supplier<List<String>> supplySuggested;

        CommandAskParameter(T plugin, int index, @NotNull String parameterName, @NotNull CommandAskType commandAskType, Supplier<List<String>> supplySuggested) {
            super(plugin, index);
            this.parameterName = parameterName;
            this.commandAskType = commandAskType;
            this.supplySuggested = supplySuggested;
        }

        public CommandAskType getCommandAskType() {
            return commandAskType;
        }

        @Override
        public @Nullable List<String> suggest(String argument) {
            List<String> suggested = new LinkedList<>(supplySuggested.get());

            if (suggested.isEmpty()) {
                switch (commandAskType) {
                    case NUMBER, POSITIVE_NUMBER -> suggested.add("1");
                    case POSITIVE_NUMBER_AND_ZERO -> suggested.add("0");
                    case NEGATIVE_NUMBER, NEGATIVE_NUMBER_AND_ZERO -> suggested.add("-1");
                    case BOOLEAN -> suggested.addAll(List.of("true", "false"));
                    case PLAYER -> VCoreNetwork
                            .getInstance()
                            .getOnlinePlayers()
                            .join()
                            .stream()
                            .map(vCorePlayerDataReference -> vCorePlayerDataReference
                                    .loadOrCreate()
                                    .join()
                                    .getter(VCorePlayer::getName))
                            .forEach(suggested::add);
                    case GAMESERVER -> VCoreNetwork
                            .getInstance()
                            .getServers()
                            .join()
                            .parallelStream()
                            .map(vCoreServer -> vCoreServer.getName().join())
                            .forEach(suggested::add);
                }
            }

            return suggested
                    .stream()
                    .filter(suggestedArgument -> suggestedArgument
                            .toLowerCase(Locale.ROOT)
                            .startsWith(argument.toLowerCase(Locale.ROOT))).toList();
        }

        @Override
        public String commandHelpPlaceholder() {
            return "§8<§e" + parameterName + "§8>";
        }
    }
}
