package de.verdox.vcore.synchronization.pipeline.parts.manipulator.redis;

import de.verdox.vcore.synchronization.pipeline.datatypes.VCoreData;
import de.verdox.vcore.synchronization.pipeline.parts.local.LocalCache;
import de.verdox.vcore.synchronization.pipeline.parts.manipulator.DataSynchronizer;
import de.verdox.vcore.synchronization.pipeline.parts.manipulator.SynchronizingService;
import de.verdox.vcore.synchronization.redisson.RedisConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 08.02.2022 00:04
 */
public class RedisSynchronizingService implements SynchronizingService {

    private final LocalCache localCache;
    private final RedisConnection redisConnection;
    private final Map<Class<? extends VCoreData>, RedisDataSynchronizer> cache;

    public RedisSynchronizingService(@NotNull LocalCache localCache, @NotNull RedisConnection redisConnection) {
        this.localCache = localCache;
        this.redisConnection = redisConnection;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public DataSynchronizer getDataSynchronizer(VCoreData vCoreData) {
        cache.putIfAbsent(vCoreData.getClass(), new RedisDataSynchronizer(localCache, redisConnection, vCoreData.getClass()));
        return cache.get(vCoreData.getClass());
    }
}
