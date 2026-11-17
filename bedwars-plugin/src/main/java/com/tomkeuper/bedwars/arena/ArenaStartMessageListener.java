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

package com.tomkeuper.bedwars.arena;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaStartMessageListener implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent e) {
        Arena arena = (Arena) e.getArena();
        GameState state = e.getNewState();
        if (state != GameState.waiting && state != GameState.starting) return;
        if (state == GameState.waiting && arena.getPlayers().size() < 1) return;

        World lobby = Bukkit.getServer().getWorld(BedWars.getLobbyWorld());
        new BukkitRunnable() {
            @Override
            public void run() {
                if (lobby == null) return;
                broadcast(buildMessage(arena), lobby);
            }
        }.runTaskLater(BedWars.plugin, 10L);
    }

    static TextComponent buildMessage(Arena arena) {
        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                rawText(arena.getDisplayName(), arena.getGroup(), arena.getStatus() == GameState.starting)));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bw join " + arena.getArenaName()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(ChatColor.translateAlternateColorCodes('&', "&a&lLEFT CLICK TO JOIN!"))}));
        return message;
    }

    static String rawText(String displayName, String group, boolean starting) {
        String stateText = starting ? "about to &bstart!" : "waiting &bfor more players to start!";
        return ("&bThere is a &e&lBedWars {arenagroup} &bgame in arena &e&l{arena}&b " + stateText
                + " &6&lCLICK HERE &bto join!")
                .replace("{arena}", displayName)
                .replace("{arenagroup}", group);
    }

    static void broadcast(TextComponent message, World lobby) {
        for (Player p : lobby.getPlayers()) {
            p.spigot().sendMessage(message);
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 10f, 10f);
        }
    }
}
