package org.akorpuzz.top;

import java.util.List;
import java.util.Map;

public class FactionData {
    public String name;
    public String leader;
    public List<String> members;
    public int kills;
    public double balance;
    public double claimValue;
    public Map<String,Integer> spawners;
    public Map<String,Integer> items;
    public Map<String,Integer> blocks;
    // constructor público...

    public FactionData(String name, String leader, List<String> members, int kills, double balance, double claimValue,
                       Map<String, Integer> spawners, Map<String, Integer> items, Map<String, Integer> blocks) {
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.kills = kills;
        this.balance = balance;
        this.claimValue = claimValue;
        this.spawners = spawners;
        this.items = items;
        this.blocks = blocks;
    }
}

