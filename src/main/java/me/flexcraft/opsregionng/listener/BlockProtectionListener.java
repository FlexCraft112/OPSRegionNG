package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, false);
    }

    private void handle(Player player, Cancellable event, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(
                BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint()
        );

        LocalPlayer wgPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        for (ProtectedRegion region : regions) {

            String path = "regions." + region.getId();
            if (!plugin.getConfig().isConfigurationSection(path)) continue;

            if (region.isOwner(wgPlayer) || region.isMember(wgPlayer)) return;

            boolean allowed = plugin.getConfig().getBoolean(
                    path + (breaking ? ".break" : ".place"), false
            );

            if (!allowed) {
                player.sendMessage(
                        plugin.getConfig()
                                .getString(breaking
                                        ? "messages.break-blocked"
                                        : "messages.place-blocked")
                                .replace("&", "§")
                );
                event.setCancelled(true);
                return;
            }
        }
    }
}
