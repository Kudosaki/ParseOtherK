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
        }, 1, 1, TimeUnit.HOURS);
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
        if (strings.length < 2) {
            return "0";
        }

        strings[0] = strings[0].substring(1).replaceAll("\\\\}_", "}_");
        if (strings[1].isEmpty() || strings[1].length() < 2) {
            return "0";
        }
        strings[1] = strings[1].substring(1, strings[1].length() - 1);

        OfflinePlayer player = null;
        String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];

        if (user == null || user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%") || !USERNAME_PATTERN.matcher(user).matches()) {
            return "0";
        }

        // Check cache first
        if (nameCache.containsKey(user.toLowerCase())) {
            player = Bukkit.getOfflinePlayer(nameCache.get(user.toLowerCase()));
        } else {
            try {
                UUID id = UUID.fromString(user);
                player = Bukkit.getOfflinePlayer(uuidCache.computeIfAbsent(id, key -> {
                    OfflinePlayer resolved = Bukkit.getOfflinePlayer(key);
                    return resolved.getName() != null ? resolved.getName() : null;
                }));
            } catch (IllegalArgumentException uuidException) { 
                player = Bukkit.getOfflinePlayer(user);
                if (player.getName() != null) {
                    nameCache.put(user.toLowerCase(), player.getName());
                }
            }
        }

        if (player == null || player.getName() == null || strings[1] == null || strings[1].isBlank() || strings[1].contains("%")) {
            return "0";
        }

        String placeholder = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
        return ChatColor.translateAlternateColorCodes('&', (placeholder == null || placeholder.trim().isEmpty() || placeholder.contains("%")) ? "0" : placeholder);
    }

    public void onUnregister() {
        cacheCleaner.shutdown();
        try {
            if (!cacheCleaner.awaitTermination(5, TimeUnit.SECONDS)) {
                cacheCleaner.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            cacheCleaner.shutdownNow();
        }
    }
}
