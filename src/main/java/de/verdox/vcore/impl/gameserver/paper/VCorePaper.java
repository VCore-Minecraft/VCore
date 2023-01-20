package de.verdox.vcore.impl.gameserver.paper;

import de.verdox.vcore.api.core.network.NetworkConfig;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.impl.core.network.commands.NetworkCommands;
import de.verdox.vcore.impl.gameserver.paper.commands.PaperCommand;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VCorePaper extends JavaPlugin {
    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new DefaultThreadFactory("VCore-ThreadPool"));
    private NetworkConfig networkConfig;

    @Override
    public void onLoad() {
        //TODO: Config zum auslesen von dem ganzen Scheiß
        networkConfig = new NetworkConfig(Path
                .of(getDataFolder() + File.separator + "config.yml")
                .toFile());
        VCoreNetwork.startNetwork(new PaperMCPlatform(this), networkConfig, scheduledExecutorService);
    }

    @Override
    public void onEnable() {
        NetworkCommands.setupTeleportCommand(new PaperCommand(this, "teleport"));
        NetworkCommands.setupHealCommand(new PaperCommand(this, "heal"));
        NetworkCommands.setupFeedCommand(new PaperCommand(this, "feed"));
        NetworkCommands.setupGameModeCommand(new PaperCommand(this, "gameMode"));
        NetworkCommands.setupBroadcastCommand(new PaperCommand(this, "broadcast"));
        NetworkCommands.setupClearInventoryCommand(new PaperCommand(this, "clearInventory"));
        NetworkCommands.setupKickCommand(new PaperCommand(this, "kick"));
        NetworkCommands.setupKillCommand(new PaperCommand(this, "kill"));
        NetworkCommands.setupMessageCommand(new PaperCommand(this, "message"));
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        VCoreNetwork.getInstance().getNetworkParticipant().shutdown();
        scheduledExecutorService.shutdown();
        try {
            scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }
}
