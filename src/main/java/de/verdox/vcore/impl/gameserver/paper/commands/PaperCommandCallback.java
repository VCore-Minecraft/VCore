package de.verdox.vcore.impl.gameserver.paper.commands;

import de.verdox.vcore.api.core.command.CommandCallback;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class PaperCommandCallback extends CommandCallback<JavaPlugin, CommandSender, Player, PaperCommandSender> {
    public PaperCommandCallback(@NotNull JavaPlugin plugin, @NotNull String... commandPath) {
        super(plugin, commandPath);
    }

    @Override
    protected boolean hasSenderPermission(CommandSender sender, String permissionNode) {
        return sender.hasPermission(permissionNode);
    }

    @Override
    protected boolean sendMessage(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message));
        return true;
    }

    @Override
    protected boolean isSenderPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    protected boolean isSenderConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }

    @Override
    protected List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    @Override
    protected PaperCommandSender instantiateSenderInfo(CommandSender sender) {
        return new PaperCommandSender(plugin, sender);
    }
}
