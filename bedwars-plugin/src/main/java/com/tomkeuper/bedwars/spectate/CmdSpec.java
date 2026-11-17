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

package com.tomkeuper.bedwars.spectate;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdSpec extends SubCommand {

    public static final String PREFIX = "addons.spectate.";

    public CmdSpec(ParentCommand parent, String name) {
        super(parent, name);
        setPermission("bw.spec");
        setPriority(19);
        showInList(false);
        setDisplayInfo(com.tomkeuper.bedwars.commands.bedwars.MainCommand.createTC(
                "§6 ▪ §7/" + com.tomkeuper.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " spec §e<player>",
                "/" + getParent().getName() + " " + getSubCommandName(),
                "§fSpectate a player's game."));
    }

    public static void initDefaults() {
        Language.saveIfNotExists(PREFIX + "usage", "&cUsage: /bw spec <player>");
        Language.saveIfNotExists(PREFIX + "no-permission", "&cYou do not have permission to use this command!");
        Language.saveIfNotExists(PREFIX + "spectate-yourself", "&cYou cannot spectate yourself!");
        Language.saveIfNotExists(PREFIX + "player-not-playing", "&c{player} is not playing anything!");
        Language.saveIfNotExists(PREFIX + "started-spectating", "&aYou started spectating {target}!");
        Language.saveIfNotExists(PREFIX + "notify-spectating", "&e{player} &astarted spectating you!");
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof Player)) return true;
        Player p = (Player) s;
        if (!hasPermission(p)) {
            p.sendMessage(Language.getMsg(p, PREFIX + "no-permission"));
            return true;
        }
        if (args.length != 1) {
            p.sendMessage(Language.getMsg(p, PREFIX + "usage"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || target.equals(p)) {
            p.sendMessage(Language.getMsg(p, PREFIX + "player-not-playing").replace("{player}", args[0]));
            return true;
        }
        IArena arena = Arena.getArenaByPlayer(target);
        if (arena == null || arena.getStatus() != GameState.playing || arena.isSpectator(target)) {
            p.sendMessage(Language.getMsg(p, PREFIX + "player-not-playing").replace("{player}", target.getName()));
            return true;
        }
        if (!arena.addSpectator(p, false, target.getLocation())) return true;
        p.sendMessage(Language.getMsg(p, PREFIX + "started-spectating").replace("{target}", target.getDisplayName()));
        if (target.hasPermission("bw.spec.notify")) {
            target.sendMessage(Language.getMsg(target, PREFIX + "notify-spectating").replace("{player}", p.getDisplayName()));
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> players = new ArrayList<>();
        for (IArena a : Arena.getArenas()) {
            for (Player p : a.getPlayers()) {
                players.add(p.getName());
            }
        }
        return players;
    }
}
