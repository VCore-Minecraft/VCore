package de.verdox.vcore.impl.gameserver.paper.listener;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class QueryListener<T extends Event, R> extends VCorePaperListener {
    private final CompletableFuture<R> result = new CompletableFuture<>();
    private final R requestedResult;
    private final Function<T, R> queryLogic;

    public QueryListener(JavaPlugin javaPlugin, R requestedResult, Function<T, R> queryLogic) {
        super(javaPlugin);
        this.requestedResult = requestedResult;
        this.queryLogic = queryLogic;
    }

    public QueryListener(JavaPlugin javaPlugin, Function<T, R> queryLogic) {
        this(javaPlugin, null, queryLogic);
    }

    public CompletableFuture<R> getResult() {
        return result;
    }

    @EventHandler
    private void listener(T event) {
        var result = queryLogic.apply(event);
        if (result == null || (requestedResult != null && !result.equals(requestedResult)))
            return;
        completeQuery(result);
        HandlerList.unregisterAll(this);
    }

    private void completeQuery(R result) {
        this.result.complete(result);
    }
}
