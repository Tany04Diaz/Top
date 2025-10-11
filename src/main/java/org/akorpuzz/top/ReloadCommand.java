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

        try {
            // Recargar config en memoria
            plugin.reloadConfig();

            // Volver a cargar valores desde config.yml a las maps de Top
            plugin.loadValues();

            // Si ClaimScanner tiene valores cargados a partir de config, forzar recarga allí también
            try {
                ClaimScanner.reloadConfigValues(plugin);
            } catch (NoClassDefFoundError | Exception ignored) {
                // Si ClaimScanner no existe o falla, lo ignoramos para no romper la recarga global
            }

            // Limpiar caché para que los cambios se vean inmediatamente
            FactionScanner.clearCache();

            // Reasegurar que FactionScanner use la instancia actual del plugin
            FactionScanner.setPlugin(plugin);

            sender.sendMessage(ChatColor.GREEN + "✅ Configuración de Top recargada correctamente.");
            plugin.getLogger().info("Top: config recargada y caché limpiado por comando /top reload.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "✖ Error al recargar la configuración. Revisa la consola.");
            plugin.getLogger().severe("Error al recargar Top: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}