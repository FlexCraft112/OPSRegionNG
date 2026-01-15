package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.bukkit.BukkitAdapter;

import me.flexcraft.opsregionng.OPSRegionNG;

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

        // bypass permission
        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) {
            return;
        }

        String message = event.getMessage().toLowerCase();

        // ловим команды WorldEdit
        if (!message.startsWith("//") && !message.startsWith("/we")) {
            return;
        }

        // получаем RegionManager ЧЕРЕЗ API WorldGuard
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) return;

        ApplicableRegionSet regions = regionManager.getApplicableRegions(
                BukkitAdapter.asBlockVector(player.getLocation())
        );

        List<String> blockedRegions = plugin.getConfig()
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
}
