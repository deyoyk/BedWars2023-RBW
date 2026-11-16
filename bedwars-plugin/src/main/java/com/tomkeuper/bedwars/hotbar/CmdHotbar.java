package com.tomkeuper.bedwars.hotbar;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdHotbar extends SubCommand {

    public CmdHotbar(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(17);
        showInList(true);
        setDisplayInfo(com.tomkeuper.bedwars.commands.bedwars.MainCommand.createTC(
                "§6 ▪ §7/" + com.tomkeuper.bedwars.commands.bedwars.MainCommand.getInstance().getName() + " " + getSubCommandName(),
                "/" + getParent().getName() + " " + getSubCommandName(),
                "§fCustomize your shop item hotbar layout."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        if (!HotbarPlayerManager.getInstance().isLoaded(p.getUniqueId())) {
            com.tomkeuper.bedwars.BedWars.plugin.getServer().getScheduler().runTaskAsynchronously(
                    com.tomkeuper.bedwars.BedWars.plugin, () -> {
                        HotbarPlayerManager.getInstance().load(p);
                        com.tomkeuper.bedwars.BedWars.plugin.getServer().getScheduler().runTask(
                                com.tomkeuper.bedwars.BedWars.plugin, () -> HotbarMenu.open(p));
                    });
            return true;
        }
        HotbarMenu.open(p);
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return new ArrayList<>();
    }
}
