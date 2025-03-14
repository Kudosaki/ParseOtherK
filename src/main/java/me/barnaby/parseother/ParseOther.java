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
    boolean unsafe = s.startsWith("unsafe_");
    if (unsafe) s = s.substring(7);

    String[] parts = s.split("(?<!\\\\)\\}_", 2);
    if (parts.length < 2) return "0";

    String user = parts[0].substring(1).replace("\\}_", "}_");
    String placeholder = parts[1].substring(1, parts[1].length() - 1);
    if (placeholder.isBlank() || placeholder.contains("%")) return "0";

    user = unsafe ? PlaceholderAPI.setPlaceholders(p, "%" + user + "%") : user;
    
    // ✅ Remove color codes
    user = ChatColor.stripColor(user);

    // ✅ Remove all non-Minecraft username characters (optimized loop instead of regex)
    StringBuilder cleanUser = new StringBuilder();
    for (char c : user.toCharArray()) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
            cleanUser.append(c);
        }
    }
    user = cleanUser.toString();

    // Reject empty names after sanitization
    if (user.isBlank()) return "0";

    String lowerUser = user.toLowerCase(); // ✅ Store lowercase username for efficiency
    OfflinePlayer player = null;

    // Check if user is in nameCache
    String cachedName = nameCache.get(lowerUser);
    if (cachedName != null) {
      player = Bukkit.getOfflinePlayer(cachedName);
    } else {
      try {
        UUID id = UUID.fromString(user);
        String cachedUUIDName = uuidCache.get(id);
        if (cachedUUIDName != null) {
          player = Bukkit.getOfflinePlayer(cachedUUIDName);
        } else {
          player = Bukkit.getOfflinePlayer(id);
          if (player.getName() != null) {
            uuidCache.put(id, player.getName());
          }
        }
      } catch (IllegalArgumentException e) {
        player = Bukkit.getOfflinePlayer(user);
        if (player.getName() != null) {
          nameCache.put(lowerUser, player.getName());
        }
      }
    }

    if (player == null || player.getName() == null) return "0";

    // Process placeholder
    String result = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
    if (result.startsWith("%") && result.endsWith("%")) result = placeholder;

    return ChatColor.translateAlternateColorCodes('&', result);
  }
}
