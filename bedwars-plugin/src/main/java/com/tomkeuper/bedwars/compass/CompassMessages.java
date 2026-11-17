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

import com.tomkeuper.bedwars.api.language.Language;

public final class CompassMessages {

    public static final String PREFIX = "addons.compass.";

    public static final String NOT_ALL_BEDS_DESTROYED = PREFIX + "messages.not-all-beds-destroyed";
    public static final String PURCHASED = PREFIX + "messages.purchase-message";
    public static final String NOT_ENOUGH_RESOURCE = PREFIX + "messages.not-enough-resource";
    public static final String ALREADY_TRACKING = PREFIX + "messages.already-tracking";
    public static final String STATUS_UNLOCKED = PREFIX + "tracker-status.unlocked";
    public static final String STATUS_LOCKED = PREFIX + "tracker-status.locked";
    public static final String STATUS_NOT_ENOUGH = PREFIX + "tracker-status.not-enough";
    public static final String ACTION_BAR_TRACKING = PREFIX + "action-bar.tracking-format";
    public static final String TEAM_MESSAGE_FORMAT = PREFIX + "team-message-format";
    public static final String MAIN_MENU_TITLE = PREFIX + "menus.main-menu.title";
    public static final String TRACKER_MENU_TITLE = PREFIX + "menus.tracker-menu.title";
    public static final String COMMUNICATIONS_MENU_TITLE = PREFIX + "menus.communications.title";
    public static final String COMMUNICATIONS_MENU_LORE = PREFIX + "communication-menus.lore";
    public static final String COMMUNICATIONS_MENU_TEAMS_TITLE = PREFIX + "communication-menus.teams.title";
    public static final String COMMUNICATIONS_MENU_RESOURCES_TITLE = PREFIX + "communication-menus.resources.title";

    private CompassMessages() {
    }

    public static void initDefaults() {
        Language.saveIfNotExists(NOT_ALL_BEDS_DESTROYED, "&cNot all enemy beds are destroyed yet!");
        Language.saveIfNotExists(PURCHASED, "&cYou will lose the ability to track this team when you die!");
        Language.saveIfNotExists(NOT_ENOUGH_RESOURCE, "&cYou don't have enough resources!");
        Language.saveIfNotExists(ALREADY_TRACKING, "&cYou are already tracking this team!");
        Language.saveIfNotExists(STATUS_UNLOCKED, "&eClick to purchase!");
        Language.saveIfNotExists(STATUS_LOCKED, "&cUnlocks when all enemy beds are destroyed!");
        Language.saveIfNotExists(STATUS_NOT_ENOUGH, "&cYou don't have enough resource!");
        Language.saveIfNotExists(ACTION_BAR_TRACKING, "&fTracking: {teamColor}&l{target} &f- Distance: &a&l{distance}m");
        Language.saveIfNotExists(TEAM_MESSAGE_FORMAT, "&a&lTEAM > &7{player}: {message}");
        Language.saveIfNotExists(MAIN_MENU_TITLE, "&8Tracker & Communications");
        Language.saveIfNotExists(TRACKER_MENU_TITLE, "&8Purchase Enemy Tracker");
        Language.saveIfNotExists(COMMUNICATIONS_MENU_TITLE, "&8Quick Communications");
        Language.saveIfNotExists(COMMUNICATIONS_MENU_LORE, java.util.Arrays.asList(
                "&7Click to send message: '{message}&7'", "&7to your teammates!", "", "&eClick to send!"));
        Language.saveIfNotExists(COMMUNICATIONS_MENU_TEAMS_TITLE, "&8Select an option:");
        Language.saveIfNotExists(COMMUNICATIONS_MENU_RESOURCES_TITLE, "&8Select an option:");
        itemDefaults("compass-item", "&aCompass &7(Right Click)", "&7Right-click to open the tracker menu.");
        itemDefaults("tracker-shop", "&aTracker Shop", "&7Purchase tracking update for", "&7your compass which will track", "&7each player on a specific team", "&7until you die.");
        itemDefaults("menus.main-menu.tracker", "&aTracker Shop", "&7Purchase tracking upgrade", "&7for your compass which will", "&7track each player on a", "&7specific team until you", "&7die.", "", "&eClick to open!");
        itemDefaults("menus.main-menu.communications", "&aQuick Communications", "&7Send highlighted chat", "&7messages to your teammates!", "", "&eClick to open!");
        itemDefaults("menus.tracker-menu.team-item", "&cTrack Team {team}", "&7Purchase tracking upgrade", "&7for your compass which will", "&7track each player on a", "&7specific team until you", "&7die.", "", "&7Cost: &22 Emeralds", "", "{status}");
        itemDefaults("menus.tracker-menu.back-item", "&aGo Back", "&7To Tracker & Communication");
        itemDefaults("menus.communications.back-item", "&aGo Back", "&7To Tracker & Communication");
        itemDefaults("communication-menus.teams.back-item", "&aGo Back", "&7To Quick Communications");
        itemDefaults("communication-menus.resources.back-item", "&aGo Back", "&7To Quick Communications");
        Language.saveIfNotExists(PREFIX + "communication-menus.resources.iron.resource-name", "&f&lIRON");
        Language.saveIfNotExists(PREFIX + "communication-menus.resources.gold.resource-name", "&6&lGOLD");
        Language.saveIfNotExists(PREFIX + "communication-menus.resources.diamond.resource-name", "&b&lDIAMOND");
        Language.saveIfNotExists(PREFIX + "communication-menus.resources.emerald.resource-name", "&2&lEMERALD");
        for (int i = 1; i <= 12; i++) {
            Language.saveIfNotExists(PREFIX + "menus.communications.items." + i + ".message", "");
            Language.saveIfNotExists(PREFIX + "menus.communications.items." + i + ".display-name", "");
            Language.saveIfNotExists(PREFIX + "menus.communications.items." + i + ".lore", new String[]{""});
        }
    }

    private static void itemDefaults(String path, String displayName, String... lore) {
        Language.saveIfNotExists(PREFIX + path + ".display-name", displayName);
        Language.saveIfNotExists(PREFIX + path + ".lore", lore);
    }
}
