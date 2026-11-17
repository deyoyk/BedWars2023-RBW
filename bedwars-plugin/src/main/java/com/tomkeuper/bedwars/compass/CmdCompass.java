package com.tomkeuper.bedwars.compass;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdCompass extends SubCommand {

    public CmdCompass(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(18);
        showInList(false);
        setDisplayInfo(com.tomkeuper.bedwars.commands.bedwars.MainCommand.createTC(
                "§6 ▪ §7/" + com.tomkeuper.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " compass",
                "/" + getParent().getName() + " compass",
                "§fOpen the tracker & communications menu."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null || arena.getStatus() != GameState.playing || arena.isSpectator(p)) return true;
        new CompassMenus.MainMenu(p).open();
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return new ArrayList<>();
    }
}
