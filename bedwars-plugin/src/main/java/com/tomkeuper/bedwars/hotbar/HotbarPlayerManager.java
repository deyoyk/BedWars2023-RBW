package com.tomkeuper.bedwars.hotbar;

import com.tomkeuper.bedwars.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player hotbar layout: 9 slots, each assigned a hotbar category or an
 * explicit "category.content" shop item path. Persisted via the plugin database
 * as a custom stat column per slot.
 */
public class HotbarPlayerManager {

    private static final String COLUMN_PREFIX = "hotbar_slot_";
    private static final String COLUMN_TYPE = "VARCHAR(200)";

    private static HotbarPlayerManager instance;

    private final Map<UUID, String[]> hotbars = new ConcurrentHashMap<>();

    public HotbarPlayerManager() {
        instance = this;
    }

    public static HotbarPlayerManager getInstance() {
        return instance;
    }

    public void load(Player player) {
        String[] slots = new String[9];
        try {
            for (int i = 0; i < 9; i++) {
                Object value = BedWars.getRemoteDatabase().getCustomStat(COLUMN_PREFIX + i, player.getUniqueId());
                slots[i] = value == null ? null : value.toString();
            }
        } catch (Throwable t) {
            slots = new String[9];
        }
        hotbars.put(player.getUniqueId(), slots);
    }

    public void unload(UUID uuid) {
        String[] slots = hotbars.remove(uuid);
        if (slots != null) save(uuid, slots, true);
    }

    public void saveAll() {
        saveAll(true);
    }

    public void saveAll(boolean async) {
        for (Map.Entry<UUID, String[]> e : hotbars.entrySet()) {
            save(e.getKey(), e.getValue(), async);
        }
    }

    private void save(UUID uuid, String[] slots, boolean async) {
        for (int i = 0; i < 9; i++) {
            final int slot = i;
            final String value = slots[i] == null ? "" : slots[i];
            if (async) {
                Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin,
                        () -> BedWars.getRemoteDatabase().saveCustomStat(COLUMN_PREFIX + slot, uuid, value, COLUMN_TYPE));
            } else {
                BedWars.getRemoteDatabase().saveCustomStat(COLUMN_PREFIX + slot, uuid, value, COLUMN_TYPE);
            }
        }
    }

    /**
     * Persist a single player's layout asynchronously (main-thread safe).
     */
    public void savePlayer(UUID uuid) {
        String[] slots = hotbars.get(uuid);
        if (slots == null) return;
        String[] copy = slots.clone();
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () -> save(uuid, copy, false));
    }

    public String[] get(UUID uuid) {
        return hotbars.computeIfAbsent(uuid, k -> new String[9]);
    }

    public List<String> getAsStringList(UUID uuid) {
        String[] slots = get(uuid);
        List<String> list = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) list.add(slots[i] == null ? "none" : slots[i]);
        return list;
    }

    public String getSlot(UUID uuid, int slot) {
        String[] slots = get(uuid);
        return slot < 0 || slot > 8 || slots[slot] == null ? "none" : slots[slot];
    }

    public void setSlot(UUID uuid, int slot, String value) {
        if (slot < 0 || slot > 8) return;
        get(uuid)[slot] = value;
    }

    public void reset(UUID uuid) {
        String[] slots = get(uuid);
        for (int i = 0; i < 9; i++) slots[i] = null;
    }

    public boolean isLoaded(UUID uuid) {
        return hotbars.containsKey(uuid);
    }
}
