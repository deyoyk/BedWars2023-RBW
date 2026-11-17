package com.tomkeuper.bedwars.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArenaStartMessageTest {

    @Test
    void waitingTextContainsPlaceholdersReplaced() {
        String text = ArenaStartMessageListener.rawText("Arena1", "Doubles", false);
        assertEquals("&bThere is a &e&lBedWars Doubles &bgame in arena &e&lArena1"
                + "&b waiting &bfor more players to start! &6&lCLICK HERE &bto join!", text);
    }

    @Test
    void startingTextContainsPlaceholdersReplaced() {
        String text = ArenaStartMessageListener.rawText("Arena2", "4v4v4v4", true);
        assertEquals("&bThere is a &e&lBedWars 4v4v4v4 &bgame in arena &e&lArena2"
                + "&b about to &bstart! &6&lCLICK HERE &bto join!", text);
    }

    @Test
    void textHasNoUnresolvedPlaceholders() {
        assertTrue(!ArenaStartMessageListener.rawText("a", "g", false).contains("{arena}"));
        assertTrue(!ArenaStartMessageListener.rawText("a", "g", true).contains("{arenagroup}"));
    }
}
