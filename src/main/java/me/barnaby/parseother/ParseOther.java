package me.barnaby.parseother;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ParseOther extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return "cj89898";
    }

    @Override
    public String getIdentifier() {
        return "parseother";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @SuppressWarnings("deprecation")
    @Override
    public String onRequest(OfflinePlayer p, String s) {
        boolean unsafe = false;
        if (s.startsWith("unsafe_")) {
            s = s.substring(7);
            unsafe = true;
        }

        String[] strings = s.split("(?<!\\\\)\\}_,", 2);
        if (strings.length < 2) {
            return "0"; // Ensure valid format
        }

        // Clean up escaped placeholders
        strings[0] = strings[0].replaceFirst("^\\{", "").replace("\\}_,", "}_,");
        strings[1] = strings[1].replaceFirst("^\\{", "").replaceFirst("\\}$", "");

        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];
        if (user.contains("%")) {
            return "0"; // If player placeholder fails, stop immediately
        }

        OfflinePlayer player = getOfflinePlayerSafely(user);
        if (player == null) {
            return "0"; // Player not found, stop processing
        }

        String placeholder = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
        return placeholder.contains("%") ? strings[1] : placeholder;
    }

    /**
     * Safely retrieves an OfflinePlayer using either a UUID or name.
     * Prevents redundant calls and ensures stability.
     */
    private OfflinePlayer getOfflinePlayerSafely(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(identifier));
        } catch (IllegalArgumentException e) {
            try {
                OfflinePlayer player = Bukkit.getOfflinePlayer(identifier);
                return (player != null && player.getName() != null && !player.getName().trim().isEmpty()) ? player : null;
            } catch (Exception ex) {
                return null; // Prevent unexpected crashes
            }
        }
    }
}
