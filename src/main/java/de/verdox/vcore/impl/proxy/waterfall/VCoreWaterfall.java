package de.verdox.vcore.impl.proxy.waterfall;

import de.verdox.vcore.api.core.network.NetworkConfig;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import de.verdox.vcore.impl.proxy.listener.PlayerListener;
import io.netty.util.concurrent.DefaultThreadFactory;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class VCoreWaterfall extends Plugin {
    private static ProxyPlatform platform;
    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new DefaultThreadFactory("VCore-ThreadPool"));
    private NetworkConfig networkConfig;

    @Override
    public void onLoad() {
        //TODO: Config zum auslesen von dem ganzen Scheiß
        platform = new WaterfallPlatform(this);
        networkConfig = new NetworkConfig(Path
                .of(getDataFolder() + File.separator + "config.yml")
                .toFile());
        VCoreNetwork.startNetwork(platform, networkConfig, scheduledExecutorService);
    }

    @Override
    public void onEnable() {
        new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        VCoreNetwork.getInstance().getPipeline().saveAll();
    }
}
