package com.tomkeuper.bedwars.hotbar;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotbarPlayerManagerTest {

    @Test
    void newPlayerHasNineEmptySlots() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        List<String> slots = manager.getAsStringList(uuid);
        assertEquals(9, slots.size());
        for (String slot : slots) assertEquals("none", slot);
    }

    @Test
    void setSlotThenGetSlotRoundTrip() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 3, "blocks.wool");
        assertEquals("blocks.wool", manager.getSlot(uuid, 3));
        assertEquals("none", manager.getSlot(uuid, 4));
    }

    @Test
    void setSlotIgnoresOutOfRangeIndices() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, -1, "blocks.wool");
        manager.setSlot(uuid, 9, "blocks.wool");
        assertEquals("none", manager.getSlot(uuid, -1));
        assertEquals("none", manager.getSlot(uuid, 9));
    }

    @Test
    void getSlotDefaultsToNoneForNullValues() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 0, "melee.stone-sword");
        manager.get(uuid)[0] = null;
        assertEquals("none", manager.getSlot(uuid, 0));
    }

    @Test
    void resetClearsAllSlots() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        for (int i = 0; i < 9; i++) manager.setSlot(uuid, i, "blocks.wool");
        manager.reset(uuid);
        List<String> slots = manager.getAsStringList(uuid);
        for (String slot : slots) assertEquals("none", slot);
    }

    @Test
    void asStringListReflectsAssignedCategorySlots() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 0, "blocks");
        manager.setSlot(uuid, 8, "melee.iron-sword");
        List<String> slots = manager.getAsStringList(uuid);
        assertEquals("blocks", slots.get(0));
        assertEquals("melee.iron-sword", slots.get(8));
        assertTrue(slots.size() == 9);
    }

    @Test
    void sameItemPathCanBeAssignedToMultipleSlots() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 0, "blocks.wool");
        manager.setSlot(uuid, 3, "blocks.wool");
        manager.setSlot(uuid, 7, "blocks.wool");
        assertEquals("blocks.wool", manager.getSlot(uuid, 0));
        assertEquals("blocks.wool", manager.getSlot(uuid, 3));
        assertEquals("blocks.wool", manager.getSlot(uuid, 7));
    }

    @Test
    void differentItemsCanShareCategorySlots() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 0, "melee.stone-sword");
        manager.setSlot(uuid, 1, "melee.iron-sword");
        manager.setSlot(uuid, 2, "melee.diamond-sword");
        assertEquals("melee.stone-sword", manager.getSlot(uuid, 0));
        assertEquals("melee.iron-sword", manager.getSlot(uuid, 1));
        assertEquals("melee.diamond-sword", manager.getSlot(uuid, 2));
    }

    @Test
    void overwritingSlotReplacesPreviousAssignment() {
        HotbarPlayerManager manager = new HotbarPlayerManager();
        UUID uuid = UUID.randomUUID();
        manager.setSlot(uuid, 4, "blocks.wool");
        manager.setSlot(uuid, 4, "utility.fireball");
        assertEquals("utility.fireball", manager.getSlot(uuid, 4));
    }
}
