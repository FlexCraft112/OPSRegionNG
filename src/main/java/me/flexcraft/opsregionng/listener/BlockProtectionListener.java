package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.flexcraft.opsregionng.OPSRegionNG;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

import java.util.Set;
import java.util.UUID;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ========= BLOCK BREAK ========= */
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        handle(e.getPlayer(), e, e.getBlock().getLocation(), "break");
    }

    /* ========= BLOCK PLACE ========= */
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        handle(e.getPlayer(), e, e.getBlock().getLocation(), "place");
    }

    /* ========= WATER / LAVA ========= */
    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent e) {
        handle(e.getPlayer(), e, e.getBlock().getLocation(), "place");
    }

    /* ========= ENTITIES ========= */
    @EventHandler
    public void onEntityPlace(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;

        Material m = e.getItem().getType();

        switch (m) {
            case ARMOR_STAND:
            case ITEM_FRAME:
            case GLOW_ITEM_FRAME:
            case END_CRYSTAL:

            case MINECART:
            case CHEST_MINECART:
            case HOPPER_MINECART:
            case TNT_MINECART:
            case FURNACE_MINECART:
            case COMMAND_BLOCK_MINECART:

            case OAK_BOAT:
            case SPRUCE_BOAT:
            case BIRCH_BOAT:
            case JUNGLE_BOAT:
            case ACACIA_BOAT:
            case DARK_OAK_BOAT:
            case MANGROVE_BOAT:
            case BAMBOO_RAFT:
                break;

            default:
                return;
        }

        handle(e.getPlayer(), e, e.getClickedBlock().getLocation(), "place");
    }

    /* ========= CORE ========= */
    private void handle(Player p, Cancellable e, Location loc, String action) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && p.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        if (regions == null || regions.size() == 0) return;

        Set<String> cfgRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        UUID uuid = p.getUniqueId();

        boolean checked = false;
        boolean allowed = false;

        for (ProtectedRegion r : regions) {

            String id = r.getId();
            if (!cfgRegions.contains(id)) continue;

            checked = true;

            // владельцы и участники
            if (r.getOwners().contains(uuid) || r.getMembers().contains(uuid)) {
                allowed = true;
                break;
            }

            boolean cfgAllow = plugin.getConfig().getBoolean(
                    "regions." + id + "." + action,
                    false
            );

            if (cfgAllow) {
                allowed = true;
                break;
            }
        }

        if (checked && !allowed) {
            String msg = plugin.getConfig()
                    .getString("messages." + action + "-blocked", "&cЗапрещено.")
                    .replace("&", "§");

            p.sendMessage(msg);
            e.setCancelled(true);
        }
    }
}
