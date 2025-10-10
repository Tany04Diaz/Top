package org.akorpuzz.top;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;

public final class Top extends JavaPlugin {

    private static Economy economy;
    private static boolean placeholderAPIEnabled = false;
    private static boolean saberFactionsEnabled = false;

    @Override
    public void onEnable() {
        System.out.println("iniciando Plugin Top, Holaaa!!");
        getLogger().info("Plugin Top activado");

        // Cargar configuración
        saveDefaultConfig();

        // Registrar comandos
        getCommand("top").setExecutor(new TopCommand(this));
        getCommand("topplayers").setExecutor(new TopPlayersCommand(this));
        getCommand("topreload").setExecutor(new ReloadCommand(this));

        // Detectar PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI detectado.");
        }

        // Detectar SaberFactions
        if (Bukkit.getPluginManager().getPlugin("SaberFactions") != null) {
            saberFactionsEnabled = true;
            getLogger().info("SaberFactions detectado.");
        } else {
            getLogger().warning("SaberFactions no está presente. Algunas funciones pueden no estar disponibles.");
        }

        // Retrasar chequeo de Vault para permitir que se registre correctamente
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!setupEconomy()) {
                getLogger().severe("Vault no está disponible. Desactivando plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getLogger().info("Vault detectado correctamente.");
        }, 20L); // 20 ticks = 1 segundo
    }

    @Override
    public void onDisable() {
        System.out.println("shutting down plugin Top, Adios!!");
        getLogger().info("Plugin Top desactivado.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public static boolean isSaberFactionsEnabled() {
        return saberFactionsEnabled;
    }
}