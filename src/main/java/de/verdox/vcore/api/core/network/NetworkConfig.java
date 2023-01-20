package de.verdox.vcore.api.core.network;

import com.google.gson.GsonBuilder;
import de.verdox.vpipeline.api.NetworkParticipant;
import de.verdox.vpipeline.api.VNetwork;
import de.verdox.vpipeline.api.messaging.builder.MessagingServiceBuilder;
import de.verdox.vpipeline.api.pipeline.builder.PipelineBuilder;
import de.verdox.vpipeline.api.pipeline.datatypes.SynchronizingService;
import de.verdox.vpipeline.api.pipeline.parts.GlobalCache;
import de.verdox.vpipeline.api.pipeline.parts.GlobalStorage;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class NetworkConfig {
    private final YamlConfiguration yamlConfiguration;
    private final File file;

    public NetworkConfig(File file) {
        this.file = file;
        if (!file.exists())
            file.getParentFile().mkdirs();
        try {
            file.createNewFile();

            yamlConfiguration = YamlConfiguration.loadConfiguration(file);

            infoSetup();

            pipelineCacheSetup();
            pipelineSynchronizerSetup();
            pipelineStorageSetup();
            messagingTransmitterSetup();

            yamlConfiguration.options().copyDefaults(true);
            yamlConfiguration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public NetworkParticipant constructParticipant(ScheduledExecutorService executorService, @Nullable Consumer<GsonBuilder> gsonBuilderConsumer) {
        var builder = VNetwork
                .getConstructionService()
                .createNetworkParticipant()
                .withExecutorService(executorService)
                .withPipeline(pipelineBuilder -> pipelineBuilder.withGson(gsonBuilderConsumer));
        if (yamlConfiguration.getBoolean("MessagingService.enable"))
            builder.withMessagingService(this::buildMessagingService);
        builder.withName(yamlConfiguration.getString("Info.Name"));
        var participant = builder.build();
        var server = new VCoreServer(participant.getAsNetworkClient());
        server.setServerAddress(yamlConfiguration.getString("Info.Address"));
        server.setServerPort(yamlConfiguration.getInt("Info.Port"));
        return participant;
    }

    private void buildPipeline(PipelineBuilder pipelineBuilder) {
        if (yamlConfiguration.getBoolean("Pipeline.Cache.enable")) {
            var cacheType = yamlConfiguration.getString("Pipeline.Cache.type");
            if (cacheType.equalsIgnoreCase("redis")) {

                pipelineBuilder.withGlobalCache(
                        GlobalCache.createRedisCache(yamlConfiguration.getBoolean("Pipeline.Cache.redis.useCluster"),
                                yamlConfiguration
                                        .getStringList("Pipeline.Cache.redis.addresses")
                                        .toArray(new String[0]),
                                yamlConfiguration.getString("Pipeline.Cache.redis.password")));

            } else
                throw new IllegalArgumentException(cacheType + " is not a valid cache type");
        }

        if (yamlConfiguration.getBoolean("Pipeline.DataSynchronizer.enable")) {
            var synchronizerType = yamlConfiguration.getString("Pipeline.DataSynchronizer.type");
            if (synchronizerType.equalsIgnoreCase("redis")) {
                pipelineBuilder.withSynchronizingService(
                        SynchronizingService.buildRedisService(yamlConfiguration.getBoolean("Pipeline.DataSynchronizer.redis.useCluster"),
                                yamlConfiguration
                                        .getStringList("Pipeline.DataSynchronizer.redis.addresses")
                                        .toArray(new String[0]),
                                yamlConfiguration.getString("Pipeline.DataSynchronizer.redis.password")));
            } else
                throw new IllegalArgumentException(synchronizerType + " is not a valid synchronizer type");
        }

        if (yamlConfiguration.getBoolean("Pipeline.GlobalStorage.enable")) {
            var storageType = yamlConfiguration.getString("Pipeline.GlobalStorage.type");
            if (storageType.equalsIgnoreCase("json"))
                pipelineBuilder.withGlobalStorage(GlobalStorage.buildJsonStorage(Path.of(file
                        .getParentFile()
                        .getAbsolutePath() + File.pathSeparator + yamlConfiguration.getString("Pipeline.GlobalStorage.type.json.path"))));
            else if (storageType.equalsIgnoreCase("mongodb") || storageType.equalsIgnoreCase("mongo")) {

                var host = yamlConfiguration.getString("Pipeline.GlobalStorage.mongodb.host");
                var port = yamlConfiguration.getInt("Pipeline.GlobalStorage.mongodb.port");
                var database = yamlConfiguration.getString("Pipeline.GlobalStorage.mongodb.database");
                var user = yamlConfiguration.getString("Pipeline.GlobalStorage.mongodb.user");
                var password = yamlConfiguration.getString("Pipeline.GlobalStorage.mongodb.password");
                var url = yamlConfiguration.getString("Pipeline.GlobalStorage.mongodb.url");

                if (url != null && !url.isEmpty())
                    pipelineBuilder.withGlobalStorage(GlobalStorage.buildMongoDBStorage(url));
                else
                    pipelineBuilder.withGlobalStorage(GlobalStorage.buildMongoDBStorage(host, database, port, user, password));
            } else if (storageType.equalsIgnoreCase("mysql") || storageType.equalsIgnoreCase("sql")) {
                throw new IllegalStateException("MySQL Storage not implemented yet.");
            } else
                throw new IllegalArgumentException(storageType + " is not a valid storage type");
        }
    }

    private void buildMessagingService(MessagingServiceBuilder messagingServiceBuilder) {
        if (yamlConfiguration.getBoolean("MessagingService.enable")) {
            var messagingType = yamlConfiguration.getString("MessagingService.type");
            if (messagingType.equalsIgnoreCase("redis")) {
                messagingServiceBuilder.useRedisTransmitter(yamlConfiguration.getBoolean("MessagingService.redis.useCluster"),
                        yamlConfiguration
                                .getStringList("MessagingService.redis.addresses")
                                .toArray(new String[0]),
                        yamlConfiguration.getString("MessagingService.redis.password"));
            } else
                throw new IllegalArgumentException(messagingType + " is not a valid transmitter type");
        }
    }

    private void infoSetup() {
        yamlConfiguration.addDefault("Info.Name", "server");
        yamlConfiguration.addDefault("Info.Address", "localhost");
        yamlConfiguration.addDefault("Info.Port", 25565);
    }

    private void pipelineCacheSetup() {
        yamlConfiguration.addDefault("Pipeline.Cache.enable", false);
        yamlConfiguration.addDefault("Pipeline.Cache.type", "redis");
        // Redis - Cache Settings
        yamlConfiguration.addDefault("Pipeline.Cache.redis.useCluster", false);
        yamlConfiguration.addDefault("Pipeline.Cache.redis.addresses", List.of("redis://localhost:6379"));
        yamlConfiguration.addDefault("Pipeline.Cache.redis.password", "");
    }


    private void pipelineSynchronizerSetup() {
        yamlConfiguration.addDefault("Pipeline.DataSynchronizer.enable", false);
        yamlConfiguration.addDefault("Pipeline.DataSynchronizer.type", "redis");
        // Redis - Synchronizer Settings
        yamlConfiguration.addDefault("Pipeline.DataSynchronizer.redis.useCluster", false);
        yamlConfiguration.addDefault("Pipeline.DataSynchronizer.redis.addresses", List.of("redis://localhost:6379"));
        yamlConfiguration.addDefault("Pipeline.DataSynchronizer.redis.password", "");
    }

    private void pipelineStorageSetup() {
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.enable", true);
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.type", "json");
        // Json - Storage Settings
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.json.path", "");
        // Mongo - Storage Settings
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.host", "127.0.0.1");
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.port", "27017");
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.database", "vcore");
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.user", "");
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.password", "");
        yamlConfiguration.addDefault("Pipeline.GlobalStorage.mongodb.url", "");
    }

    private void messagingTransmitterSetup() {
        yamlConfiguration.addDefault("MessagingService.enable", false);
        yamlConfiguration.addDefault("MessagingService.type", "redis");
        yamlConfiguration.addDefault("MessagingService.redis.useCluster", false);
        yamlConfiguration.addDefault("MessagingService.redis.addresses", List.of("redis://localhost:6379"));
        yamlConfiguration.addDefault("MessagingService.redis.password", "");
    }
}
