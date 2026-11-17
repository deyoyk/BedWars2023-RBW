package com.tomkeuper.bedwars.arena.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GGFeatureTest {

    @Test
    void namedColorResolvesToBukkitChatColor() {
        assertEquals("§6", GGFeature.parseColor("GOLD"));
        assertEquals("§a", GGFeature.parseColor("GREEN"));
    }

    @Test
    void legacyCodeIsTranslated() {
        assertEquals("§b", GGFeature.parseColor("&b"));
        assertEquals("§c§l", GGFeature.parseColor("&c&l"));
    }

    @Test
    void invalidNameFallsBackToLegacyTranslation() {
        assertEquals("NOT_A_COLOR", GGFeature.parseColor("NOT_A_COLOR"));
        assertEquals("zz", GGFeature.parseColor("zz"));
    }

    @Test
    void namedColorIsCaseInsensitive() {
        assertEquals("§6", GGFeature.parseColor("gold"));
    }
}
