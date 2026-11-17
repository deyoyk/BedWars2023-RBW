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

package com.tomkeuper.bedwars.sponge;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SpongeListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onSpongePlace(BlockPlaceEvent e) {
        IArena arena = Arena.getArenaByPlayer(e.getPlayer());
        if (arena == null || arena.getStatus() != GameState.playing) return;
        if (e.getBlock().getType() != Material.SPONGE) return;
        new SpongeAnimationTask(e.getBlock()).runTaskTimer(BedWars.plugin, 0L, 8L);
    }

    private static class SpongeAnimationTask extends BukkitRunnable {

        private final Block block;
        private final Location location;

        private int radius = 1;
        private int pitch = 17;

        private SpongeAnimationTask(Block block) {
            this.block = block;
            this.location = block.getLocation();
        }

        @Override
        public void run() {
            if (radius > 4) {
                cancel();
                block.setType(Material.AIR);
                location.getWorld().playSound(location, Sound.SPLASH, 1, 1);
                return;
            }
            for (Location particle : particles(location, radius)) {
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, true,
                        (float) particle.getX(), (float) particle.getY(), (float) particle.getZ(),
                        0.0F, 0.0F, 0.0F, 0.1F, 1);
                for (Player p : location.getWorld().getPlayers()) {
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                }
            }
            location.getWorld().playSound(location, Sound.WOOD_CLICK, 1.0F, pitch / 10F);
            radius++;
            pitch++;
        }

        private static java.util.List<Location> particles(Location loc, int radius) {
            java.util.List<Location> result = new java.util.ArrayList<>();
            Block start = loc.getWorld().getBlockAt(loc);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block b = start.getRelative(x, y, z);
                        if (b.getType() == Material.AIR) result.add(b.getLocation());
                    }
                }
            }
            return result;
        }
    }
}
