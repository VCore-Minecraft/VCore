package de.verdox.vcore.api.core.command;

import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public record CommandParameters(List<Object> parameters) {

    public <T> T getObject(@NonNegative int index, @NotNull Class<? extends T> type) {
        return type.cast(parameters.get(index));
    }

    public <R extends IPipelineData, T extends DataReference<R>> T getReference(@NonNegative int index, @NotNull Class<? extends R> type) {
        return (T) DataReference.class.cast(parameters.get(index));
    }

    public <E extends Enum<?>> E getEnum(@NonNegative int index, Class<? extends E> type) {
        String input = getObject(index, String.class);
        return Arrays
                .stream(type.getEnumConstants())
                .filter(anEnum -> anEnum.name().toUpperCase(Locale.ROOT).equals(input.toUpperCase()))
                .findAny()
                .orElse(null);
    }

    public Class<?> getType(@NonNegative int index) {
        return parameters.get(index).getClass();
    }

    public int size() {
        return parameters.size();
    }
}
