package de.verdox.vcore.impl.gameserver.paper.commands;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import de.verdox.vcore.api.core.command.CommandCallback;
import de.verdox.vcore.api.core.command.VCoreCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PaperCommand extends VCoreCommand<JavaPlugin, CommandSender, Player, PaperCommandSender, PaperCommandCallback> implements CommandExecutor, Listener {

    private Command command;

    public PaperCommand(@NotNull JavaPlugin plugin, @NotNull String commandName) {
        super(plugin, commandName);
    }

    @Override
    protected void registerCommandOnPlatform() {
        this.command = plugin.getCommand(commandName);
        Objects.requireNonNull(command);
        ((PluginCommand) command).setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Command registered: " + commandName);
    }

    @Override
    protected PaperCommandCallback instantiateCommandCallback(JavaPlugin plugin, String[] commandPath) {
        return new PaperCommandCallback(plugin, commandPath);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean errorMessageSent = false;
            for (PaperCommandCallback commandCallback : commandCallbacks) {
                var response = commandCallback.onCommand(sender, args);

                if (response.errorMessageSent())
                    errorMessageSent = true;
                if (response.responseType().equals(CommandCallback.CallbackResponse.ResponseType.SUCCESS))
                    return;
            }

            if (!errorMessageSent) {
                sender.sendMessage("");

                for (PaperCommandCallback commandCallback : commandCallbacks) {
                    if (commandCallback.getNeededPermission() != null && !commandCallback
                            .getNeededPermission()
                            .isEmpty() && !sender.hasPermission(commandCallback.getNeededPermission()))
                        continue;
                    String suggested = commandCallback.getSuggested(this);
                    if (suggested != null && !suggested.isEmpty())
                        sender.sendMessage(suggested);
                }

                sender.sendMessage("");
            }
        });

        return true;
    }

    @EventHandler
    public void asyncTabComplete(AsyncTabCompleteEvent e) {
        if (!e.isCommand())
            return;
        List<String> suggest = new ArrayList<>();
        String[] cmdArgs = e.getBuffer().replace("/", "").split(" ");
        String commandLabel = cmdArgs[0].toLowerCase(Locale.ROOT);

        var command = Bukkit.getCommandMap().getCommand(commandLabel);
        if (command == null)
            return;
        if (!command.equals(this.command))
            return;
        e.setHandled(true);


        String[] args = Arrays.copyOfRange(cmdArgs, 1, cmdArgs.length);
/*        if (args.length == 0)
            return;*/


        // -> We are already in the next argument -> We add an empty string to the arg array
        if (args.length == 0 || e.getBuffer().lastIndexOf(" ") == e.getBuffer().length() - 1) {
            args = Arrays.copyOf(args, args.length + 1);
            args[args.length - 1] = "";
        }

        String permission = Bukkit.getCommandMap().getCommand(commandLabel).getPermission();
        if (permission != null && !permission.isEmpty() && !e.getSender().hasPermission(permission)) {
            e.setCancelled(true);
            return;
        }
        for (PaperCommandCallback vCommandCallback : commandCallbacks) {
            List<String> suggested = vCommandCallback.suggest(e.getSender(), args);

            if (suggested != null && !suggested.isEmpty())
                suggest.addAll(suggested);
        }
        e.setCompletions(suggest);
    }
}
