package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.event.PlayerEditSessionEvent;
import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WorldEditListener implements Listener {

    private final OPSRegionNG plugin;

    public WorldEditListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldEdit(PlayerEditSessionEvent event) {
        Player player = Bukkit.getPlayer(event.getActor().getUniqueId());
        if (player == null) return;

        // bypass permission
        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) {
            return;
        }

        // дальше будет WorldGuard (следующий шаг)
    }
}
