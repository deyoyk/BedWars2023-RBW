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
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompassMenus {

    private CompassMenus() {
    }

    public static class MainMenu extends CompassMenu {

        public MainMenu(Player player) {
            super(player);
        }

        @Override
        protected String titlePath() {
            return CompassMessages.MAIN_MENU_TITLE;
        }

        @Override
        protected int slots() {
            return 27;
        }

        @Override
        protected void handle(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            IArena arena = Arena.getArenaByPlayer(p);
            if (arena == null || arena.getStatus() != com.tomkeuper.bedwars.api.arena.GameState.playing || arena.isSpectator(p)) return;
            String data = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_DATA);
            if ("tracker-menu".equals(data)) {
                new TrackerMenu(p, arena, false).open();
            } else if ("communications-menu".equals(data)) {
                new CommunicationsMenu(p, arena).open();
            }
        }

        @Override
        protected void setItems() {
            IArena arena = Arena.getArenaByPlayer(player);
            ITeam team = arena.getTeam(player);
            if (team != null && team.getMembers().size() > 1) {
                inventory.setItem(13, trackerItem());
                inventory.setItem(15, communicationsItem());
            } else {
                inventory.setItem(13, trackerItem());
            }
        }

        private ItemStack trackerItem() {
            Language lang = Language.getPlayerLanguage(player);
            return CompassItemBuilder.build(Material.COMPASS, lang.m(CompassMessages.PREFIX + "menus.main-menu.tracker.display-name"),
                    lang.l(CompassMessages.PREFIX + "menus.main-menu.tracker.lore"), false, "tracker-menu", 13, null, null);
        }

        private ItemStack communicationsItem() {
            Language lang = Language.getPlayerLanguage(player);
            return CompassItemBuilder.build(Material.EMERALD, lang.m(CompassMessages.PREFIX + "menus.main-menu.communications.display-name"),
                    lang.l(CompassMessages.PREFIX + "menus.main-menu.communications.lore"), false, "communications-menu", 15, null, null);
        }
    }

    public static class TrackerMenu extends CompassMenu {

        private final IArena arena;
        private final Map<Integer, ITeam> teamSlots = new HashMap<>();
        private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        private final boolean backToShop;

        public TrackerMenu(Player player, IArena arena, boolean backToShop) {
            super(player);
            this.arena = arena;
            this.backToShop = backToShop;
        }

        @Override
        protected String titlePath() {
            return CompassMessages.TRACKER_MENU_TITLE;
        }

        @Override
        protected int slots() {
            return 36;
        }

        @Override
        protected void handle(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            String data = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_DATA);
            if ("main-menu".equals(data)) {
                if (backToShop) {
                    com.tomkeuper.bedwars.shop.main.ShopIndex shop = com.tomkeuper.bedwars.shop.ShopManager.shop;
                    shop.open(p, com.tomkeuper.bedwars.shop.quickbuy.PlayerQuickBuyCache.getInstance().getQuickBuyCache(p.getUniqueId()), false);
                } else {
                    new MainMenu(p).open();
                }
                return;
            }
            if (!teamSlots.containsKey(e.getSlot())) return;

            ITeam ownTeam = arena.getTeam(p);
            if (!CompassTracking.allBedsDestroyed(ownTeam)) {
                p.closeInventory();
                p.sendMessage(Language.getMsg(p, CompassMessages.NOT_ALL_BEDS_DESTROYED));
                return;
            }

            Material currency = Material.EMERALD;
            int cost = 2;
            if (!p.getInventory().contains(currency, cost)) {
                p.closeInventory();
                p.sendMessage(Language.getMsg(p, CompassMessages.NOT_ENOUGH_RESOURCE));
                return;
            }

            p.closeInventory();

            ITeam target = teamSlots.get(e.getSlot());
            if (CompassTracking.isTracking(arena, p.getUniqueId())
                    && target.equals(CompassTracking.getTeam(arena, p.getUniqueId()))) {
                p.sendMessage(Language.getMsg(p, CompassMessages.ALREADY_TRACKING));
                return;
            }

            CompassTracking.startTaskIfAbsent(arena);
            CompassTracking.setTeam(arena, p.getUniqueId(), target);
            com.tomkeuper.bedwars.shop.main.CategoryContent.takeMoney(p, currency, cost);
            p.sendMessage(Language.getMsg(p, CompassMessages.PURCHASED));
        }

        @Override
        protected void setItems() {
            Language lang = Language.getPlayerLanguage(player);
            int index = 0;
            for (ITeam team : arena.getTeams()) {
                if (team.getMembers().isEmpty()) continue;
                if (team.equals(arena.getTeam(player))) continue;
                if (index >= SLOTS.length) break;
                teamSlots.put(SLOTS[index], team);
                inventory.setItem(SLOTS[index], teamItem(team, lang));
                index++;
            }
            inventory.setItem(31, CompassItemBuilder.build(Material.ARROW,
                    lang.m(CompassMessages.PREFIX + "menus.tracker-menu.back-item.display-name"),
                    lang.l(CompassMessages.PREFIX + "menus.tracker-menu.back-item.lore"), false, "main-menu", 31, null, null));
        }

        private ItemStack teamItem(ITeam team, Language lang) {
            String displayName = lang.m(CompassMessages.PREFIX + "menus.tracker-menu.team-item.display-name")
                    .replace("{team}", team.getDisplayName(lang));
            List<String> lore = new ArrayList<>();
            String status = CompassTracking.allBedsDestroyed(arena.getTeam(player))
                    ? Language.getMsg(player, CompassMessages.STATUS_UNLOCKED)
                    : Language.getMsg(player, CompassMessages.STATUS_LOCKED);
            for (String line : lang.l(CompassMessages.PREFIX + "menus.tracker-menu.team-item.lore")) {
                lore.add(line.replace("{status}", status));
            }
            ItemStack is = CompassItemBuilder.build(Material.WOOL, displayName, lore, false, "team-item", null, null, null);
            is = BedWars.nms.colourItem(is, team);
            if (is == null) {
                is = new ItemStack(Material.WOOL, 1, team.getColor().itemByte());
                ItemMeta meta = is.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                meta.setLore(lore);
                is.setItemMeta(meta);
            }
            return BedWars.nms.setTag(is, CompassItemBuilder.TAG_DATA, "team-item");
        }
    }

    public static class CommunicationsMenu extends CompassMenu {

        private final IArena arena;

        public CommunicationsMenu(Player player, IArena arena) {
            super(player);
            this.arena = arena;
        }

        @Override
        protected String titlePath() {
            return CompassMessages.COMMUNICATIONS_MENU_TITLE;
        }

        @Override
        protected int slots() {
            return 45;
        }

        @Override
        protected void handle(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            ITeam team = arena.getTeam(p);
            if (team == null || team.getMembers().size() <= 1) return;
            String data = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_DATA);
            if ("back-item".equals(data)) {
                new MainMenu(p).open();
            } else if ("communication-item".equals(data)) {
                String menuType = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_MENU);
                String path = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_PATH);
                switch (menuType) {
                    case "TEAM":
                        new TeamSelector(p, team, path).open();
                        break;
                    case "RESOURCE":
                        new ResourceSelector(p, team, path).open();
                        break;
                    default:
                        Messaging.simpleMessage(p, team, path);
                        break;
                }
            }
        }

        @Override
        protected void setItems() {
            Language lang = Language.getPlayerLanguage(player);
            int[] slots = {10, 11, 12, 13, 14, 15, 16, 20, 21, 22, 23, 24};
            String[] materials = {"BOOK", "BOOK", "IRON_FENCE", "IRON_SWORD", "DIAMOND", "CHEST", "BOOK", "BOOK", "IRON_FENCE", "IRON_SWORD", "DIAMOND", "FEATHER"};
            String[] menuTypes = {"NONE", "NONE", "NONE", "TEAM", "RESOURCE", "RESOURCE", "NONE", "NONE", "NONE", "TEAM", "RESOURCE", "NONE"};
            for (int i = 0; i < 12; i++) {
                String itemPath = CompassMessages.PREFIX + "menus.communications.items." + (i + 1);
                Material material = Material.valueOf(BedWars.getForCurrentVersion(materials[i], materials[i], materials[i].equals("IRON_FENCE") ? "IRON_BARS" : materials[i]));
                inventory.setItem(slots[i], CompassItemBuilder.build(material,
                        lang.m(itemPath + ".display-name"), lang.l(itemPath + ".lore"), false,
                        "communication-item", slots[i], itemPath + ".message", menuTypes[i]));
            }
            inventory.setItem(40, CompassItemBuilder.build(Material.ARROW,
                    lang.m(CompassMessages.PREFIX + "menus.communications.back-item.display-name"),
                    lang.l(CompassMessages.PREFIX + "menus.communications.back-item.lore"), false, "back-item", 40, null, null));
        }
    }

    public static class TeamSelector extends CompassMenu {

        private final ITeam team;
        private final String path;
        private final Map<Integer, ITeam> teamMap = new HashMap<>();
        private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        public TeamSelector(Player player, ITeam team, String path) {
            super(player);
            this.team = team;
            this.path = path;
        }

        @Override
        protected String titlePath() {
            return CompassMessages.COMMUNICATIONS_MENU_TEAMS_TITLE;
        }

        @Override
        protected int slots() {
            return 36;
        }

        @Override
        protected void handle(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            String data = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_DATA);
            if ("back-item".equals(data)) {
                new CommunicationsMenu(p, team.getArena()).open();
            } else if ("team-item".equals(data)) {
                Messaging.teamMessage(p, team, path, teamMap.get(e.getSlot()));
            }
        }

        @Override
        protected void setItems() {
            Language lang = Language.getPlayerLanguage(player);
            int index = 0;
            for (ITeam t : team.getArena().getTeams()) {
                if (t.getMembers().isEmpty() || t.equals(team)) continue;
                if (index >= SLOTS.length) break;
                inventory.setItem(SLOTS[index], teamItem(t, lang));
                teamMap.put(SLOTS[index], t);
                index++;
            }
            inventory.setItem(31, CompassItemBuilder.build(Material.ARROW,
                    lang.m(CompassMessages.PREFIX + "communication-menus.teams.back-item.display-name"),
                    lang.l(CompassMessages.PREFIX + "communication-menus.teams.back-item.lore"), false, "back-item", 31, null, null));
        }

        private ItemStack teamItem(ITeam t, Language lang) {
            String displayName = lang.m(path).replace("{team}", t.getColor().chat() + "§l" + t.getDisplayName(lang));
            List<String> lore = new ArrayList<>();
            for (String line : lang.l(CompassMessages.COMMUNICATIONS_MENU_LORE)) {
                lore.add(line.replace("{message}", displayName));
            }
            ItemStack is = CompassItemBuilder.build(Material.WOOL, displayName, lore, false, "team-item", null, path, null);
            is = BedWars.nms.colourItem(is, t);
            return BedWars.nms.setTag(is, CompassItemBuilder.TAG_DATA, "team-item");
        }
    }

    public static class ResourceSelector extends CompassMenu {

        private final ITeam team;
        private final String path;
        private final Map<Integer, String> resourceMap = new HashMap<>();
        private static final String[] RESOURCES = {"iron", "gold", "diamond", "emerald"};
        private static final int[] SLOTS = {10, 12, 14, 16};
        private static final Material[] MATERIALS = {Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.EMERALD};

        public ResourceSelector(Player player, ITeam team, String path) {
            super(player);
            this.team = team;
            this.path = path;
        }

        @Override
        protected String titlePath() {
            return CompassMessages.COMMUNICATIONS_MENU_RESOURCES_TITLE;
        }

        @Override
        protected int slots() {
            return 36;
        }

        @Override
        protected void handle(InventoryClickEvent e) {
            Player p = (Player) e.getWhoClicked();
            String data = CompassItemBuilder.tag(e.getCurrentItem(), CompassItemBuilder.TAG_DATA);
            if ("back-item".equals(data)) {
                new CommunicationsMenu(p, team.getArena()).open();
            } else if ("resource-item".equals(data)) {
                Messaging.resourceMessage(p, team, path, resourceMap.get(e.getSlot()));
            }
        }

        @Override
        protected void setItems() {
            Language lang = Language.getPlayerLanguage(player);
            for (int i = 0; i < RESOURCES.length; i++) {
                String resourcePath = CompassMessages.PREFIX + "communication-menus.resources." + RESOURCES[i];
                String displayName = lang.m(path).replace("{resource}", lang.m(resourcePath + ".resource-name"));
                List<String> lore = new ArrayList<>();
                for (String line : lang.l(CompassMessages.COMMUNICATIONS_MENU_LORE)) {
                    lore.add(line.replace("{message}", displayName));
                }
                inventory.setItem(SLOTS[i], CompassItemBuilder.build(MATERIALS[i], displayName, lore, false, "resource-item", SLOTS[i], path, RESOURCES[i]));
                resourceMap.put(SLOTS[i], RESOURCES[i]);
            }
            inventory.setItem(31, CompassItemBuilder.build(Material.ARROW,
                    lang.m(CompassMessages.PREFIX + "communication-menus.resources.back-item.display-name"),
                    lang.l(CompassMessages.PREFIX + "communication-menus.resources.back-item.lore"), false, "back-item", 31, null, null));
        }
    }

    public static class Messaging {

        private Messaging() {
        }

        public static void simpleMessage(Player player, ITeam team, String path) {
            player.closeInventory();
            for (Player p : team.getMembers()) {
                Language lang = Language.getPlayerLanguage(p);
                String formatted = lang.m(CompassMessages.TEAM_MESSAGE_FORMAT)
                        .replace("{player}", player.getDisplayName())
                        .replace("{message}", lang.m(path));
                p.sendMessage(formatted);
            }
        }

        public static void resourceMessage(Player player, ITeam team, String path, String resource) {
            player.closeInventory();
            for (Player p : team.getMembers()) {
                Language lang = Language.getPlayerLanguage(p);
                String resourceName = lang.m(CompassMessages.PREFIX + "communication-menus.resources." + resource + ".resource-name");
                String formatted = lang.m(CompassMessages.TEAM_MESSAGE_FORMAT)
                        .replace("{player}", player.getDisplayName())
                        .replace("{message}", lang.m(path).replace("{resource}", resourceName));
                p.sendMessage(formatted);
            }
        }

        public static void teamMessage(Player player, ITeam team, String path, ITeam specifiedTeam) {
            player.closeInventory();
            for (Player p : team.getMembers()) {
                Language lang = Language.getPlayerLanguage(p);
                String formatted = lang.m(CompassMessages.TEAM_MESSAGE_FORMAT)
                        .replace("{player}", player.getDisplayName())
                        .replace("{message}", lang.m(path).replace("{team}", specifiedTeam.getColor().chat() + "§l" + specifiedTeam.getDisplayName(lang)));
                p.sendMessage(formatted);
            }
        }
    }
}
