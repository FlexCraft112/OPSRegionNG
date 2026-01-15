package me.flexcraft.opsregionng.listener;

import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class RegionProtectListener implements Listener {

    private final OPSRegionNG plugin;

    public RegionProtectListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegionCommand(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();

        if (!msg.startsWith("/rg") && !msg.startsWith("/region")) return;

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        for (String regionId : plugin.getConfig().getConfigurationSection("regions").getKeys(false)) {
            if (msg.contains(" " + regionId + " ")) {

                if (
                        msg.contains("delete") ||
                        msg.contains("remove") ||
                        msg.contains("addmember") ||
                        msg.contains("addowner") ||
                        msg.contains("removemember") ||
                        msg.contains("removeowner")
                ) {
                    player.sendMessage("§cЭтот регион защищён и не может быть изменён.");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
