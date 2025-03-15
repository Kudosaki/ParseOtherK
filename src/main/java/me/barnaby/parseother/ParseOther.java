package me.barnaby.parseother;

import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class ParseOther extends PlaceholderExpansion {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

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
        boolean unsafe = s.startsWith("unsafe_");
        if (unsafe) {
            s = s.substring(7);
        }

        String[] strings = s.split("(?<!\\\\)\}_", 2);
        if (strings.length < 2 || strings[1].length() < 2) {
            return "0";
        }

        strings[0] = strings[0].substring(1).replace("\\}_", "}_");
        strings[1] = strings[1].substring(1, strings[1].length() - 1);

        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];

        // Strip colors and ensure valid username
        user = ChatColor.stripColor(user).replaceAll("[^a-zA-Z0-9_]", "");
        if (!USERNAME_PATTERN.matcher(user).matches()) {
            return "0";
        }

        OfflinePlayer player = resolvePlayer(user);
        if (player == null) {
            return "0";
        }

        try {
            String placeholderResult = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
            return (placeholderResult == null || placeholderResult.trim().isEmpty()) ? "0"
                   : ChatColor.translateAlternateColorCodes('&', placeholderResult);
        } catch (Exception e) {
            return "0";
        }
    }

    private OfflinePlayer resolvePlayer(String user) {
        if (USERNAME_PATTERN.matcher(user).matches()) {
            return Bukkit.getOfflinePlayer(user);
        }
        
        if (user.length() == 36) {
            try {
                return Bukkit.getOfflinePlayer(UUID.fromString(user));
            } catch (IllegalArgumentException ignored) {}
        }
        
        return null;
    }
}
