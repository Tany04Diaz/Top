package org.akorpuzz.top;

import org.akorpuzz.top.placeholder.TopPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;

import java.util.List;

public class TopUpdateCommand implements CommandExecutor {

    private final Top plugin;
    private final TopPlaceholder topPlaceholder;

    public TopUpdateCommand(Top plugin, TopPlaceholder topPlaceholder) {
        this.plugin = plugin;
        this.topPlaceholder = topPlaceholder;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("TopUpdate: forzando actualización del snapshot...");
            try {
                List<FactionScanner.FactionData> data = FactionScanner.getValidFactions();
                plugin.getLogger().info("TopUpdate: FactionScanner returned size=" + (data == null ? 0 : data.size()));
                if (data != null && !data.isEmpty()) {
                    for (int i = 0; i < Math.min(5, data.size()); i++) {
                        FactionScanner.FactionData f = data.get(i);
                        plugin.getLogger().info("TopUpdate: top[" + (i+1) + "] = " + f.name + " total=" + f.totalValue);
                    }
                }
                topPlaceholder.updateSnapshot(data);
                plugin.getLogger().info("TopUpdate: snapshot actualizado.");
                sender.sendMessage("Top actualizado y snapshot forzado.");
            } catch (Throwable t) {
                plugin.getLogger().warning("TopUpdate: error forzando snapshot: " + t.getMessage());
                sender.sendMessage("Error al actualizar Top: " + t.getMessage());
            }
        });
        return true;
    }
}