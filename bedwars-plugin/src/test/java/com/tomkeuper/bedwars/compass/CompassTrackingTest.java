package com.tomkeuper.bedwars.compass;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompassTrackingTest {

    private static ITeam team(com.tomkeuper.bedwars.api.arena.team.TeamColor color, IArena arena, boolean bedDestroyed) {
        return (ITeam) Proxy.newProxyInstance(CompassTrackingTest.class.getClassLoader(), new Class[]{ITeam.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if (method.getName().equals("equals")) return proxy == args[0];
                        return defaultValue(method.getReturnType());
                    }
                    switch (method.getName()) {
                        case "getColor":
                            return color;
                        case "getArena":
                            return arena;
                        case "isBedDestroyed":
                            return bedDestroyed;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                });
    }

    private static IArena arena(ITeam... teams) {
        return arenaOf(new java.util.ArrayList<>(java.util.Arrays.asList(teams)));
    }

    private static IArena arenaOf(java.util.List<ITeam> teamList) {
        return (IArena) Proxy.newProxyInstance(CompassTrackingTest.class.getClassLoader(), new Class[]{IArena.class},
                (proxy, method, args) -> method.getName().equals("getTeams")
                        ? teamList
                        : defaultValue(method.getReturnType()));
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        return null;
    }

    @Test
    void setTeamThenIsTracking() {
        IArena a = arena();
        UUID uuid = UUID.randomUUID();
        assertFalse(CompassTracking.isTracking(a, uuid));
        CompassTracking.setTeam(a, uuid, team(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED, a, true));
        assertTrue(CompassTracking.isTracking(a, uuid));
        assertEquals(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED,
                CompassTracking.getTeam(a, uuid).getColor());
    }

    @Test
    void removePlayerStopsTracking() {
        IArena a = arena();
        UUID uuid = UUID.randomUUID();
        CompassTracking.setTeam(a, uuid, team(com.tomkeuper.bedwars.api.arena.team.TeamColor.BLUE, a, true));
        CompassTracking.removePlayer(a, uuid);
        assertFalse(CompassTracking.isTracking(a, uuid));
        assertNull(CompassTracking.getTeam(a, uuid));
    }

    @Test
    void removeArenaClearsAllPlayers() {
        IArena a = arena();
        CompassTracking.setTeam(a, UUID.randomUUID(), team(com.tomkeuper.bedwars.api.arena.team.TeamColor.GREEN, a, true));
        CompassTracking.setTeam(a, UUID.randomUUID(), team(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED, a, true));
        CompassTracking.removeArena(a);
        assertTrue(CompassTracking.isEmpty(a));
    }

    @Test
    void emptyArenaHasNoTracking() {
        assertTrue(CompassTracking.isEmpty(arena()));
        assertFalse(CompassTracking.isTracking(arena(), UUID.randomUUID()));
    }

    @Test
    void allBedsDestroyedFalseWhileAnyEnemyBedAlive() {
        java.util.List<ITeam> teamList = new java.util.ArrayList<>();
        IArena full = arenaOf(teamList);
        ITeam red = team(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED, full, true);
        ITeam blue = team(com.tomkeuper.bedwars.api.arena.team.TeamColor.BLUE, full, false);
        teamList.add(red);
        teamList.add(blue);
        assertFalse(CompassTracking.allBedsDestroyed(red));
    }

    @Test
    void allBedsDestroyedTrueWhenEveryEnemyBedGone() {
        java.util.List<ITeam> teamList = new java.util.ArrayList<>();
        IArena full = arenaOf(teamList);
        ITeam red = team(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED, full, true);
        ITeam blue = team(com.tomkeuper.bedwars.api.arena.team.TeamColor.BLUE, full, true);
        teamList.add(red);
        teamList.add(blue);
        assertTrue(CompassTracking.allBedsDestroyed(red));
    }

    @Test
    void allBedsDestroyedIgnoresOwnTeamOnly() {
        java.util.List<ITeam> teamList = new java.util.ArrayList<>();
        IArena full = arenaOf(teamList);
        ITeam red = team(com.tomkeuper.bedwars.api.arena.team.TeamColor.RED, full, false);
        teamList.add(red);
        assertTrue(CompassTracking.allBedsDestroyed(red));
    }
}
