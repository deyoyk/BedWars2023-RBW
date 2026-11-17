/*
 * BedWars2023 - A bed wars mini-game.
 * Copyright (C) 2024 Tomas Keuper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: contact@fyreblox.com
 */

package com.tomkeuper.bedwars.compass;

import com.tomkeuper.bedwars.BedWars;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class CompassItemBuilder {

    public static final String TAG_DATA = "compass-data";
    public static final String TAG_SLOT = "compass-slot";
    public static final String TAG_PATH = "compass-path";
    public static final String TAG_MENU = "compass-menu";

    public static ItemStack build(Material material, String displayName, List<String> lore, boolean enchanted, String data, Integer slot, String path, String menuType) {
        ItemStack is = new ItemStack(material);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(color(displayName));
        if (lore != null) meta.setLore(lore.stream().map(CompassItemBuilder::color).collect(Collectors.toList()));
        if (enchanted) {
            meta.addEnchant(Enchantment.LUCK, 0, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        is.setItemMeta(meta);
        if (data != null) is = BedWars.nms.setTag(is, TAG_DATA, data);
        if (slot != null) is = BedWars.nms.setTag(is, TAG_SLOT, String.valueOf(slot));
        if (path != null) is = BedWars.nms.setTag(is, TAG_PATH, path);
        if (menuType != null) is = BedWars.nms.setTag(is, TAG_MENU, menuType);
        return is;
    }

    public static String tag(ItemStack is, String key) {
        String value = BedWars.nms.getTag(is, key);
        return value == null ? "" : value;
    }

    public static int slot(ItemStack is) {
        try {
            return Integer.parseInt(tag(is, TAG_SLOT));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String color(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static boolean isCompass(ItemStack is) {
        return is != null && is.getType() == Material.COMPASS && "compass-item".equals(tag(is, TAG_DATA));
    }
}
