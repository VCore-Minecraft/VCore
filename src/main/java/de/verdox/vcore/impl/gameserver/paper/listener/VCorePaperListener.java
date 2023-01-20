package de.verdox.vcore.impl.gameserver.paper.listener;

import de.verdox.vcore.api.core.listener.VCoreListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class VCorePaperListener extends VCoreListener<JavaPlugin> implements Listener {
    public VCorePaperListener(JavaPlugin platformPlugin) {
        super(platformPlugin);
    }

    @Override
    protected void registerListener(JavaPlugin platformPlugin) {
        Bukkit.getPluginManager().registerEvents(this, platformPlugin);
    }

    @Override
    protected void unRegisterListener(JavaPlugin platformPlugin) {
        HandlerList.unregisterAll(this);
    }
}
