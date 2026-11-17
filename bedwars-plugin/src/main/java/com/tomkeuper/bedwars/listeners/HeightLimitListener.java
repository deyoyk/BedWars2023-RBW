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

package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeightLimitListener implements Listener {

    public static final String COUNTDOWN = "addons.heightlimit.countdown";
    public static final String REACHED = "addons.heightlimit.reached";

    static final int RANGE = 5;
    static final int FAR = -1;

    private final Map<UUID, Integer> lastShown = new HashMap<>();

    public static void initDefaults() {
        Language.saveIfNotExists(COUNTDOWN, "&eHeight limit in &c{n}");
        Language.saveIfNotExists(REACHED, "&c&lHeight limit reached!");
    }

    static int proximity(int maxBuildY, int y) {
        int remaining = maxBuildY - y;
        if (remaining > RANGE) return FAR;
        return Math.max(remaining, 0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null || e.getFrom().getBlockY() == e.getTo().getBlockY()) return;
        Player p = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null || arena.getStatus() != GameState.playing || arena.isSpectator(p)) return;
        int maxBuildY = arena.getConfig().getInt(ConfigPath.ARENA_CONFIGURATION_MAX_BUILD_Y);
        int shown = proximity(maxBuildY, e.getTo().getBlockY());
        if (shown == FAR) {
            lastShown.remove(p.getUniqueId());
            return;
        }
        Integer last = lastShown.get(p.getUniqueId());
        if (last != null && last == shown) return;
        lastShown.put(p.getUniqueId(), shown);
        String msg = shown == 0
                ? Language.getMsg(p, REACHED)
                : Language.getMsg(p, COUNTDOWN).replace("{n}", String.valueOf(shown));
        BedWars.nms.playAction(p, msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastShown.remove(e.getPlayer().getUniqueId());
    }
}
