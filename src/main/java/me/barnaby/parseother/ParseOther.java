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
    
    OfflinePlayer player;
    if (unsafe) {
        String user = PlaceholderAPI.setPlaceholders(p, ("%" + strings[0] + "%"));
        if (user.contains("%")) {
            player = getOfflinePlayerByIdentifier(strings[0]);
        } else {
            player = getOfflinePlayerByIdentifier(user);
        }
    } else {
        player = getOfflinePlayerByIdentifier(strings[0]);
    }

    // If the player is offline, return "0"
    if (!player.isOnline()) {
        return "0";
    }

    String placeholder = PlaceholderAPI.setPlaceholders(player, ("%" + strings[1] + "%"));
    if (placeholder.startsWith("%") && placeholder.endsWith("%")) {
        placeholder = strings[1];
    }
    
    return PlaceholderAPI.setPlaceholders(player, placeholder);
}

private OfflinePlayer getOfflinePlayerByIdentifier(String identifier) {
    try {
        UUID id = UUID.fromString(identifier);
        return Bukkit.getOfflinePlayer(id);
    } catch (IllegalArgumentException e) {
        return Bukkit.getOfflinePlayer(identifier);
    }
}
