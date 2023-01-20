package de.verdox.vcore.impl.gameserver.paper.commands;

import de.verdox.vcore.api.core.command.CommandSenderInfo;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperCommandSender extends CommandSenderInfo<JavaPlugin, CommandSender, Player> {
    public PaperCommandSender(JavaPlugin plugin, CommandSender sender) {
        super(plugin, sender);
    }

    @Override
    public Player getAsPlayer() {
        return (Player) sender;
    }

    @Override
    public DataReference<VCorePlayer> getAsVCorePlayer() {
        return VCoreNetwork.getInstance().getPlayerReference(getAsPlayer().getUniqueId());
    }

    @Override
    public CommandSender getAsSender() {
        return sender;
    }
}
