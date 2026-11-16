package com.tomkeuper.bedwars.hotbar;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HotbarIdentifierTest {

    @Test
    void categoryOfDefaultScopedIdentifier() {
        assertEquals(HotbarCategory.BLOCKS,
                HotbarIdentifier.categoryOf("default-blocks-category.category-content.wool"));
    }

    @Test
    void categoryOfGroupScopedIdentifier() {
        assertEquals(HotbarCategory.MELEE,
                HotbarIdentifier.categoryOf("ranked-melee-category.category-content.stone-sword"));
    }

    @Test
    void categoryOfArenaScopedIdentifier() {
        assertEquals(HotbarCategory.RANGED,
                HotbarIdentifier.categoryOf("Arena1-ranged-category.category-content.bow1"));
    }

    @Test
    void categoryOfAllCategories() {
        assertEquals(HotbarCategory.TOOLS, HotbarIdentifier.categoryOf("default-tools-category.category-content.shears"));
        assertEquals(HotbarCategory.POTIONS, HotbarIdentifier.categoryOf("default-potions-category.category-content.speed-potion"));
        assertEquals(HotbarCategory.UTILITY, HotbarIdentifier.categoryOf("default-utility-category.category-content.tnt"));
        assertEquals(HotbarCategory.MELEE, HotbarIdentifier.categoryOf("default-melee-category.category-content.iron-sword"));
    }

    @Test
    void categoryOfInvalidIdentifiers() {
        assertEquals(HotbarCategory.NONE, HotbarIdentifier.categoryOf(null));
        assertEquals(HotbarCategory.NONE, HotbarIdentifier.categoryOf(""));
        assertEquals(HotbarCategory.NONE, HotbarIdentifier.categoryOf("not-a-shop-identifier"));
        assertEquals(HotbarCategory.NONE, HotbarIdentifier.categoryOf("specials-category.category-content.silverfish"));
    }

    @Test
    void contentOfExtractsItemName() {
        assertEquals("wool", HotbarIdentifier.contentOf("default-blocks-category.category-content.wool"));
        assertEquals("speed-potion", HotbarIdentifier.contentOf("default-potions-category.category-content.speed-potion"));
        assertNull(HotbarIdentifier.contentOf(null));
        assertNull(HotbarIdentifier.contentOf("invalid"));
    }

    @Test
    void itemPathJoinsCategoryAndContent() {
        assertEquals("blocks.wool", HotbarIdentifier.itemPath(HotbarCategory.BLOCKS, "wool"));
        assertNull(HotbarIdentifier.itemPath(HotbarCategory.NONE, "wool"));
        assertNull(HotbarIdentifier.itemPath(HotbarCategory.BLOCKS, null));
        assertNull(HotbarIdentifier.itemPath(HotbarCategory.BLOCKS, ""));
    }

    @Test
    void parseItemPathRoundTrip() {
        String[] parsed = HotbarIdentifier.parseItemPath("blocks.wool");
        assertEquals("blocks", parsed[0]);
        assertEquals("wool", parsed[1]);
        assertNull(HotbarIdentifier.parseItemPath("blocks"));
        assertNull(HotbarIdentifier.parseItemPath("none"));
        assertNull(HotbarIdentifier.parseItemPath(null));
        assertNull(HotbarIdentifier.parseItemPath("blocks."));
    }

    @Test
    void fromStringIsCaseInsensitive() {
        assertEquals(HotbarCategory.MELEE, HotbarCategory.fromString("melee"));
        assertEquals(HotbarCategory.MELEE, HotbarCategory.fromString("MELEE"));
        assertEquals(HotbarCategory.NONE, HotbarCategory.fromString("unknown"));
        assertEquals(HotbarCategory.NONE, HotbarCategory.fromString(null));
    }

    @Test
    void shopCategoryKeyMatchesShopFiles() {
        assertEquals("blocks-category", HotbarCategory.BLOCKS.shopCategoryKey());
        assertEquals("utility-category", HotbarCategory.UTILITY.shopCategoryKey());
    }

    @Test
    void assignableCategoriesCoverShopDefaults() {
        assertEquals(6, HotbarCategory.ASSIGNABLE.length);
        assertTrue(HotbarCategory.ASSIGNABLE[0] == HotbarCategory.BLOCKS);
    }
}
