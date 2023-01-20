package de.verdox.vcore.api.core.command;

import de.verdox.vcore.api.core.PlatformDependentImplementation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @param <T> Platform plugin
 * @param <S> Command Sender implemented by platform
 * @param <P> Player class implemented by platform
 * @param <C> CommandCallback implemented for a certain platform
 */
public abstract class VCoreCommand<T, S, P, I extends CommandSenderInfo<T,S,P>, C extends CommandCallback<T, S, P, I>> extends PlatformDependentImplementation<T> {
    protected final String commandName;
    protected final List<C> commandCallbacks = new ArrayList<>();

    public VCoreCommand(@NotNull T plugin, @NotNull String commandName) {
        super(plugin);
        Objects.requireNonNull(commandName);
        this.commandName = commandName;
        registerCommandOnPlatform();
    }

    protected abstract void registerCommandOnPlatform();

    public C addCommandCallback(String... commandPath) {
        C callback = instantiateCommandCallback(plugin, commandPath);
        commandCallbacks.add(Objects.requireNonNull(callback));
        return callback;
    }

    protected abstract C instantiateCommandCallback(T plugin, String[] commandPath);

    public String[] suggestEnum(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }
}
