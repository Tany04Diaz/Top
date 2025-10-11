package org.akorpuzz.top;

import org.akorpuzz.top.placeholder.TopPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitRunnable;

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

    // Placeholder snapshot
    private TopPlaceholder topPlaceholder;

    @Override
    public void onEnable() {
        getLogger().info("§a[Top] Activando plugin...");

        // Cargar configuración
        saveDefaultConfig();
        loadValues();

        // Registrar comandos
        if (getCommand("top") != null) getCommand("top").setExecutor(new TopCommand(this));
        if (getCommand("topplayers") != null) getCommand("topplayers").setExecutor(new TopPlayersCommand(this));
        if (getCommand("topreload") != null) getCommand("topreload").setExecutor(new ReloadCommand(this));

        // Detectar dependencias
        placeholderAPIEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        saberFactionsEnabled = Bukkit.getPluginManager().getPlugin("SaberFactions") != null;

        if (placeholderAPIEnabled)
            getLogger().info("§aPlaceholderAPI detectado.");
        else
            getLogger().warning("§ePlaceholderAPI no está presente.");

        if (saberFactionsEnabled)
            getLogger().info("§aSaberFactions detectado.");
        else
            getLogger().warning("§eSaberFactions no está presente. Algunas funciones pueden no funcionar.");

        // Esperar un segundo para que Vault se cargue
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!setupEconomy()) {
                getLogger().severe("§cVault no está disponible. Desactivando plugin.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getLogger().info("§aVault detectado correctamente.");

            // Inicializar FactionScanner
            FactionScanner.setPlugin(this);

            // Registrar placeholders
            if (placeholderAPIEnabled) {
                int maxEntries = Math.max(1, getConfig().getInt("holograms.max-lines", 10));
                topPlaceholder = new TopPlaceholder(this, maxEntries);

                try {
                    topPlaceholder.register();
                    getLogger().info("§aTopPlaceholder registrado correctamente con PlaceholderAPI.");

                    // Actualizar inmediatamente
                    actualizarSnapshotAsync();

                    // Crear tarea repetitiva
                    long intervalSeconds = Math.max(10, getConfig().getLong("holograms.update-interval-seconds", 60));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            actualizarSnapshotAsync();
                        }
                    }.runTaskTimerAsynchronously(this, intervalSeconds * 20L, intervalSeconds * 20L);

                } catch (Throwable t) {
                    getLogger().warning("§cError al registrar TopPlaceholder: " + t.getMessage());
                }
            }

        }, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info("§c[Top] Desactivando plugin...");
        if (topPlaceholder != null) {
            try {
                topPlaceholder.unregister();
                getLogger().info("§7TopPlaceholder desregistrado.");
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Actualiza el snapshot de las facciones de manera asíncrona
     * para que los placeholders reflejen siempre los datos actuales.
     */
    private void actualizarSnapshotAsync() {
        if (topPlaceholder == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                var data = FactionScanner.getValidFactions(); // obtiene datos actualizados
                topPlaceholder.updateSnapshot(data);
                getLogger().info("§7TopPlaceholder snapshot actualizado (" + data.size() + " facciones).");
            } catch (Throwable t) {
                getLogger().warning("§cError al actualizar snapshot: " + t.getMessage());
            }
        });
    }

    // --- Economía (Vault) ---
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public static Economy getEconomyStatic() {
        return economy;
    }

    // --- Dependencias ---
    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public static boolean isSaberFactionsEnabled() {
        return saberFactionsEnabled;
    }

    // --- Configuración ---
    public void loadValues() {
        blockValues.clear();
        spawnerValues.clear();
        itemValues.clear();

        ConfigurationSection blocks = getConfig().getConfigurationSection("values.blocks");
        if (blocks != null)
            for (String key : blocks.getKeys(false))
                blockValues.put(key.toUpperCase(), blocks.getDouble(key));

        ConfigurationSection spawners = getConfig().getConfigurationSection("values.spawners");
        if (spawners != null)
            for (String key : spawners.getKeys(false))
                spawnerValues.put(key.toUpperCase(), spawners.getDouble(key));

        ConfigurationSection items = getConfig().getConfigurationSection("values.items");
        if (items != null)
            for (String key : items.getKeys(false))
                itemValues.put(key.toUpperCase(), items.getDouble(key));
    }

    // --- Getters ---
    public Map<String, Double> getBlockValues() { return blockValues; }
    public Map<String, Double> getSpawnerValues() { return spawnerValues; }
    public Map<String, Double> getItemValues() { return itemValues; }
}
