package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class WorldEditListener implements Listener {

    private final OPSRegionNG plugin;

    public WorldEditListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // bypass
        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) {
            return;
        }

        String message = event.getMessage().toLowerCase();

        // ловим все команды WorldEdit
        if (!message.startsWith("/we") && !message.startsWith("//")) {
            return;
        }

        WorldGuardPlugin wg = getWorldGuard();
        if (wg == null) return;

        RegionManager manager = wg.getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(
                BukkitAdapter.asBlockVector(player.getLocation())
        );

        List<String> blockedRegions = plugin
                .getConfig()
                .getStringList("protected-regions");

        for (ProtectedRegion region : regions) {
            if (blockedRegions.contains(region.getId())) {

                String msg = plugin.getConfig()
                        .getString("messages.blocked", "&cWorldEdit запрещён здесь.")
                        .replace("&", "§");

                player.sendMessage(msg);
                event.setCancelled(true);
                return;
            }
        }
    }

    private WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) Bukkit.getPluginManager()
                .getPlugin("WorldGuard");
    }
}
