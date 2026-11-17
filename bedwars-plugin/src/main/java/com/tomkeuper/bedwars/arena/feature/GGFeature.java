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

package com.tomkeuper.bedwars.arena.feature;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.configuration.Permissions;
import com.tomkeuper.bedwars.listeners.chat.ChatFormatting;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GGFeature implements Listener {

    private final List<UUID> winners = new CopyOnWriteArrayList<>();

    private final String gg;

    public GGFeature() {
        this.gg = parseColor(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_GOLDEN_GG_COLOR));
    }

    static String parseColor(String raw) {
        try {
            return ChatColor.valueOf(raw.toUpperCase()).toString();
        } catch (IllegalArgumentException ex) {
            return ChatColor.translateAlternateColorCodes('&', raw);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameEnd(GameEndEvent event) {
        for (UUID uuid : event.getWinners()) {
            if (Bukkit.getPlayer(uuid) == null) continue;
            winners.add(uuid);
        }
        long delay = BedWars.config.getInt(ConfigPath.GENERAL_CONFIGURATION_RESTART) * 20L;
        Bukkit.getScheduler().runTaskLaterAsynchronously(BedWars.plugin, winners::clear, delay);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_GOLDEN_GG_ENABLED)) return;
        Player player = event.getPlayer();
        if (!player.hasPermission(Permissions.PERMISSION_GOLDEN_GG)) return;
        if (!winners.contains(player.getUniqueId())) return;
        String message = event.getMessage();
        if (message.toLowerCase().contains("gg")) {
            event.setMessage(message.replaceAll("(?i)gg", gg + "gg§r"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void autoGG(GameEndEvent event) {
        if (!BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_AUTO_GG_ENABLED)) return;
        IArena arena = event.getArena();
        for (Player viewer : arena.getPlayers()) {
            broadcast(viewer, event.getWinners(), arena);
        }
        for (Player viewer : arena.getSpectators()) {
            broadcast(viewer, event.getWinners(), arena);
        }
    }

    private void broadcast(Player viewer, List<UUID> winnerUuids, IArena arena) {
        for (UUID w : winnerUuids) {
            Player winner = Bukkit.getPlayer(w);
            if (winner == null) continue;
            if (!winner.hasPermission(Permissions.PERMISSION_AUTO_GG)) continue;
            ITeam team = arena.getTeam(winner);
            if (team == null) continue;
            Component message = ChatFormatting.parseLegacyMini(BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_AUTO_GG_TEXT)
                    .replaceAll("§", "&")
                    .replace("%bw_player%", winner.getName())
                    .replace("%bw_level%", BedWars.getAPI().getLevelsUtil().getLevel(winner))
                    .replace("%bw_team_color%", team.getColor().chat() + "[" + team.getDisplayName(Language.getPlayerLanguage(winner)).toUpperCase() + "]"));
            BedWars.plugin.adventure().player(viewer).sendMessage(message);
        }
    }
}
