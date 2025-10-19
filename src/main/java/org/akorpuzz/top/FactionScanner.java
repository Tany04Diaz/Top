package org.akorpuzz.top;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FactionScanner {

    private static Top plugin;

    public static void setPlugin(Top instance) {
        plugin = instance;
    }

    private static final Map<String, CachedScan> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5 minutos

    public static List<FactionData> getValidFactions() {
        List<FactionData> result = new ArrayList<>();
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction == null) continue;
            if (faction.isWilderness() || faction.isSafeZone() || faction.isWarZone()) continue;
            FactionData data = scanFactionClaimsReal(faction);
            result.add(data);
        }
        return result;
    }

    public static FactionData scanFactionClaimsReal(Faction faction) {
        if (faction == null)
            return new FactionData("unknown", "Desconocido", Collections.emptyList(), 0, 0.0, 0.0, 0.0, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        String leaderName = faction.getFPlayers().stream()
                .map(FPlayer::getName)
                .findFirst()
                .orElse("Desconocido");

        List<String> members = faction.getFPlayers().stream().map(FPlayer::getName).collect(Collectors.toList());
        int kills = faction.getFPlayers().stream().mapToInt(FPlayer::getKills).sum();

        // calcular balances individuales y totalBalance
        double totalBalance = 0.0;
        if (plugin != null && plugin.getEconomy() != null) {
            for (String memberName : members) {
                try {
                    OfflinePlayer off = Bukkit.getOfflinePlayer(memberName);
                    totalBalance += plugin.getEconomy().getBalance(off);
                } catch (Exception ignored) {
                }
            }
        }

        String name = faction.getTag();
        double claimValue = 0.0;
        Map<String, Integer> spawners = new HashMap<>();
        Map<String, Integer> items = new HashMap<>();
        Map<String, Integer> blocks = new HashMap<>();

        // Set para evitar procesar el mismo contenedor dos veces usando claves por ubicación
        Set<String> seenContainerKeys = new HashSet<>();

        // Rango por defecto de chunks a escanear; cambia por claims reales si lo prefieres
        int minChunkX = -300, maxChunkX = 300, minChunkZ = -300, maxChunkZ = 300;

        for (World world : Bukkit.getWorlds()) {
            if (world == null) continue;
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    FLocation floc = new FLocation(world.getName(), chunkX, chunkZ);
                    Faction at = Board.getInstance().getFactionAt(floc);
                    if (at == null) continue;
                    if (!at.getId().equals(faction.getId())) continue;

                    Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    if (!chunk.isLoaded()) continue;

                    for (int cx = 0; cx < 16; cx++) {
                        for (int cz = 0; cz < 16; cz++) {
                            for (int y = 0; y < world.getMaxHeight(); y++) {
                                Block block = chunk.getBlock(cx, y, cz);
                                Material type = block.getType();

                                // Bloques valorados desde config
                                if (plugin != null && plugin.getBlockValues().containsKey(type.name())) {
                                    blocks.put(type.name(), blocks.getOrDefault(type.name(), 0) + 1);
                                    claimValue += plugin.getBlockValues().get(type.name());
                                }

                                // Spawners
                                if (type == Material.SPAWNER && block.getState() instanceof CreatureSpawner) {
                                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                                    String mob = spawner.getSpawnedType().name();
                                    spawners.put(mob, spawners.getOrDefault(mob, 0) + 1);
                                    if (plugin != null)
                                        claimValue += plugin.getSpawnerValues().getOrDefault(mob.toUpperCase(), 0.0);
                                }

                                // Contenedores / cofres: identificación por clave de ubicación y detección de cofres dobles
                                BlockState state = block.getState();
                                if (state instanceof Container) {
                                    Container container = (Container) state;
                                    Inventory invRef = container.getInventory();
                                    if (invRef == null) continue;

                                    // Ubicación del BlockState actual (siempre disponible para block states)
                                    Location loc = container.getLocation();
                                    if (loc == null) continue;

                                    // intentamos detectar cofre doble buscando bloque adyacente que forme la pareja
                                    Block currentBlock = loc.getBlock();
                                    Block adjacentDouble = null;

                                    BlockFace[] faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
                                    for (BlockFace face : faces) {
                                        Block nb = currentBlock.getRelative(face);
                                        if (nb == null) continue;
                                        BlockState nbState = nb.getState();
                                        if (nbState instanceof Chest || nbState instanceof Container) {
                                            try {
                                                Inventory nbInv = ((Container) nbState).getInventory();
                                                if (nbInv != null) {
                                                    // Si ambos inventarios comparten el mismo holder (DoubleChestInventory) o son exactamente el mismo inventario,
                                                    // consideramos que forman un cofre doble y marcamos adjacentDouble.
                                                    InventoryHolder h1 = invRef.getHolder();
                                                    InventoryHolder h2 = nbInv.getHolder();

                                                    if (nbInv == invRef || (h1 != null && h2 != null && Objects.equals(h1, h2))) {
                                                        adjacentDouble = nb;
                                                        break;
                                                    }

                                                    // Alternativamente, detectar DoubleChestInventory directamente
                                                    if (invRef instanceof DoubleChestInventory || nbInv instanceof DoubleChestInventory) {
                                                        // ordenar por coordenadas para generar key determinista
                                                        adjacentDouble = nb;
                                                        break;
                                                    }
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    }

                                    String key;
                                    if (adjacentDouble != null) {
                                        // ordenar para generar key determinista
                                        Block a = currentBlock;
                                        Block b = adjacentDouble;
                                        if (compareBlockCoords(a, b) > 0) { Block tmp = a; a = b; b = tmp; }
                                        key = "DOUBLE:" + a.getWorld().getName() + ":" + a.getX() + "," + a.getY() + "," + a.getZ()
                                                + "|" + b.getX() + "," + b.getY() + "," + b.getZ();
                                    } else {
                                        key = "SINGLE:" + loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                                    }

                                    if (!seenContainerKeys.add(key)) continue; // ya procesado

                                    // procesar inventario una sola vez
                                    for (ItemStack item : invRef.getContents()) {
                                        if (item == null) continue;
                                        String itemName = item.getType().name();
                                        items.put(itemName, items.getOrDefault(itemName, 0) + item.getAmount());
                                        if (plugin != null)
                                            claimValue += plugin.getItemValues().getOrDefault(itemName.toUpperCase(), 0.0) * item.getAmount();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        double totalValue = totalBalance + claimValue;

        // Guardar en caché solo claimValue como antes
        cache.put(faction.getId(), new CachedScan(claimValue, System.currentTimeMillis()));
        return new FactionData(name, leaderName, members, kills, totalBalance, claimValue, totalValue, spawners, items, blocks);
    }

    public static double getClaimValueCached(Faction faction) {
        if (faction == null) return 0.0;
        String id = faction.getId();
        CachedScan cached = cache.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.value;
        }
        FactionData fresh = scanFactionClaimsReal(faction);
        return fresh.claimValue;
    }

    public static double getTotalValueCached(Faction faction) {
        if (faction == null) return 0.0;
        String id = faction.getId();
        CachedScan cached = cache.get(id);
        if (cached != null && !cached.isExpired()) {
            // cache tiene claimValue; para totalValue necesitamos recalcular (consistente)
            FactionData fresh = scanFactionClaimsReal(faction);
            return fresh.totalValue;
        }
        FactionData fresh = scanFactionClaimsReal(faction);
        return fresh.totalValue;
    }

    public static void invalidateCache(String factionId) {
        if (factionId == null) return;
        cache.remove(factionId);
    }

    public static void clearCache() {
        cache.clear();
    }

    // Clase de datos pública y estática para acceso fácil desde TopCommand
    public static class FactionData {
        public final String name;
        public final String leader;
        public final List<String> members;
        public final int kills;
        public final double totalBalance;
        public final double claimValue;
        public final double totalValue;
        public final Map<String, Integer> spawners;
        public final Map<String, Integer> items;
        public final Map<String, Integer> blocks;

        public FactionData(String name, String leader, List<String> members, int kills,
                           double totalBalance, double claimValue, double totalValue,
                           Map<String, Integer> spawners, Map<String, Integer> items, Map<String, Integer> blocks) {
            this.name = name;
            this.leader = leader;
            this.members = Collections.unmodifiableList(new ArrayList<>(members));
            this.kills = kills;
            this.totalBalance = totalBalance;
            this.claimValue = claimValue;
            this.totalValue = totalValue;
            this.spawners = Collections.unmodifiableMap(new HashMap<>(spawners));
            this.items = Collections.unmodifiableMap(new HashMap<>(items));
            this.blocks = Collections.unmodifiableMap(new HashMap<>(blocks));
        }
    }

    // Clase de caché (solo valor y timestamp)
    public static class CachedScan {
        public final double value;
        public final long timestamp;

        public CachedScan(double value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    // Helper para ordenar bloques de forma determinista
    private static int compareBlockCoords(Block a, Block b) {
        int c = a.getWorld().getName().compareTo(b.getWorld().getName());
        if (c != 0) return c;
        if (a.getX() != b.getX()) return Integer.compare(a.getX(), b.getX());
        if (a.getY() != b.getY()) return Integer.compare(a.getY(), b.getY());
        return Integer.compare(a.getZ(), b.getZ());
    }
}