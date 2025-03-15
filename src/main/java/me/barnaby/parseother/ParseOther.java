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
        if (!cacheCleaner.isShutdown() && !cacheCleaner.isTerminated()) {
            cacheCleaner.scheduleAtFixedRate(() -> {
                nameCache.clear();
                uuidCache.clear();
            }, 30, 30, TimeUnit.MINUTES);
        }
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
        Bukkit.getLogger().info("[ParseOther] Received request: Player=" + (p != null ? p.getName() : "null") + ", Placeholder=" + s);

        boolean unsafe = false;
        if (s.startsWith("unsafe_")) {
            s = s.substring(7);
            unsafe = true;
        }

        String[] strings = s.split("(?<!\\\\)\}_", 2);
        if (strings.length < 2) {
            Bukkit.getLogger().info("[ParseOther] Invalid format, returning 0.");
            return "0";
        }

        strings[0] = strings[0].substring(1).replaceAll("\\\\}_", "}_");
        if (strings[1].isEmpty() || strings[1].length() < 2) {
            Bukkit.getLogger().info("[ParseOther] Invalid placeholder content, returning 0.");
            return "0";
        }
        strings[1] = strings[1].substring(1, strings[1].length() - 1);

        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];
        Bukkit.getLogger().info("[ParseOther] Parsed user: " + user);

        if (user == null || user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%") || !USERNAME_PATTERN.matcher(user).matches()) {
            Bukkit.getLogger().info("[ParseOther] Invalid username, returning 0.");
            return "0";
        }

        OfflinePlayer player = resolvePlayer(user);

        if (player == null || player.getName() == null || strings[1] == null || strings[1].isBlank() || strings[1].contains("%")) {
            Bukkit.getLogger().info("[ParseOther] Could not resolve player or invalid placeholder, returning 0.");
            return "0";
        }

        try {
            String placeholder = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
            Bukkit.getLogger().info("[ParseOther] Fetched placeholder result: " + placeholder);
            return ChatColor.translateAlternateColorCodes('&', (placeholder == null || placeholder.trim().isEmpty() || placeholder.contains("%")) ? "0" : placeholder);
        } catch (Exception e) {
            Bukkit.getLogger().severe("[ParseOther] Error processing placeholder: " + e.getMessage());
            return "0";
        }
    }

    private OfflinePlayer resolvePlayer(String user) {
        Bukkit.getLogger().info("[ParseOther] Resolving player: " + user);
        OfflinePlayer player = null;

        if (USERNAME_PATTERN.matcher(user).matches()) {
            String playerName = nameCache.get(user.toLowerCase());
            if (playerName != null) {
                player = Bukkit.getOfflinePlayer(playerName);
            } else {
                player = Bukkit.getOfflinePlayer(user);
            }
        } else if (user.length() == 36) {
            try {
                UUID uuid = UUID.fromString(user);
                player = Bukkit.getOfflinePlayer(uuid);
            } catch (IllegalArgumentException ignored) {}
        }

        if (player == null || !player.isOnline() || player.getName() == null) {
            Bukkit.getLogger().info("[ParseOther] Player not found or offline, returning null.");
            return null;
        }

        Bukkit.getLogger().info("[ParseOther] Successfully resolved player: " + player.getName());
        return player;
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
