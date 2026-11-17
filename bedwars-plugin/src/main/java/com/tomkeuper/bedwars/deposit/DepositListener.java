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

package com.tomkeuper.bedwars.deposit;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DepositListener implements Listener {

    private static final long DEPOSIT_COOLDOWN_MS = 500L;
    private final Map<UUID, Long> lastDeposit = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST && block.getType() != Material.ENDER_CHEST) return;

        Player p = e.getPlayer();
        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null) return;
        if (arena.getStatus() != GameState.playing) return;
        if (arena.isSpectator(p) || arena.getRespawnSessions().containsKey(p)) return;

        e.setCancelled(true);

        long now = System.currentTimeMillis();
        Long last = lastDeposit.get(p.getUniqueId());
        if (last != null && now - last < DEPOSIT_COOLDOWN_MS) return;
        lastDeposit.put(p.getUniqueId(), now);

        Inventory target;
        if (block.getType() == Material.ENDER_CHEST) {
            target = p.getEnderChest();
        } else {
            if (!(block.getState() instanceof Chest)) return;
            target = ((Chest) block.getState()).getInventory();
        }

        ItemStack held = BedWars.nms.getItemInHand(p);
        if (held == null || held.getType() == Material.AIR) return;
        if (BedWars.nms.isSword(held) || BedWars.nms.isTool(held) || BedWars.nms.isBow(held) || BedWars.nms.isArmor(held)) return;
        if (held.getType() == Material.COMPASS) return;

        Map<Integer, ItemStack> leftover = target.addItem(held.clone());
        int rejected = 0;
        for (ItemStack item : leftover.values()) {
            rejected += item.getAmount();
        }
        int accepted = held.getAmount() - rejected;
        if (accepted <= 0) {
            p.sendMessage(org.bukkit.ChatColor.RED + "That chest doesn't have enough space!");
            return;
        }

        if (rejected == 0) {
            p.getInventory().setItemInHand(new ItemStack(Material.AIR));
        } else {
            ItemStack remainder = held.clone();
            remainder.setAmount(rejected);
            p.getInventory().setItemInHand(remainder);
        }

        p.getWorld().playSound(p.getLocation(), Sound.valueOf(BedWars.getForCurrentVersion("CHEST_CLOSE", "BLOCK_CHEST_CLOSE", "BLOCK_CHEST_CLOSE")), 1.0F, 1.0F);
        p.updateInventory();
    }
}
