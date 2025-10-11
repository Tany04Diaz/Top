package org.akorpuzz.top;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TopCommand implements CommandExecutor {

    private final Top plugin;

    public TopCommand(Top plugin) {
        this.plugin = plugin;
        // Asegúrate de que FactionScanner tenga referencia al plugin
        FactionScanner.setPlugin(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("top.use")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadValues();
            FactionScanner.clearCache();
            sender.sendMessage(ChatColor.GREEN + "Top recargado: config y caché actualizados.");
            return true;
        }

        List<FactionScanner.FactionData> factions = FactionScanner.getValidFactions();
        sender.sendMessage(ChatColor.GOLD + "Top de facciones (" + factions.size() + ")");

        int i = 1;
        for (FactionScanner.FactionData f : factions) {
            // Visible: número + nombre + valor total de la facción
            String visible = ChatColor.YELLOW + "" + i + ". " + ChatColor.AQUA + f.name
                    + ChatColor.WHITE + " - " + ChatColor.GOLD + String.format("$%.2f", f.totalValue);

            // Construir el texto del hover usando datos reales del escaneo
            StringBuilder hover = new StringBuilder();
            hover.append(ChatColor.GRAY).append("Leader: ").append(ChatColor.GREEN).append(f.leader).append("\n");
            hover.append(ChatColor.GRAY).append("Miembros: ").append(ChatColor.GREEN).append(f.members.size()).append("\n");
            hover.append(ChatColor.GRAY).append("Kills: ").append(ChatColor.GREEN).append(f.kills).append("\n");
            hover.append(ChatColor.GRAY).append("Balance total miembros: ").append(ChatColor.GREEN).append(String.format("$%.2f", f.totalBalance)).append("\n");
            hover.append(ChatColor.GRAY).append("Valor chunks: ").append(ChatColor.GOLD).append(String.format("$%.2f", f.claimValue)).append("\n");
            hover.append(ChatColor.GRAY).append("Valor de la facción: ").append(ChatColor.GOLD).append(String.format("$%.2f", f.totalValue)).append("\n");

            // Spawners (ordenados desc por cantidad), Items y Blocks
            if (f.spawners != null && !f.spawners.isEmpty()) {
                hover.append(ChatColor.GRAY).append("Spawners:").append("\n");
                f.spawners.entrySet().stream()
                        .sorted(Map.Entry.<String,Integer>comparingByValue((a,b)->b-a))
                        .forEach(e -> hover.append("  ").append(ChatColor.YELLOW).append(e.getKey()).append(": ").append(ChatColor.GREEN).append(e.getValue()).append("\n"));
            }

            if (f.items != null && !f.items.isEmpty()) {
                hover.append(ChatColor.GRAY).append("Items:").append("\n");
                f.items.entrySet().stream()
                        .sorted(Map.Entry.<String,Integer>comparingByValue((a,b)->b-a))
                        .forEach(e -> hover.append("  ").append(ChatColor.YELLOW).append(formatName(e.getKey())).append(": ").append(ChatColor.GREEN).append(e.getValue()).append("\n"));
            }

            if (f.blocks != null && !f.blocks.isEmpty()) {
                hover.append(ChatColor.GRAY).append("Blocks:").append("\n");
                f.blocks.entrySet().stream()
                        .sorted(Map.Entry.<String,Integer>comparingByValue((a,b)->b-a))
                        .forEach(e -> hover.append("  ").append(ChatColor.YELLOW).append(formatName(e.getKey())).append(": ").append(ChatColor.GREEN).append(e.getValue()).append("\n"));
            }

            if (sender instanceof Player) {
                Player p = (Player) sender;
                TextComponent comp = new TextComponent(visible);
                comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover.toString())));
                p.spigot().sendMessage(comp);
            } else {
                // Consola: mostrar resumen en texto plano
                sender.sendMessage(visible + ChatColor.WHITE + " - Leader: " + ChatColor.GREEN + f.leader
                        + ChatColor.WHITE + " - Valor de la facción: " + ChatColor.GOLD + String.format("$%.2f", f.totalValue));
            }

            i++;
            if (i > 10) break; // límite por página; ajusta o elimina según necesites
        }

        return true;
    }

    private static String formatName(String raw) {
        return raw.replace('_', ' ').toLowerCase();
    }
}