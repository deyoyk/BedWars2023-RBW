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

package com.tomkeuper.bedwars.listeners.antidrop;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.ArrayList;
import java.util.List;

public class AntiDropListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;
        if (arena.getStatus() != GameState.playing) return;
        List<Block> blocks = new ArrayList<>();
        blocks.add(player.getLocation().clone().subtract(0.0D, 0.1D, 0.0D).getBlock());
        for (int i = 1; i <= 4; i++) {
            blocks.add(player.getLocation().clone().subtract(0.0D, i, 0.0D).getBlock());
        }
        for (Block block : blocks) {
            if (block.getType().equals(Material.AIR)) {
                continue;
            }
            return;
        }
        e.setCancelled(true);
    }
}
