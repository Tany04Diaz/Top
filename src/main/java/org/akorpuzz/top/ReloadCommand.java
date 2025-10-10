package org.akorpuzz.top;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class ReloadCommand implements CommandExecutor {

    private final Top plugin;

    public ReloadCommand(Top plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("top.reload")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para recargar el plugin.");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "✅ Configuración de Top recargada correctamente.");
        return true;
    }
}

