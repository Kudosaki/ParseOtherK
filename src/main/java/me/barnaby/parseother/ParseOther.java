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

    @Override
    public String onRequest(OfflinePlayer p, String s) {
        boolean unsafe = s.startsWith("unsafe_");
        if (unsafe) s = s.substring(7);

        String[] parts = s.split("(?<!\\\\)\\}_", 2);
        if (parts.length < 2) return "0"; // Ensure valid format

        String userPlaceholder = parts[0].substring(1).replace("\\}_", "}_");
        String placeholderKey = parts[1].substring(1, parts[1].length() - 1);

        String resolvedUser = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + userPlaceholder + "%") : userPlaceholder;

        // Prevent parsing if resolvedUser is null, empty, or "none"
        if (resolvedUser == null || resolvedUser.isBlank() || resolvedUser.equalsIgnoreCase("none") || resolvedUser.contains("%")) {
            return "0";
        }

        OfflinePlayer player = getOfflinePlayer(resolvedUser);
        if (player == null) return "0";

        String result = PlaceholderAPI.setPlaceholders(player, "%" + placeholderKey + "%");
        return (result.startsWith("%") && result.endsWith("%")) ? placeholderKey : result;
    }

    private OfflinePlayer getOfflinePlayer(String identifier) {
        if (identifier == null || identifier.isBlank()) return null;

        try {
            UUID uuid = UUID.fromString(identifier);
            return getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            return getOfflinePlayerByName(identifier);
        }
    }

    private OfflinePlayer getOfflinePlayer(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return (player != null && player.getName() != null && !player.getName().isBlank()) ? player : null;
    }

    private OfflinePlayer getOfflinePlayerByName(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        return (player != null && player.getName() != null && !player.getName().isBlank()) ? player : null;
    }
}
