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
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerKillEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerReSpawnEvent;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class CompassListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena != null) CompassTracking.removePlayer(arena, player.getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent e) {
        CompassTracking.removePlayer(e.getArena(), e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKill(PlayerKillEvent e) {
        IArena arena = e.getArena();
        CompassTracking.removePlayer(arena, e.getVictim().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        giveCompass(e.getPlayer());
    }

    @EventHandler
    public void onStateChange(GameStateChangeEvent e) {
        IArena arena = e.getArena();
        if (e.getNewState() == GameState.playing) {
            for (Player p : arena.getPlayers()) {
                giveCompass(p);
            }
        } else if (e.getNewState() == GameState.restarting) {
            CompassTracking.removeArena(arena);
        }
    }

    @EventHandler
    public void onCompassDrop(ItemSpawnEvent e) {
        ItemStack is = e.getEntity().getItemStack();
        if (CompassItemBuilder.isCompass(is)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCompassClick(InventoryClickEvent e) {
        ItemStack current = e.getCurrentItem();
        if (!CompassItemBuilder.isCompass(current)) return;
        if (e.getClick().isShiftClick()) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory() != e.getView().getBottomInventory()) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCompassDrag(InventoryDragEvent e) {
        for (ItemStack is : e.getNewItems().values()) {
            if (CompassItemBuilder.isCompass(is)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!e.getAction().name().contains("RIGHT_CLICK")) return;
        ItemStack item = BedWars.nms.getItemInHand(e.getPlayer());
        if (!CompassItemBuilder.isCompass(item)) return;
        e.setCancelled(true);
        Player player = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || arena.getStatus() != GameState.playing || arena.isSpectator(player)) return;
        new CompassMenus.MainMenu(player).open();
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof CompassMenu)) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (e.getClickedInventory() != e.getInventory()) return;
        ((CompassMenu) e.getInventory().getHolder()).handle(e);
    }

    private void giveCompass(Player p) {
        p.getInventory().setItem(8, CompassItemBuilder.build(Material.COMPASS,
                com.tomkeuper.bedwars.api.language.Language.getMsg(p, CompassMessages.PREFIX + "compass-item.display-name"),
                com.tomkeuper.bedwars.api.language.Language.getPlayerLanguage(p).l(CompassMessages.PREFIX + "compass-item.lore"),
                false, "compass-item", 8, null, null));
    }
}
