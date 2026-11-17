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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hypixel-style hotbar editor: the top row shows the 9 hotbar slots, the rows
 * below are a per-item palette. Click a palette item to select it, then click
 * any number of hotbar slots to assign that item to them. Right-click a slot
 * to clear it.
 */
public class HotbarMenu implements Listener {

    public static final String TITLE_PATH = "hotbar-menu-inventory-name";

    private static final Map<UUID, String> selection = new HashMap<>();
    private static final Map<UUID, Boolean> open = new HashMap<>();

    private static final int RESET_SLOT = 49;

    private static final int[] SELECTED_DECOR_SLOTS = {36, 37, 38, 39, 40, 41, 42, 43, 44};
    private static final int[] TOP_DECOR_SLOTS = {9, 10, 11, 12, 13, 14, 15, 16, 17};

    private static final List<PaletteEntry> PALETTE = new ArrayList<>();

    static {
        PALETTE.add(new PaletteEntry(19, "melee", "Melee Swords", "§cMelee", Material.IRON_SWORD, (short) 0));
        PALETTE.add(new PaletteEntry(20, "blocks", "Blocks", "§eBlocks", Material.WOOL, (short) 0));
        PALETTE.add(new PaletteEntry(21, "tools", "Tools", "§6Tools", Material.STONE_PICKAXE, (short) 0));
        PALETTE.add(new PaletteEntry(22, "ranged", "Bow & Arrows", "§9Ranged", Material.BOW, (short) 0));
        PALETTE.add(new PaletteEntry(23, "potions", "Potions", "§dPotions", Material.POTION, (short) 3));
        PALETTE.add(new PaletteEntry(24, "utility.fireball", "Fireball", "§aUtility", Material.FIREBALL, (short) 0));
        PALETTE.add(new PaletteEntry(25, "utility", "Utility", "§aUtility", Material.TNT, (short) 0));
    }

    static final class PaletteEntry {
        final int slot;
        final String path;
        final String name;
        final String categoryColor;
        final Material material;
        final short data;

        PaletteEntry(int slot, String path, String name, String categoryColor, Material material, short data) {
            this.slot = slot;
            this.path = path;
            this.name = name;
            this.categoryColor = categoryColor;
            this.material = material;
            this.data = data;
        }

        static PaletteEntry byPath(String path) {
            if (path == null) return null;
            for (PaletteEntry entry : PALETTE) {
                if (entry.path.equals(path)) return entry;
            }
            return null;
        }
    }

    public static void initDefaults() {
        for (Language l : Language.getLanguages()) {
            if (!l.exists(TITLE_PATH) || "&8Hotbar Layout".equals(l.getString(TITLE_PATH))) l.set(TITLE_PATH, "&8Hotbar Editor");
            if (!l.exists("hotbar-menu-info-name")) l.set("hotbar-menu-info-name", "&6Hotbar Editor");
            if (!l.exists("hotbar-menu-info-lore")) l.set("hotbar-menu-info-lore",
                    "&7Click a category, then click a hotbar slot to assign it. Right-click a slot to clear it.");
            if (!l.exists("hotbar-menu-palette-lore")) l.set("hotbar-menu-palette-lore",
                    "&7Click to select, then click a hotbar slot above.");
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
        if (msg == null || msg.isEmpty() || msg.equals(TITLE_PATH)) msg = "§8Hotbar Editor";
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private static void render(Player player, Inventory inv) {
        String selected = selection.get(player.getUniqueId());
        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());

        ItemStack pane = BedWars.nms.setTag(separator(), "hbm", "separator");
        ItemStack greenPane = BedWars.nms.setTag(greenSeparator(), "hbm", "separator");
        for (int i = 0; i < 54; i++) inv.setItem(i, pane);

        if (selected == null) {
            for (int i : TOP_DECOR_SLOTS) inv.setItem(i, greenPane);
        } else {
            for (int i : SELECTED_DECOR_SLOTS) inv.setItem(i, greenPane);
        }

        for (int slot = 0; slot < 9; slot++) {
            inv.setItem(36 + slot, previewItem(player, slot, slots[slot]));
        }

        for (PaletteEntry entry : PALETTE) {
            inv.setItem(entry.slot, paletteItem(player, entry, selected));
        }

        ItemStack reset = BedWars.nms.setTag(new ItemStack(Material.BARRIER), "hbm", "reset");
        ItemMeta resetMeta = reset.getItemMeta();
        if (resetMeta != null) {
            resetMeta.setDisplayName(color(Language.getMsg(player, "hotbar-menu-reset-name")));
            resetMeta.setLore(Collections.singletonList(color(Language.getMsg(player, "hotbar-menu-reset-lore"))));
            reset.setItemMeta(resetMeta);
        }
        inv.setItem(RESET_SLOT, reset);
    }

    private static ItemStack previewItem(Player player, int slot, String value) {
        String[] parsed = HotbarIdentifier.parseItemPath(value);
        HotbarCategory category = parsed == null ? HotbarCategory.fromString(value) : HotbarCategory.fromString(parsed[0]);

        if (category == HotbarCategory.NONE) {
            ItemStack item = BedWars.nms.setTag(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7),
                    "hbm", "slot-" + slot + ":none");
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7Empty §8(" + (slot + 1) + ")");
                meta.setLore(Arrays.asList("§7Select a palette item below,", "§7then click here to assign."));
                item.setItemMeta(meta);
            }
            return item;
        }

        String displayName = colorName(player, category);
        Material material = categoryDisplayMaterial(category);
        short data = 0;
        PaletteEntry entry = PaletteEntry.byPath(value);
        if (entry != null) {
            displayName = entry.name;
            material = entry.material;
            data = entry.data;
        }

        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f" + displayName);
            meta.setLore(Arrays.asList("§7Slot " + (slot + 1), "", "§7Right-click to clear."));
            item.setItemMeta(meta);
        }
        return BedWars.nms.setTag(item, "hbm", "slot-" + slot + ":" + value);
    }

    private static ItemStack paletteItem(Player player, PaletteEntry entry, String selected) {
        boolean isSelected = entry.path.equals(selected);
        ItemStack item = new ItemStack(entry.material, 1, entry.data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f" + entry.name + (isSelected ? " §a✔" : ""));
            meta.setLore(Arrays.asList(
                    entry.categoryColor,
                    "",
                    color(Language.getMsg(player, "hotbar-menu-palette-lore"))));
            item.setItemMeta(meta);
        }
        return BedWars.nms.setTag(item, "hbm", "palette-" + entry.path);
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

        if (tag.startsWith("palette-")) {
            String path = tag.substring("palette-".length());
            if (PaletteEntry.byPath(path) == null) return;
            if (path.equals(selection.get(player.getUniqueId()))) {
                selection.remove(player.getUniqueId());
            } else {
                selection.put(player.getUniqueId(), path);
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

            String selected = selection.get(player.getUniqueId());
            if (selected != null && !e.isRightClick()) {
                slots[slot] = selected;
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

    private static ItemStack greenSeparator() {
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
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

    private static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
