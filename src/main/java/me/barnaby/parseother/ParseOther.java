package me.barnaby.parseother;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class ParseOther extends PlaceholderExpansion {
  
  private final Map<String, String> nameCache = new ConcurrentHashMap<>();
  private final Map<UUID, String> uuidCache = new ConcurrentHashMap<>();

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

    strings[0] = strings[0].substring(1);
    strings[0] = strings[0].replaceAll("\\\\}_", "}_");
    strings[1] = strings[1].substring(1, strings[1].length() - 1);

    OfflinePlayer player = null;
    String user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + strings[0] + "%") : strings[0];

    // Validate input username
    if (user == null || user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%") || !user.matches("^[a-zA-Z0-9_]{3,16}$")) {
      return "0";
    }

    // Check if user is already cached
    if (nameCache.containsKey(user.toLowerCase())) {
      player = Bukkit.getOfflinePlayer(nameCache.get(user.toLowerCase()));
    } else {
      // Try resolving UUID first
      try {
        UUID id = UUID.fromString(user);
        if (uuidCache.containsKey(id)) {
          player = Bukkit.getOfflinePlayer(uuidCache.get(id));
        } else {
          player = Bukkit.getOfflinePlayer(id);
          if (player.getName() != null) {
            uuidCache.put(id, player.getName());
          }
        }
      } catch (IllegalArgumentException e) {
        player = Bukkit.getOfflinePlayer(user);
        if (player.getName() != null) {
          nameCache.put(user.toLowerCase(), player.getName()); // Cache real username
        }
      }
    }

    if (player == null || player.getName() == null) {
      return "0";
    }

    if (strings[1] == null || strings[1].isBlank() || strings[1].contains("%")) {
      return "0";
    }

    String placeholder = PlaceholderAPI.setPlaceholders(player, "%" + strings[1] + "%");
    if (placeholder.startsWith("%") && placeholder.endsWith("%")) {
        placeholder = strings[1];
    }

    return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, placeholder));
  }
}
