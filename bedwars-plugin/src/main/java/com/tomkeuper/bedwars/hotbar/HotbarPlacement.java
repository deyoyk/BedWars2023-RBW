package com.tomkeuper.bedwars.hotbar;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Places shop-bought items into the hotbar slot the player assigned to the
 * item's category in the hotbar customization menu. Applies only to items
 * bought from the shop (items tagged with a shop upgrade identifier).
 */
public final class HotbarPlacement {

    private HotbarPlacement() {}

    /**
     * Snapshot the player's inventory contents before an item is added,
     * used by {@link #place} to find where the new stack landed.
     */
    public static ItemStack[] snapshot(Player player) {
        ItemStack[] before = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            ItemStack is = player.getInventory().getItem(i);
            before[i] = is == null ? null : is.clone();
        }
        return before;
    }

    /**
     * Move an item that was just added to the player's inventory into its
     * configured hotbar slot. Only items whose material maps to a hotbar
     * category and whose category is assigned a slot are moved.
     *
     * @param player     the buyer
     * @param item       the item that was given (same instance added to inventory)
     * @param identifier the shop content identifier of the bought item
     * @param before     inventory snapshot taken before the item was added
     */
    public static void place(Player player, ItemStack item, String identifier, ItemStack[] before) {
        if (player == null || item == null || before == null) return;
        if (item.getType() == Material.AIR) return;
        if (identifier == null || identifier.isEmpty()) return;

        HotbarCategory category = HotbarIdentifier.categoryOf(identifier);
        if (category == HotbarCategory.NONE) return;
        String content = HotbarIdentifier.contentOf(identifier);
        String itemPath = HotbarIdentifier.itemPath(category, content);
        if (itemPath == null) return;

        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());

        // find the hotbar slot assigned to this exact item, fall back to category slot
        int target = -1;
        for (int i = 0; i < 9; i++) {
            if (itemPath.equals(slots[i])) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            for (int i = 0; i < 9; i++) {
                if (category.name().toLowerCase().equals(slots[i])) {
                    target = i;
                    break;
                }
            }
        }
        if (target == -1) return;

        PlayerInventory inv = player.getInventory();

        // find where the new stack landed: the slot whose content changed since the snapshot
        int given = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack now = inv.getItem(i);
            ItemStack was = before[i];
            boolean changed;
            if (now == null || now.getType() == Material.AIR) {
                changed = was != null && was.getType() != Material.AIR;
            } else if (was == null || was.getType() != now.getType()) {
                changed = true;
            } else {
                changed = now.getAmount() != was.getAmount();
            }
            if (changed && now != null && now.getType() == item.getType()) {
                given = i;
                break;
            }
        }
        if (given == -1) return;

        if (given == target) return;

        ItemStack stack = inv.getItem(given);
        if (stack == null) return;

        ItemStack targetOld = inv.getItem(target);
        inv.setItem(given, null);
        inv.setItem(target, stack);
        player.updateInventory();

        if (targetOld != null && targetOld.getType() != Material.AIR) {
            inv.addItem(targetOld);
            player.updateInventory();
        }
    }

    public static void placeDefaultItem(Player player, ItemStack item) {
        if (player == null || item == null) return;
        if (item.getType() == Material.AIR) return;

        HotbarCategory category = null;
        if (BedWars.nms.isSword(item)) category = HotbarCategory.MELEE;
        else if (BedWars.nms.isBow(item)) category = HotbarCategory.RANGED;
        else if (item.getType() == Material.WOOL) category = HotbarCategory.BLOCKS;
        if (category == null) return;

        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());
        if (slots == null) return;

        int target = -1;
        for (int i = 0; i < 9; i++) {
            if (category.name().toLowerCase().equals(slots[i])) {
                target = i;
                break;
            }
        }
        if (target == -1) {
            for (int i = 0; i < 9; i++) {
                String[] parsed = HotbarIdentifier.parseItemPath(slots[i]);
                if (parsed != null && parsed[0].equals(category.name().toLowerCase())) {
                    target = i;
                    break;
                }
            }
        }
        if (target == -1) return;

        PlayerInventory inv = player.getInventory();

        int given = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack current = inv.getItem(i);
            if (current != null && current.getType() == item.getType()
                    && current.getAmount() == item.getAmount()) {
                given = i;
                break;
            }
        }
        if (given == -1 || given == target) return;

        ItemStack targetOld = inv.getItem(target);
        inv.setItem(given, null);
        inv.setItem(target, item);
        player.updateInventory();

        if (targetOld != null && targetOld.getType() != Material.AIR) {
            inv.addItem(targetOld);
            player.updateInventory();
        }
    }

    /**
     * Reorder the whole hotbar after respawn so permanent shop items sit in
     * their configured slots. Items without a configured slot stay untouched
     * (they are re-added by the vanilla respawn flow).
     */
    public static void sortOnRespawn(Player player) {
        if (player == null) return;
        if (!HotbarPlayerManager.getInstance().isLoaded(player.getUniqueId())) return;

        String[] slots = HotbarPlayerManager.getInstance().get(player.getUniqueId());
        PlayerInventory inv = player.getInventory();

        // collect hotbar items keyed by their shop item path or category
        ItemStack[] found = new ItemStack[9];
        boolean any = false;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            String identifier = BedWars.nms.getShopUpgradeIdentifier(item);
            if (identifier == null || identifier.isEmpty()) continue;
            HotbarCategory category = HotbarIdentifier.categoryOf(identifier);
            if (category == HotbarCategory.NONE) continue;
            String content = HotbarIdentifier.contentOf(identifier);
            String itemPath = HotbarIdentifier.itemPath(category, content);
            int target = -1;
            if (itemPath != null) {
                for (int s = 0; s < 9; s++) {
                    if (itemPath.equals(slots[s])) {
                        target = s;
                        break;
                    }
                }
            }
            if (target == -1) {
                for (int s = 0; s < 9; s++) {
                    String[] parsed = HotbarIdentifier.parseItemPath(slots[s]);
                    if (parsed != null && parsed[0].equals(category.name().toLowerCase())) {
                        target = s;
                        break;
                    }
                }
            }
            if (target == -1) continue;
            found[target] = item;
            inv.setItem(i, null);
            any = true;
        }
        if (!any) return;

        for (int i = 0; i < 9; i++) {
            if (found[i] != null) inv.setItem(i, found[i]);
        }
        player.updateInventory();
    }
}
