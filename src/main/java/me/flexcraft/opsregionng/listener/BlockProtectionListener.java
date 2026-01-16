package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;
    private final WorldGuardPlugin wg;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
        this.wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    /* ================= BLOCK BREAK ================= */

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        check(event.getPlayer(), event.getBlock(), event, true);
    }

    /* ================= BLOCK PLACE ================= */

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        check(event.getPlayer(), event.getBlock(), event, false);
    }

    /* ================= BUCKETS ================= */

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        check(event.getPlayer(), event.getBlock(), event, false);
    }

    /* ================= BOATS / ARMOR STANDS / ETC ================= */

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Block block = event.getRightClicked().getLocation().getBlock();
        check(event.getPlayer(), block, event, false);
    }

    /* ================= ITEM INTERACT (boats, frames) ================= */

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getClickedBlock() == null) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // Всё, что НЕ блок — запрещаем
        if (!item.getType().isBlock()) {
            check(event.getPlayer(), event.getClickedBlock(), event, false);
        }
    }

    /* ================= PAINTINGS / FRAMES ================= */

    @EventHandler
    public void onHanging(HangingPlaceEvent event) {
        check(event.getPlayer(), event.getBlock(), event, false);
    }

    /* ================= CORE ================= */

    private void check(Player player, Block block, Cancellable event, boolean breaking) {

        if (player.hasPermission(plugin.getConfig().getString("bypass-permission"))) return;

        RegionManager rm = wg.getRegionManager(block.getWorld());
        if (rm == null) return;

        ApplicableRegionSet regions = rm.getApplicableRegions(block.getLocation());
        if (regions.size() == 0) return;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        for (ProtectedRegion region : regions) {
            String id = region.getId();

            if (!cfgRegions.contains(id)) continue;

            boolean allowed = plugin.getConfig().getBoolean(
                    "regions." + id + (breaking ? ".break" : ".place"),
                    false
            );

            if (allowed) return;

            String msg = plugin.getConfig().getString(
                    breaking ? "messages.break-blocked" : "messages.place-blocked",
                    "&cЗапрещено."
            ).replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
            return;
        }
    }
}
