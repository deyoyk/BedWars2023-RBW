package com.tomkeuper.bedwars.hotbar;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 54-slot menu to customize which hotbar slot each shop item category is
 * placed in. Top area: category pick items (click to select). Bottom row
 * (45-53): the 9 hotbar slots (click while a category is selected to assign,
 * or click to clear). Raw slots are tagged with NBT to stay click-safe.
 */
public class HotbarMenu implements Listener {

    public static final String TITLE_PATH = "hotbar-menu-inventory-name";

    private static final Map<UUID, HotbarCategory> selection = new HashMap<>();
    private static final Map<UUID, Boolean> open = new HashMap<>();

    private static final int[] CATEGORY_SLOTS = {10, 11, 12, 14, 15, 16};

    public static void initDefaults() {
        for (com.tomkeuper.bedwars.api.language.Language l : com.tomkeuper.bedwars.api.language.Language.getLanguages()) {
            if (!l.exists(TITLE_PATH)) l.set(TITLE_PATH, "&8Hotbar Layout");
            if (!l.exists("hotbar-menu-category-lore")) l.set("hotbar-menu-category-lore", "&7Click to select, then click a hotbar slot below.");
            if (!l.exists("hotbar-menu-slot-used-lore")) l.set("hotbar-menu-slot-used-lore", "&7Click to clear this slot.");
            if (!l.exists("hotbar-menu-slot-empty-lore")) l.set("hotbar-menu-slot-empty-lore", "&7Select a category above, then click here.");
            if (!l.exists("hotbar-menu-reset-name")) l.set("hotbar-menu-reset-name", "&cReset Layout");
            if (!l.exists("hotbar-menu-reset-lore")) l.set("hotbar-menu-reset-lore", "&7Clears every hotbar slot.");
        }
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, title(player));
        render(player, inv);
        player.openInventory(inv);
        open.put(player.getUniqueId(), true);
    }

    public static boolean isOpen(Player player) {
        return Boolean.TRUE.equals(open.get(player.getUniqueId()));
    }

    public static String title(Player player) {
        String msg = Language.getMsg(player, TITLE_PATH);
        if (msg == null || msg.isEmpty() || msg.equals(TITLE_PATH)) msg = "§8Hotbar Layout";
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static void render(Player player, Inventory inv) {
        HotbarCategory selected = selection.get(player.getUniqueId());
        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());

        for (int i = 0; i < 9; i++) inv.setItem(i, BedWars.nms.setTag(separator(), "hbm", "separator"));
        for (int i = 9; i < 17; i++) inv.setItem(i, BedWars.nms.setTag(separator(), "hbm", "separator"));
        for (int i = 18; i < 45; i++) inv.setItem(i, BedWars.nms.setTag(separator(), "hbm", "separator"));

        HotbarCategory[] categories = HotbarCategory.ASSIGNABLE;
        for (int c = 0; c < categories.length; c++) {
            HotbarCategory category = categories[c];
            boolean isSelected = category == selected;
            ItemStack item = new ItemStack(categoryDisplayMaterial(category), 1, categoryDisplayData(category));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(colorName(player, category)
                        + (isSelected ? " §a✔" : ""));
                List<String> lore = new ArrayList<>();
                lore.add(Language.getMsg(player, "hotbar-menu-category-lore"));
                item.setItemMeta(meta);
            }
            item = BedWars.nms.setTag(item, "hbm", "category-" + category.name().toLowerCase());
            inv.setItem(CATEGORY_SLOTS[c], item);
        }

        // hotbar row 45-53
        for (int slot = 0; slot < 9; slot++) {
            int invSlot = 45 + slot;
            String value = slots[slot] == null ? "none" : slots[slot];
            ItemStack item;
            String[] parsed = HotbarIdentifier.parseItemPath(value);
            HotbarCategory category = parsed == null ? HotbarCategory.fromString(value) : HotbarCategory.fromString(parsed[0]);
            if (category != HotbarCategory.NONE) {
                item = new ItemStack(categoryDisplayMaterial(category), 1, categoryDisplayData(category));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(colorName(player, category));
                    meta.setLore(java.util.Collections.singletonList(Language.getMsg(player, "hotbar-menu-slot-used-lore")));
                    item.setItemMeta(meta);
                }
                String tag = parsed == null
                        ? "slot-" + slot + ":" + category.name().toLowerCase()
                        : "slot-" + slot + ":" + value;
                item = BedWars.nms.setTag(item, "hbm", tag);
            } else {
                item = BedWars.nms.setTag(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), "hbm", "slot-" + slot + ":none");
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§7Empty §8(" + (slot + 1) + ")");
                    meta.setLore(java.util.Collections.singletonList(Language.getMsg(player, "hotbar-menu-slot-empty-lore")));
                    item.setItemMeta(meta);
                }
            }
            inv.setItem(invSlot, item);
        }

        ItemStack reset = BedWars.nms.setTag(new ItemStack(Material.TNT), "hbm", "reset");
        ItemMeta resetMeta = reset.getItemMeta();
        if (resetMeta != null) {
            resetMeta.setDisplayName(Language.getMsg(player, "hotbar-menu-reset-name"));
            resetMeta.setLore(java.util.Collections.singletonList(Language.getMsg(player, "hotbar-menu-reset-lore")));
            reset.setItemMeta(resetMeta);
        }
        inv.setItem(40, reset);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!isOpen(player)) return;
        if (!e.getView().getTitle().equals(title(player))) return;
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        String tag = BedWars.nms.getTag(clicked, "hbm");
        if (tag == null) return;

        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());

        if (tag.startsWith("category-")) {
            String name = tag.substring("category-".length()).toUpperCase();
            HotbarCategory category = HotbarCategory.fromString(name);
            if (category == HotbarCategory.NONE) return;
            if (selection.get(player.getUniqueId()) == category) {
                selection.remove(player.getUniqueId());
            } else {
                selection.put(player.getUniqueId(), category);
            }
            render(player, e.getInventory());
            return;
        }

        if (tag.startsWith("slot-")) {
            String[] parts = tag.substring("slot-".length()).split(":", 2);
            int slot;
            try {
                slot = Integer.parseInt(parts[0]);
            } catch (NumberFormatException ex) {
                return;
            }
            if (slot < 0 || slot > 8) return;

            HotbarCategory selected = selection.get(player.getUniqueId());
            if (selected != null) {
                // clear any other slot bound to this category (category or any of its items)
                for (int i = 0; i < 9; i++) {
                    if (i == slot) continue;
                    String other = slots[i] == null ? "none" : slots[i];
                    HotbarCategory otherCategory = HotbarIdentifier.parseItemPath(other) == null
                            ? HotbarCategory.fromString(other)
                            : HotbarCategory.fromString(HotbarIdentifier.parseItemPath(other)[0]);
                    if (otherCategory == selected) slots[i] = null;
                }
                slots[slot] = selected.name().toLowerCase();
                selection.remove(player.getUniqueId());
            } else {
                slots[slot] = null;
            }
            save(player);
            render(player, e.getInventory());
            return;
        }

        if (tag.equals("reset")) {
            HotbarPlayerManager.getInstance().reset(player.getUniqueId());
            save(player);
            render(player, e.getInventory());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();
        open.remove(player.getUniqueId());
        selection.remove(player.getUniqueId());
    }

    private static void save(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () ->
                HotbarPlayerManager.getInstance().savePlayer(player.getUniqueId()));
    }

    private static ItemStack separator() {
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
    }

    private static Material categoryDisplayMaterial(HotbarCategory category) {
        switch (category) {
            case BLOCKS: return Material.SANDSTONE;
            case MELEE: return Material.IRON_SWORD;
            case TOOLS: return Material.STONE_PICKAXE;
            case RANGED: return Material.BOW;
            case POTIONS: return Material.BREWING_STAND_ITEM;
            case UTILITY: return Material.TNT;
            default: return Material.STONE;
        }
    }

    private static short categoryDisplayData(HotbarCategory category) {
        return 0;
    }

    private static String colorName(Player player, HotbarCategory category) {
        switch (category) {
            case BLOCKS: return "§eBlocks";
            case MELEE: return "§cMelee";
            case TOOLS: return "§6Tools";
            case RANGED: return "§9Ranged";
            case POTIONS: return "§dPotions";
            case UTILITY: return "§aUtility";
            default: return "§7None";
        }
    }
}
