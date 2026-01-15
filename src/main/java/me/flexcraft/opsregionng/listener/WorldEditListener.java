package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

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

        // ловим ВСЕ команды WorldEdit
        if (!message.startsWith("//") && !message.startsWith("/we")) {
            return;
        }

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(
                BukkitAdapter.asBlockVector(player.getLocation())
        );

        Set<String> configRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!configRegions.contains(id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + ".worldedit",
                    false
            );

            if (!allowed) {
                String msg = plugin.getConfig()
                        .getString("messages.worldedit-blocked",
                                "&cВы не можете использовать WorldEdit в этом регионе.")
                        .replace("&", "§");

                player.sendMessage(msg);
                event.setCancelled(true);
                return;
            }
        }
    }
}
