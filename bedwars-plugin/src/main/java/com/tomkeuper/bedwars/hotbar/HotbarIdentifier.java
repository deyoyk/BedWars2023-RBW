package com.tomkeuper.bedwars.hotbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps shop content identifiers (e.g. "default-blocks-category.category-content.wool")
 * to hotbar categories and per-item hotbar paths.
 */
public final class HotbarIdentifier {

    private static final Pattern CONTENT_PATTERN = Pattern.compile("^(.*)\\.category-content\\.(.+)$");

    private HotbarIdentifier() {}

    /**
     * Extract the hotbar category for a shop content identifier.
     * "default-blocks-category.category-content.wool" -> BLOCKS
     */
    public static HotbarCategory categoryOf(String identifier) {
        if (identifier == null || identifier.isEmpty()) return HotbarCategory.NONE;
        Matcher m = CONTENT_PATTERN.matcher(identifier);
        if (!m.find()) return HotbarCategory.NONE;
        String categorySegment = m.group(1);
        int lastDash = categorySegment.lastIndexOf('-');
        if (lastDash < 0) return HotbarCategory.NONE;
        String category = categorySegment.substring(0, lastDash);
        // strip any scope prefix (default-, group-, arena names-)
        int dash = category.indexOf('-');
        if (dash >= 0) category = category.substring(dash + 1);
        return HotbarCategory.fromString(category);
    }

    /**
     * Extract the bare content name for a shop content identifier.
     * "default-blocks-category.category-content.wool" -> "wool"
     */
    public static String contentOf(String identifier) {
        if (identifier == null || identifier.isEmpty()) return null;
        Matcher m = CONTENT_PATTERN.matcher(identifier);
        if (!m.find()) return null;
        return m.group(2);
    }

    /**
     * Build the item path used as hotbar slot value.
     * e.g. BLOCKS + "wool" -> "blocks.wool"
     */
    public static String itemPath(HotbarCategory category, String content) {
        if (category == null || category == HotbarCategory.NONE || content == null || content.isEmpty()) return null;
        return category.name().toLowerCase() + "." + content;
    }

    /**
     * Parse a stored hotbar slot value back into category + content.
     * Returns null when the value is a plain category name or "none".
     */
    public static String[] parseItemPath(String value) {
        if (value == null || value.isEmpty()) return null;
        String[] parts = value.split("\\.", 2);
        if (parts.length != 2) return null;
        HotbarCategory category = HotbarCategory.fromString(parts[0]);
        if (category == HotbarCategory.NONE || parts[1].isEmpty()) return null;
        return parts;
    }
}
