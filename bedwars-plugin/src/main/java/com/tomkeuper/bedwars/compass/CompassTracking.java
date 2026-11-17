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
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CompassTracking {

    private static final Map<IArena, Map<UUID, ITeam>> tracking = new HashMap<>();

    public static void setTeam(IArena arena, UUID uuid, ITeam team) {
        tracking.computeIfAbsent(arena, k -> new HashMap<>()).put(uuid, team);
    }

    public static boolean isTracking(IArena arena, UUID uuid) {
        Map<UUID, ITeam> map = tracking.get(arena);
        return map != null && map.containsKey(uuid);
    }

    public static ITeam getTeam(IArena arena, UUID uuid) {
        Map<UUID, ITeam> map = tracking.get(arena);
        return map == null ? null : map.get(uuid);
    }

    public static void removePlayer(IArena arena, UUID uuid) {
        Map<UUID, ITeam> map = tracking.get(arena);
        if (map != null) map.remove(uuid);
    }

    public static void removeArena(IArena arena) {
        tracking.remove(arena);
    }

    public static boolean isEmpty(IArena arena) {
        Map<UUID, ITeam> map = tracking.get(arena);
        return map == null || map.isEmpty();
    }

    public static void startTaskIfAbsent(IArena arena) {
        if (tracking.containsKey(arena)) return;
        new ActionBarTask(arena).runTaskTimer(BedWars.plugin, 0L, 10L);
    }

    public static boolean allBedsDestroyed(ITeam exclude) {
        boolean result = true;
        for (ITeam team : exclude.getArena().getTeams()) {
            if (team.equals(exclude)) continue;
            if (team.isBedDestroyed()) continue;
            result = false;
            break;
        }
        return result;
    }

    public static Player nearest(Player player, ITeam team) {
        Player nearest = null;
        double distance = Double.MAX_VALUE;
        for (Player p : team.getMembers()) {
            if (!player.getWorld().equals(p.getWorld())) continue;
            double d = player.getLocation().distance(p.getLocation());
            if (d < distance) {
                distance = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private static class ActionBarTask extends BukkitRunnable {

        private final IArena arena;

        private ActionBarTask(IArena arena) {
            this.arena = arena;
        }

        @Override
        public void run() {
            if (arena.getStatus() != com.tomkeuper.bedwars.api.arena.GameState.playing || isEmpty(arena)) {
                cancel();
                removeArena(arena);
                return;
            }
            Map<UUID, ITeam> map = new HashMap<>(tracking.getOrDefault(arena, new HashMap<>()));
            for (Map.Entry<UUID, ITeam> entry : map.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null) continue;
                Player target = nearest(player, entry.getValue());
                if (target == null) continue;
                player.setCompassTarget(target.getLocation());
                sendActionBar(player, entry.getValue(), target, (int) player.getLocation().distance(target.getLocation()));
            }
        }

        private void sendActionBar(Player player, ITeam team, Player target, int distance) {
            String format = com.tomkeuper.bedwars.api.language.Language.getMsg(player, CompassMessages.ACTION_BAR_TRACKING);
            String msg = format
                    .replace("{teamColor}", "§" + team.getColor().chat().getChar())
                    .replace("{target}", target.getDisplayName())
                    .replace("{distance}", String.valueOf(distance));
            BedWars.nms.playAction(player, msg);
        }
    }
}
