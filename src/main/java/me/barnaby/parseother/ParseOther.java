package me.barnaby.parseother;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class ParseOther extends PlaceholderExpansion {

    private final Map<String, String> nameCache = new ConcurrentHashMap<>();
    private final Map<UUID, String> uuidCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public ParseOther() {
        cacheCleaner.scheduleAtFixedRate(() -> {
            nameCache.clear();
            uuidCache.clear();
        }, 30, 30, TimeUnit.MINUTES);
    }

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
        if (strings.length < 2 || strings[1].length() < 2) {
            return "0";
        }

        strings[0] = strings[0].substring(1).replaceAll("\\\\}_", "}_");
        strings[1] = strings[1].substring(1, strings[1].length() - 1);

        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];

        // Strip colors and invalid characters
        user = ChatColor.stripColor(user).replaceAll("[^a-zA-Z0-9_]", "");

        if (user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%") || !USERNAME_PATTERN.matcher(user).matches()) {
            return "0";
        }

        OfflinePlayer player = resolvePlayer(user);

        if (player == null || player.getName() == null || strings[1].isBlank() || strings[1].contains("%")) {
            return "0";
        }

        try {
            String placeholderResult = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");

            // Ensure unresolved placeholders or empty results default to "0"
            if (placeholderResult == null || placeholderResult.trim().isEmpty() || placeholderResult.contains("{")) {
                return "0";
            }

            return ChatColor.translateAlternateColorCodes('&', placeholderResult);
        } catch (Exception e) {
            return "0"; // If any error occurs, return "0"
        }
    }

    private OfflinePlayer resolvePlayer(String user) {
        OfflinePlayer player = null;

        if (USERNAME_PATTERN.matcher(user).matches()) {
            player = Bukkit.getOfflinePlayer(user);
            if (!player.hasPlayedBefore() && !player.isOnline()) {
                return null; // Ensure only known players are returned
            }
            nameCache.put(user.toLowerCase(), player.getName());
        } else if (user.length() == 36) {
            try {
                UUID uuid = UUID.fromString(user);
                player = Bukkit.getOfflinePlayer(uuid);
                if (!player.hasPlayedBefore() && !player.isOnline()) {
                    return null;
                }
                uuidCache.put(uuid, player.getName());
            } catch (IllegalArgumentException ignored) {}
        }

        return (player != null && player.getName() != null) ? player : null;
    }

    public void onUnregister() {
        cacheCleaner.shutdown();
        try {
            if (!cacheCleaner.awaitTermination(5, TimeUnit.SECONDS)) {
                cacheCleaner.shutdownNow();
                Bukkit.getLogger().warning("[ParseOther] Forced shutdown of cacheCleaner due to timeout.");
            }
        } catch (InterruptedException ignored) {
            cacheCleaner.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
