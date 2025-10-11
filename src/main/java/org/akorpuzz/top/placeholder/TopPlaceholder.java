package org.akorpuzz.top.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.akorpuzz.top.FactionScanner;
import org.akorpuzz.top.Top;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class TopPlaceholder extends PlaceholderExpansion {

    private final Top plugin;
    // snapshot que será actualizada por el plugin periódicamente
    private volatile List<FactionScanner.FactionData> snapshot = List.of();
    private final int maxEntries;

    public TopPlaceholder(Top plugin, int maxEntries) {
        this.plugin = plugin;
        this.maxEntries = Math.max(1, maxEntries);
    }

    @Override
    public boolean persist() {
        return true; // permanece cargada
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "AkorpuzZ";
    }

    @Override
    public String getIdentifier() {
        return "top"; // placeholders: %top_1_name% etc.
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    // Llamado por PlaceholderAPI cuando quiere el valor
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null || identifier.isEmpty()) return "";

        // identificador esperado: "<index>_name", "<index>_value", "<index>_rankline", etc.
        String id = identifier.toLowerCase(Locale.ROOT);
        String[] parts = id.split("_", 2);
        if (parts.length < 2) return "";

        int index;
        try {
            index = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return "";
        }
        if (index < 1) return "";

        int idx = index - 1; // lista 0-based
        List<FactionScanner.FactionData> list = snapshot;
        if (idx >= list.size()) return "";

        FactionScanner.FactionData f = list.get(idx);
        String field = parts[1];

        switch (field) {
            case "name": return f.name;
            case "leader": return f.leader;
            case "members": return String.valueOf(f.members.size());
            case "kills": return String.valueOf(f.kills);
            case "claimvalue": return String.format("%.2f", f.claimValue);
            case "totalbalance": return String.format("%.2f", f.totalBalance);
            case "totalvalue": return String.format("%.2f", f.totalValue);
            case "rankline":
                return index + ". " + f.name + " - $" + String.format("%.2f", f.totalValue);
            default:
                return "";
        }
    }

    // método público que el plugin puede llamar para actualizar el snapshot
    public void updateSnapshot(List<FactionScanner.FactionData> data) {
        if (data == null) {
            this.snapshot = List.of();
            return;
        }
        if (data.size() > maxEntries) {
            this.snapshot = List.copyOf(data.subList(0, maxEntries));
        } else {
            this.snapshot = List.copyOf(data);
        }
    }
}