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

    private static final int INFO_SLOT = 51;
    private static final int RESET_SLOT = 53;

    private static final List<PaletteEntry> PALETTE = new ArrayList<>();

    static {
        PALETTE.add(new PaletteEntry(9, "melee.stone-sword", "Stone Sword", Material.STONE_SWORD, (short) 0));
        PALETTE.add(new PaletteEntry(10, "melee.iron-sword", "Iron Sword", Material.IRON_SWORD, (short) 0));
        PALETTE.add(new PaletteEntry(11, "melee.diamond-sword", "Diamond Sword", Material.DIAMOND_SWORD, (short) 0));

        PALETTE.add(new PaletteEntry(13, "ranged.bow1", "Bow", Material.BOW, (short) 0));
        PALETTE.add(new PaletteEntry(14, "ranged.arrow", "Arrows", Material.ARROW, (short) 0));

        PALETTE.add(new PaletteEntry(18, "blocks.wool", "Wool", Material.WOOL, (short) 0));
        PALETTE.add(new PaletteEntry(19, "blocks.clay", "Hardened Clay", Material.STAINED_CLAY, (short) 1));
        PALETTE.add(new PaletteEntry(20, "blocks.glass", "Glass", Material.GLASS, (short) 0));
        PALETTE.add(new PaletteEntry(21, "blocks.stone", "End Stone", Material.ENDER_STONE, (short) 0));
        PALETTE.add(new PaletteEntry(22, "blocks.ladder", "Ladder", Material.LADDER, (short) 0));
        PALETTE.add(new PaletteEntry(23, "blocks.wood", "Wood Planks", Material.WOOD, (short) 0));
        PALETTE.add(new PaletteEntry(24, "blocks.obsidian", "Obsidian", Material.OBSIDIAN, (short) 0));

        PALETTE.add(new PaletteEntry(27, "tools.pickaxe", "Pickaxe", Material.WOOD_PICKAXE, (short) 0));
        PALETTE.add(new PaletteEntry(28, "tools.axe", "Axe", Material.WOOD_AXE, (short) 0));
        PALETTE.add(new PaletteEntry(29, "tools.shears", "Shears", Material.SHEARS, (short) 0));

        PALETTE.add(new PaletteEntry(31, "potions.jump-potion", "Jump Potion", Material.POTION, (short) 11));
        PALETTE.add(new PaletteEntry(32, "potions.speed-potion", "Speed Potion", Material.POTION, (short) 3));
        PALETTE.add(new PaletteEntry(33, "potions.invisibility", "Invisibility", Material.POTION, (short) 14));

        PALETTE.add(new PaletteEntry(36, "utility.fireball", "Fireball", Material.FIREBALL, (short) 0));

        PALETTE.add(new PaletteEntry(38, "utility.golden-apple", "Golden Apple", Material.GOLDEN_APPLE, (short) 0));
        PALETTE.add(new PaletteEntry(39, "utility.bedbug", "Bedbug", Material.SNOW_BALL, (short) 0));
        PALETTE.add(new PaletteEntry(40, "utility.dream-defender", "Dream Defender", Material.MONSTER_EGG, (short) 0));
        PALETTE.add(new PaletteEntry(41, "utility.tnt", "TNT", Material.TNT, (short) 0));
        PALETTE.add(new PaletteEntry(42, "utility.ender-pearl", "Ender Pearl", Material.ENDER_PEARL, (short) 0));
        PALETTE.add(new PaletteEntry(43, "utility.water-bucket", "Water Bucket", Material.WATER_BUCKET, (short) 0));
        PALETTE.add(new PaletteEntry(44, "utility.bridge-egg", "Bridge Egg", Material.EGG, (short) 0));
        PALETTE.add(new PaletteEntry(45, "utility.magic-milk", "Magic Milk", Material.MILK_BUCKET, (short) 0));
        PALETTE.add(new PaletteEntry(46, "utility.sponge", "Sponge", Material.SPONGE, (short) 0));
        PALETTE.add(new PaletteEntry(47, "utility.tower", "Pop-up Tower", Material.CHEST, (short) 0));
    }

    static final class PaletteEntry {
        final int slot;
        final String path;
        final String name;
        final Material material;
        final short data;

        PaletteEntry(int slot, String path, String name, Material material, short data) {
            this.slot = slot;
            this.path = path;
            this.name = name;
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
            if (!l.exists(TITLE_PATH)) l.set(TITLE_PATH, "&8Hotbar Editor");
            if (!l.exists("hotbar-menu-info-name")) l.set("hotbar-menu-info-name", "&6Hotbar Editor");
            if (!l.exists("hotbar-menu-info-lore")) l.set("hotbar-menu-info-lore",
                    "&7Click a palette item below, then click a hotbar slot to assign it. Right-click a slot to clear it.");
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
        for (int i = 0; i < 54; i++) inv.setItem(i, pane);

        for (int slot = 0; slot < 9; slot++) {
            inv.setItem(slot, previewItem(player, slot, slots[slot]));
        }

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(color(Language.getMsg(player, "hotbar-menu-info-name")));
            infoMeta.setLore(Collections.singletonList(color(Language.getMsg(player, "hotbar-menu-info-lore"))));
            info.setItemMeta(infoMeta);
        }
        inv.setItem(INFO_SLOT, BedWars.nms.setTag(info, "hbm", "info"));

        for (PaletteEntry entry : PALETTE) {
            inv.setItem(entry.slot, paletteItem(player, entry, selected));
        }

        ItemStack reset = BedWars.nms.setTag(new ItemStack(Material.TNT), "hbm", "reset");
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
        if (parsed != null) {
            PaletteEntry entry = PaletteEntry.byPath(value);
            if (entry != null) {
                displayName = entry.name;
                material = entry.material;
                data = entry.data;
            }
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
                    colorName(player, HotbarCategory.fromString(entry.path.substring(0, entry.path.indexOf('.')))),
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
