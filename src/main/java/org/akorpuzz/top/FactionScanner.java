package org.akorpuzz.top;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class FactionScanner {

    public static class FactionData {
        public String name;
        public String leader;
        public List<String> members;
        public int kills;
        public double balance;

        public FactionData(String name, String leader, List<String> members, int kills, double balance) {
            this.name = name;
            this.leader = leader;
            this.members = members;
            this.kills = kills;
            this.balance = balance;
        }
    }

    public static List<FactionData> getValidFactions() {
        List<FactionData> result = new ArrayList<>();

        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (!faction.isNormal()) continue;

            String name = faction.getTag();
            FPlayer admin = faction.getFPlayerAdmin();
            String leader = (admin != null) ? admin.getName() : "Desconocido";

            List<String> members = faction.getFPlayers().stream()
                    .map(FPlayer::getName)
                    .toList();

            int kills = 0; // Puedes sumar kills por jugador si lo implementas

            double totalBalance = 0.0;
            for (FPlayer fp : faction.getFPlayers()) {
                Player p = Bukkit.getPlayer(fp.getName());
                if (p != null && p.isOnline()) {
                    totalBalance += Top.getEconomy().getBalance(p);
                }
            }

            result.add(new FactionData(name, leader, members, kills, totalBalance));
        }

        return result;
    }
}