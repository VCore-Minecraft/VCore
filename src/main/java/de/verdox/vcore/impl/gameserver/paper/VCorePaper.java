package de.verdox.vcore.impl.gameserver.paper;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.verdox.vcore.api.core.network.NetworkConfig;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.impl.core.network.commands.NetworkCommands;
import de.verdox.vcore.impl.gameserver.paper.commands.PaperCommand;
import de.verdox.vcore.impl.gameserver.paper.data.PlayerAdvancementProgress;
import de.verdox.vcore.impl.gameserver.paper.listener.PlayerListener;
import de.verdox.vcore.impl.gameserver.paper.typeadapters.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class VCorePaper extends JavaPlugin {
    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new DefaultThreadFactory("VCore-ThreadPool"));
    private NetworkConfig networkConfig;
    public static boolean isTestEnv = false;

    @Override
    public void onLoad() {
        //TODO: Config zum auslesen von dem ganzen Scheiß
        if (!isTestEnv) {
            networkConfig = new NetworkConfig(Path
                    .of(getDataFolder() + File.separator + "config.yml")
                    .toFile());
        } else {
            Bukkit.getLogger().info("Dev Environment");
            networkConfig = new NetworkConfig(new YamlConfiguration(), true, "server" + ThreadLocalRandom.current()
                                                                                                         .nextInt(100), "localhost", 25565 + ThreadLocalRandom
                    .current()
                    .nextInt(100));
        }

        getLogger().info("Constructing VCoreNetwork");
        VCoreNetwork.startNetwork(new PaperMCPlatform(this), networkConfig, scheduledExecutorService, gsonBuilder -> {
            gsonBuilder.registerTypeHierarchyAdapter(Inventory.class, new InventoryTypeAdapter());
            gsonBuilder.registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectTypeAdapter());
            gsonBuilder.registerTypeHierarchyAdapter(NamespacedKey.class, new NameSpacedKeyTypeAdapter());
            gsonBuilder.registerTypeHierarchyAdapter(AttributeModifierTypeAdapter.class, new AttributeModifierTypeAdapter());
            gsonBuilder.registerTypeHierarchyAdapter(PlayerAdvancementProgress.class, new PlayerAdvancementProgressTypeAdapter());
            gsonBuilder.registerTypeHierarchyAdapter(ReadWriteNBT.class, new NBTCompoundTypeAdapter());
        });
    }

    @Override
    public void onEnable() {
        NetworkCommands.setupTeleportCommand(new PaperCommand(this, "teleport"));
        NetworkCommands.setupHealCommand(new PaperCommand(this, "heal"));
        NetworkCommands.setupSwitchServerCommand(new PaperCommand(this, "switchserver"));
        NetworkCommands.setupFeedCommand(new PaperCommand(this, "feed"));
        NetworkCommands.setupGameModeCommand(new PaperCommand(this, "gameMode"));
        NetworkCommands.setupBroadcastCommand(new PaperCommand(this, "broadcast"));
        NetworkCommands.setupClearInventoryCommand(new PaperCommand(this, "clearInventory"));
        NetworkCommands.setupKickCommand(new PaperCommand(this, "kick"));
        NetworkCommands.setupKillCommand(new PaperCommand(this, "killPlayer"));
        NetworkCommands.setupMessageCommand(new PaperCommand(this, "message"));
        new PlayerListener(this);
    }

    @Override
    public void onDisable() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.kick();
        }
        VCoreNetwork.getInstance().getPipeline().saveAll();
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
