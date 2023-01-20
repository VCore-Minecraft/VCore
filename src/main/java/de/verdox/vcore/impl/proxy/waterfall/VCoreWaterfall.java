package de.verdox.vcore.impl.proxy.waterfall;

import de.verdox.vcore.api.core.network.NetworkConfig;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class VCoreWaterfall extends Plugin {

    private static final ProxyPlatform platform = new WaterfallPlatform();

    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new DefaultThreadFactory("VCore-ThreadPool"));
    private NetworkConfig networkConfig;

    @Override
    public void onLoad() {
        //TODO: Config zum auslesen von dem ganzen Scheiß
        networkConfig = new NetworkConfig(Path
                .of(getDataFolder() + File.separator + "config.yml")
                .toFile());
        VCoreNetwork.startNetwork(platform, networkConfig, scheduledExecutorService);
    }
}
