package com.tomkeuper.bedwars.hotbar;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.events.player.PlayerReSpawnEvent;
import com.tomkeuper.bedwars.api.events.shop.ShopBuyEvent;
import com.tomkeuper.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Loads player hotbar layout on join, applies hotbar order on respawn and
 * persists on quit. Buy-time placement happens in the shop give flow via
 * {@link HotbarPlacement#place}.
 */
public class HotbarListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(BedWars.plugin, () ->
                HotbarPlayerManager.getInstance().load(e.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        HotbarPlayerManager.getInstance().unload(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () ->
                HotbarPlacement.sortOnRespawn(e.getPlayer()), 2L);
    }
}
