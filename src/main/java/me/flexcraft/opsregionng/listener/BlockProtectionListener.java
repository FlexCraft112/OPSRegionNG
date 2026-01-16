package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.Cancellable;

import java.util.Comparator;
import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, event.getBlock(), true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, event.getBlock(), false);
    }

    private void handle(Player player, Cancellable event, Block block, boolean breaking) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(block.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));

        if (regions == null || regions.size() == 0) return;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        // берём регион с НАИБОЛЬШИМ priority
        ProtectedRegion region = regions.getRegions().stream()
                .filter(r -> cfgRegions.contains(r.getId()))
                .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                .orElse(null);

        if (region == null) return;

        boolean allowed = plugin.getConfig().getBoolean(
                "regions." + region.getId() + (breaking ? ".break" : ".place"),
                false
        );

        if (allowed) return;

        String msgKey = breaking
                ? "messages.break-blocked"
                : "messages.place-blocked";

        player.sendMessage(plugin.getConfig()
                .getString(msgKey, "&cДействие запрещено.")
                .replace("&", "§"));

        event.setCancelled(true);
    }
}
