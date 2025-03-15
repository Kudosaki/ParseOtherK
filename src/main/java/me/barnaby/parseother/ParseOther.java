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

        OfflinePlayer targetPlayer = resolvePlayer(user);
        if (targetPlayer == null || targetPlayer.getName() == null || strings[1].isBlank()) {
            return "0";
        }

        try {
            // **Fix: Resolve nested placeholders multiple times to ensure full expansion**
            String resolvedPlaceholder = resolvePlaceholders(targetPlayer, "%" + strings[1] + "%");

            Bukkit.getLogger().info("[ParseOther] Resolved Placeholder: " + resolvedPlaceholder);

            if (resolvedPlaceholder == null || resolvedPlaceholder.trim().isEmpty() || containsUnresolvedPlaceholders(resolvedPlaceholder)) {
                return "0";
            }

            return ChatColor.translateAlternateColorCodes('&', resolvedPlaceholder);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[ParseOther] Error parsing placeholder: " + e.getMessage());
            return "0";
        }
    }

    private OfflinePlayer resolvePlayer(String user) {
        OfflinePlayer player = null;

        if (USERNAME_PATTERN.matcher(user).matches()) {
            player = Bukkit.getOfflinePlayer(user);
            if (player.hasPlayedBefore() || player.isOnline()) {
                nameCache.put(user.toLowerCase(), player.getName());
            }
        } else if (user.length() == 36) {
            try {
                UUID uuid = UUID.fromString(user);
                player = Bukkit.getOfflinePlayer(uuid);
                if (player.hasPlayedBefore() || player.isOnline()) {
                    uuidCache.put(uuid, player.getName());
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return (player != null && player.getName() != null) ? player : null;
    }

    /**
     * Resolves placeholders recursively to ensure full expansion.
     */
    private String resolvePlaceholders(OfflinePlayer player, String placeholder) {
        String resolved = PlaceholderAPI.setPlaceholders(player, placeholder);

        // Recursively resolve placeholders if still unresolved
        int attempts = 3;
        while (containsUnresolvedPlaceholders(resolved) && attempts-- > 0) {
            resolved = PlaceholderAPI.setPlaceholders(player, resolved);
        }

        return resolved;
    }

    /**
     * Checks if the string contains unresolved PlaceholderAPI placeholders.
     */
    private boolean containsUnresolvedPlaceholders(String input) {
        return input.contains("%") || input.contains("{") || input.contains("}");
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
