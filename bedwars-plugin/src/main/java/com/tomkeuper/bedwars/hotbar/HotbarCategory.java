package com.tomkeuper.bedwars.hotbar;

public enum HotbarCategory {
    NONE,
    BLOCKS,
    MELEE,
    TOOLS,
    RANGED,
    POTIONS,
    UTILITY;

    public static final HotbarCategory[] ASSIGNABLE = {BLOCKS, MELEE, TOOLS, RANGED, POTIONS, UTILITY};

    public static HotbarCategory fromString(String value) {
        if (value == null || value.isEmpty()) return NONE;
        for (HotbarCategory c : values()) {
            if (c.name().equalsIgnoreCase(value)) return c;
        }
        return NONE;
    }

    public String shopCategoryKey() {
        return name().toLowerCase() + "-category";
    }
}
