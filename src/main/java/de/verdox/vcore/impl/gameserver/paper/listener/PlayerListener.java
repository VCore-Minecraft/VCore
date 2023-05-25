package de.verdox.vcore.impl.gameserver.paper.listener;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.verdox.vcore.api.core.listener.PlayerDataLoader;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.data.PlayerData;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vcore.api.core.network.platform.types.TeleportCause;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerListener extends VCorePaperListener implements PlayerDataLoader {

    public PlayerListener(JavaPlugin platformPlugin) {
        super(platformPlugin);
    }

    @EventHandler
    public void onPreConnect(AsyncPlayerPreLoginEvent e) {
        try {
            ServerLocation serverLocation = new ServerLocation(VCoreNetwork
                    .getInstance()
                    .getAsServer(), extractLastLocation(e.getUniqueId()));

            onJoin(e.getUniqueId(), e.getName(), serverLocation).join();
            //restoreVanillaDataFromStorage(e.getUniqueId());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

/*    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerJoinEvent e) {
        VCoreNetwork.getInstance().getPlayerReference(e.getPlayer().getUniqueId())
                    .writeAsync(vCorePlayer -> vCorePlayer.teleport(vCorePlayer.getGameLocation(), TeleportCause.COMMAND));
    }*/

/*    @EventHandler
    public void onQuit(PlayerConnectionCloseEvent e) {
        var removeFromCache = (VCoreNetwork
                .getInstance()
                .getPlatform() instanceof GameServerPlatform gameServerPlatform) && !gameServerPlatform.isConnectedToProxyNetwork();
        saveVanillaDataToStorage(e.getPlayerUniqueId()).whenComplete((unused, throwable) -> onQuit(e.getPlayerUniqueId(), e.getPlayerName(), removeFromCache));
    }*/

    private CompletableFuture<Void> saveVanillaDataToStorage(UUID playerUUID) {
        var data = getPlayerData(playerUUID);

        return VCoreNetwork.getInstance().getPlayerData(playerUUID)
                           .writeAsync(playerData -> {
                               Bukkit.getLogger().info("Saving vanilla data to storage [" + playerUUID + "]");
                               playerData.setNbtData(data);
                           });
    }

    private void restoreVanillaDataFromStorage(UUID uuid) throws IOException {
        var data = VCoreNetwork.getInstance().getPlayerData(uuid).get(PlayerData::getNbtData);
        var standardWorld = Bukkit.getWorlds().get(0);
        var playerDataFile = Path.of(standardWorld.getWorldFolder() + "/playerdata/" + uuid + ".dat");
        if (!playerDataFile.toFile().exists())
            playerDataFile.toFile().createNewFile();
        var playerDatFile = new NBTFile(playerDataFile.toFile());
        playerDatFile.clearNBT();
        if (data != null) {
            playerDatFile.mergeCompound(data);
            playerDatFile.save();
            Bukkit.getLogger().info("Restoring from database to vanilla file storage [" + uuid + "]");
        } else {
            Bukkit.getLogger().info("No vanilla data found in storage [" + uuid + "]");
        }
    }

    private GameLocation extractLastLocation(UUID uuid) throws IOException {
        var standardWorld = Bukkit.getWorlds().get(0);
        var playerDataFile = Path.of(standardWorld.getWorldFolder() + "/playerdata/" + uuid + ".dat");

        var playerData = NBT.createNBTObject();
        playerData.mergeCompound(new NBTFile(playerDataFile.toFile()));

        if (playerData.getKeys().isEmpty())
            return new GameLocation(standardWorld.getName(), standardWorld.getSpawnLocation().getX(), standardWorld
                    .getSpawnLocation().getY(), standardWorld.getSpawnLocation().getZ(), standardWorld
                    .getSpawnLocation().getYaw(), standardWorld.getSpawnLocation().getPitch());

        var pos = playerData.getDoubleList("Pos");
        var rotation = playerData.getDoubleList("Pos");

        UUID uid = new UUID(playerData.getLong("WorldUUIDMost"), playerData.getLong("WorldUUIDLeast"));
        var lastLoginWorld = Bukkit.getWorld(uid);

        if (lastLoginWorld == null)
            lastLoginWorld = standardWorld;

        return new GameLocation(lastLoginWorld.getName(), pos.get(0), pos.get(1), pos.get(2), rotation.get(0), rotation.get(1));
    }

    private ReadWriteNBT getPlayerData(UUID uuid) {
        try {
            var standardWorld = Bukkit.getWorlds().get(0);
            var playerDataFile = Path.of(standardWorld.getWorldFolder() + "/playerdata/" + uuid + ".dat");
            if (!playerDataFile.toFile().exists())
                return NBT.createNBTObject();

            var playerData = NBT.createNBTObject();
            playerData.mergeCompound(new NBTFile(playerDataFile.toFile()));
            return playerData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
