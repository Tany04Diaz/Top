package org.akorpuzz.top;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.*;

public class TopPlayersCommand implements CommandExecutor {

    private final Top plugin;

    public TopPlayersCommand(Top plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        player.sendMessage(ChatColor.GOLD + "💰 Top de Jugadores por Dinero (Página " + page + ")");
        player.sendMessage(ChatColor.GRAY + "No hay datos disponibles aún. El top se generará cuando se integre Vault.");

        return true;
    }
}