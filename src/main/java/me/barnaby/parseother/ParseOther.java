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

        String[] strings = s.split("(?<!\\\\)\\}_", 2);
        strings[0] = strings[0].substring(1);
        strings[0] = strings[0].replaceAll("\\\\}_", "}_");
        strings[1] = strings[1].substring(1, strings[1].length() - 1);
        OfflinePlayer player = null;

        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];
        if (user.contains("%")) {
            return "0"; // Stop processing if the placeholder fails to resolve
        }
        
        try {
            UUID id = UUID.fromString(user);
            player = safeGetOfflinePlayer(id);
            if (player == null) {
                player = safeGetOfflinePlayer(user);
            }
        } catch (IllegalArgumentException e) {
            player = safeGetOfflinePlayer(user);
        }

        if (player == null) {
            return "0";
        }

        String placeholder = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
        if (placeholder.startsWith("%") && placeholder.endsWith("%")) {
            placeholder = strings[1];
        }

        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }

    private OfflinePlayer safeGetOfflinePlayer(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        OfflinePlayer player;
        try {
            UUID uuid = UUID.fromString(identifier);
            player = Bukkit.getOfflinePlayer(uuid);
        } catch (IllegalArgumentException e) {
            player = Bukkit.getOfflinePlayer(identifier);
        }
        if (player == null || player.getName() == null || player.getName().trim().isEmpty()) {
            return null;
        }
        return player;
    }

    private OfflinePlayer safeGetOfflinePlayer(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player == null || player.getName() == null || player.getName().trim().isEmpty()) {
            return null;
        }
        return player;
    }
}
