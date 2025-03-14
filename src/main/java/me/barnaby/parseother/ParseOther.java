package me.barnaby.parseother;

import java.util.UUID;
import java.util.logging.Level;
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
    Bukkit.getLogger().log(Level.INFO, "[ParseOther] Received placeholder request: " + s);

    boolean unsafe = false;
    if (s.startsWith("unsafe_")) {
      s = s.substring(7);
      unsafe = true;
    }

    String[] strings = s.split("(?<!\\\\)\\}_", 2);
    if (strings.length < 2) {
      Bukkit.getLogger().log(Level.WARNING, "[ParseOther] Invalid placeholder format: " + s);
      return "0";
    }

    strings[0] = strings[0].substring(1);
    strings[0] = strings[0].replaceAll("\\\\}_", "}_");
    strings[1] = strings[1].substring(1, strings[1].length() - 1);

    OfflinePlayer player;
    if (unsafe) {
      String user = PlaceholderAPI.setPlaceholders(p, ("%" + strings[0] + "%"));
      
      if (user == null || user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%")) {
        Bukkit.getLogger().log(Level.WARNING, "[ParseOther] Invalid user placeholder: " + strings[0]);
        return "0";
      }
      
      try {
        UUID id = UUID.fromString(user);
        player = Bukkit.getOfflinePlayer(id);
        if (player.getName() == null)
          player = Bukkit.getOfflinePlayer(user);
      } catch (IllegalArgumentException e) {
        player = Bukkit.getOfflinePlayer(user);
      }
    } else {
      String user = strings[0];
      
      if (user == null || user.isBlank() || user.equalsIgnoreCase("none") || user.contains("%")) {
        Bukkit.getLogger().log(Level.WARNING, "[ParseOther] Invalid user input: " + user);
        return "0";
      }
      
      try {
        UUID id = UUID.fromString(user);
        player = Bukkit.getOfflinePlayer(id);
        if (player.getName() == null)
          player = Bukkit.getOfflinePlayer(user);
      } catch (IllegalArgumentException e) {
        player = Bukkit.getOfflinePlayer(user);
      }
    }

    if (strings[1] == null || strings[1].isBlank() || strings[1].contains("%")) {
      Bukkit.getLogger().log(Level.WARNING, "[ParseOther] Invalid placeholder target: " + strings[1]);
      return "0";
    }

    String placeholder = PlaceholderAPI.setPlaceholders(player, ("%" + strings[1] + "%"));
    if (placeholder.startsWith("%") && placeholder.endsWith("%")) {
        placeholder = strings[1];
    }

    Bukkit.getLogger().log(Level.INFO, "[ParseOther] Resolved placeholder: " + strings[1] + " -> " + placeholder);
    return PlaceholderAPI.setPlaceholders(player, placeholder);
  }
}
