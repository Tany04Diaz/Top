package org.akorpuzz.top;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.Map;

public final class Top extends JavaPlugin {

    private static Economy economy;
    private static boolean placeholderAPIEnabled = false;
    private static boolean saberFactionsEnabled = false;

    // Valores cargados desde config.yml
    private final Map<String, Double> blockValues = new HashMap<>();
    private final Map<String, Double> spawnerValues = new HashMap<>();
    private final Map<String, Double> itemValues = new HashMap<>();

    @Override
    public void onEnable() {
        System.out.println("iniciando Plugin Top, Holaaa!!");
        getLogger().info("Plugin Top activado");

        // Cargar configuración por defecto si no existe
        saveDefaultConfig();

        // Registrar comandos (se asume que las clases existan y tengan constructores apropiados)
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

            // Cargar valores desde config y poner la instancia en FactionScanner
            loadValues();
            FactionScanner.setPlugin(this);

            getLogger().info("Valores cargados desde config.yml y FactionScanner inicializado.");
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

    // Instancia (no estática) para que FactionScanner pueda llamar plugin.getEconomy()
    public Economy getEconomy() {
        return economy;
    }

    // Conserva getter estático por compatibilidad con otras partes del código si lo usan
    public static Economy getEconomyStatic() {
        return economy;
    }

    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public static boolean isSaberFactionsEnabled() {
        return saberFactionsEnabled;
    }

    // Cargar valores desde config.yml en las maps
    public void loadValues() {
        blockValues.clear();
        spawnerValues.clear();
        itemValues.clear();

        ConfigurationSection blocks = getConfig().getConfigurationSection("values.blocks");
        if (blocks != null) {
            for (String key : blocks.getKeys(false)) {
                try {
                    blockValues.put(key.toUpperCase(), blocks.getDouble(key));
                } catch (Exception ignored) {}
            }
        }

        ConfigurationSection spawners = getConfig().getConfigurationSection("values.spawners");
        if (spawners != null) {
            for (String key : spawners.getKeys(false)) {
                try {
                    spawnerValues.put(key.toUpperCase(), spawners.getDouble(key));
                } catch (Exception ignored) {}
            }
        }

        ConfigurationSection items = getConfig().getConfigurationSection("values.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                try {
                    itemValues.put(key.toUpperCase(), items.getDouble(key));
                } catch (Exception ignored) {}
            }
        }
    }

    // Getters para que FactionScanner acceda a los valores
    public Map<String, Double> getBlockValues() {
        return blockValues;
    }

    public Map<String, Double> getSpawnerValues() {
        return spawnerValues;
    }

    public Map<String, Double> getItemValues() {
        return itemValues;
    }
}