package org.akorpuzz.top;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.List;

public class TopCommand implements CommandExecutor {

    private final Top plugin;

    public TopCommand(Top plugin) {
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

        List<FactionScanner.FactionData> factions = FactionScanner.getValidFactions();
        if (factions.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No hay facciones válidas registradas.");
            return true;
        }

        int perPage = (page == 1) ? 5 : 15;
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, factions.size());

        if (start >= factions.size()) {
            player.sendMessage(ChatColor.RED + "Esa página no existe.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Comando /top ejecutado correctamente.");
        player.sendMessage(ChatColor.BLUE + " Top de Facciones (Página " + page + ")");

        for (int i = start; i < end; i++) {
            FactionScanner.FactionData f = factions.get(i);

            TextComponent line = new TextComponent(ChatColor.YELLOW + "" + (i + 1) + ". ");
            TextComponent name = new TextComponent(f.name);
            name.setColor(net.md_5.bungee.api.ChatColor.GOLD);

            String hoverText =
                    ChatColor.GOLD + "Líder: " + ChatColor.WHITE + f.leader + "\n" +
                            ChatColor.GOLD + "Miembros: " + ChatColor.WHITE + f.members.size() + "\n" +
                            ChatColor.GOLD + "Kills: " + ChatColor.WHITE + f.kills + "\n" +
                            ChatColor.GOLD + "Dinero total: " + ChatColor.WHITE + "$" + String.format("%.2f", f.balance);

            name.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(hoverText).create()
            ));

            line.addExtra(name);
            player.spigot().sendMessage(line);
        }

        return true;
    }
}