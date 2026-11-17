package com.tomkeuper.bedwars.arena.team;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TeamAssignerTest {

    private static ITeam team(String name, int size) {
        return (ITeam) Proxy.newProxyInstance(TeamAssignerTest.class.getClassLoader(), new Class[]{ITeam.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if (method.getName().equals("equals")) return proxy == args[0];
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        return null;
                    }
                    switch (method.getName()) {
                        case "getName":
                            return name;
                        case "getSize":
                        case "getMembers":
                            if (method.getName().equals("getMembers")) return membersOfSize(size);
                            return size;
                        default:
                            return null;
                    }
                });
    }

    private static List<Object> membersOfSize(int size) {
        List<Object> members = new ArrayList<>();
        for (int i = 0; i < size; i++) members.add(new Object());
        return members;
    }

    private static List<ITeam> teams(ITeam... t) {
        return new ArrayList<>(java.util.Arrays.asList(t));
    }

    private static ITeam findTarget(List<ITeam> teams, int maxPerTeam, int playerAmount) throws Exception {
        java.lang.reflect.Method m = TeamAssigner.class.getDeclaredMethod("findTargetTeam", List.class, int.class, int.class);
        m.setAccessible(true);
        return (ITeam) m.invoke(null, teams, maxPerTeam, playerAmount);
    }

    @Test
    void returnsTeamWithFewestPlayersUnderCap() throws Exception {
        ITeam red = team("RED", 2);
        ITeam blue = team("BLUE", 0);
        List<ITeam> teams = teams(red, blue);
        assertSame(blue, findTarget(teams, 4, 3));
    }

    @Test
    void groupsSoloPlayersWhenMoreThanTwoPlayers() throws Exception {
        ITeam red = team("RED", 1);
        ITeam blue = team("BLUE", 1);
        List<ITeam> teams = teams(red, blue);
        assertSame(red, findTarget(teams, 4, 5));
    }

    @Test
    void fallsBackToFirstOpenTeam() throws Exception {
        ITeam red = team("RED", 3);
        ITeam blue = team("BLUE", 3);
        List<ITeam> teams = teams(red, blue);
        assertSame(red, findTarget(teams, 4, 8));
    }

    @Test
    void returnsNullWhenAllTeamsFull() throws Exception {
        ITeam red = team("RED", 4);
        ITeam blue = team("BLUE", 4);
        List<ITeam> teams = teams(red, blue);
        assertNull(findTarget(teams, 4, 8));
    }

    @Test
    void skipsFullTeamsInFirstPass() throws Exception {
        ITeam red = team("RED", 2);
        ITeam blue = team("BLUE", 2);
        ITeam green = team("GREEN", 0);
        List<ITeam> teams = teams(red, blue, green);
        assertSame(green, findTarget(teams, 4, 7));
    }

    @Test
    void assignmentIsRepeatable() throws Exception {
        List<ITeam> teams = teams(team("RED", 0), team("BLUE", 0));
        assertSame(teams.get(0), findTarget(teams, 4, 3));
        assertSame(teams.get(0), findTarget(teams, 4, 3));
        assertEquals(2, teams.size());
    }
}
