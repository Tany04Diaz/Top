package org.akorpuzz.top;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClaimScanner {

    private static final Map<Material, Double> blockValues = new HashMap<>();
    private static final Map<Material, Double> itemValues = new HashMap<>();

    /**
     * Carga los valores configurados desde config.yml
     */
    public static void reloadConfigValues(Top plugin) {
        blockValues.clear();
        itemValues.clear();

        ConfigurationSection blocks = plugin.getConfig().getConfigurationSection("claim-values.blocks");
        if (blocks != null) {
            for (String key : blocks.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    blockValues.put(mat, blocks.getDouble(key));
                } else {
                    plugin.getLogger().warning("[Top] Bloque inválido en config.yml: " + key);
                }
            }
        }

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("claim-values.items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    itemValues.put(mat, items.getDouble(key));
                } else {
                    plugin.getLogger().warning("[Top] Ítem inválido en config.yml: " + key);
                }
            }
        }

        plugin.getLogger().info("[Top] Valores de bloques e ítems recargados correctamente.");
    }

    /**
     * Escanea los claims de una facción de forma asíncrona
     */
    public static CompletableFuture<Double> scanFactionClaimsAsync(Faction faction) {
        return CompletableFuture.supplyAsync(() -> scanFactionClaims(faction));
    }

    /**
     * Escaneo síncrono (puede llamarse desde fuera si lo necesitas)
     */
    public static double scanFactionClaims(Faction faction) {
        double total = 0.0;

        // Escanea todos los chunks cargados actualmente
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                FLocation floc = new FLocation(world.getName(), chunk.getX(), chunk.getZ());
                Faction owner = Board.getInstance().getFactionAt(floc);

                if (owner != null && owner.equals(faction)) {
                    total += scanChunk(chunk);
                }
            }
        }

        return total;
    }

    /**
     * Escanea un chunk reclamado y calcula su valor
     */
    private static double scanChunk(Chunk chunk) {
        double value = 0.0;

        for (BlockState state : chunk.getTileEntities()) {
            Material type = state.getType();

            // Valor de bloque
            if (blockValues.containsKey(type)) {
                value += blockValues.get(type);
            }

            // Valor de ítems dentro de cofres
            if (state instanceof Chest chest) {
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item == null) continue;
                    Material itemType = item.getType();
                    if (itemValues.containsKey(itemType)) {
                        value += item.getAmount() * itemValues.get(itemType);
                    }
                }
            }
        }

        return value;
    }
}
