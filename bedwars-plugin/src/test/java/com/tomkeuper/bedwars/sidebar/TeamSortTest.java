package com.tomkeuper.bedwars.sidebar;

import com.tomkeuper.bedwars.api.arena.team.TeamColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamSortTest {

    @Test
    void prefixIsZeroPaddedOrdinal() {
        assertEquals("00", TeamSort.prefix(TeamColor.RED));
        assertEquals("01", TeamSort.prefix(TeamColor.BLUE));
        assertEquals("09", TeamSort.prefix(TeamColor.DARK_GRAY));
        assertEquals("10", TeamSort.prefix(TeamColor.BROWN));
    }

    @Test
    void prefixOrderMatchesEnumOrder() {
        TeamColor[] colors = TeamColor.values();
        for (int i = 0; i < colors.length; i++) {
            assertEquals(String.format("%02d", i), TeamSort.prefix(colors[i]));
        }
    }

    @Test
    void prefixesSortInTeamColorOrder() {
        String red = TeamSort.prefix(TeamColor.RED);
        String blue = TeamSort.prefix(TeamColor.BLUE);
        String yellow = TeamSort.prefix(TeamColor.YELLOW);
        String pink = TeamSort.prefix(TeamColor.PINK);
        assertTrue(red.compareTo(blue) < 0);
        assertTrue(blue.compareTo(yellow) < 0);
        assertTrue(yellow.compareTo(pink) < 0);
    }
}
