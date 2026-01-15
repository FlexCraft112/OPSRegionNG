package me.flexcraft.opsregionng.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.LocalPlayer;
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

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    /* ================= BLOCK BREAK ================= */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), event, event.getBlock().getLocation(), "break");
    }

    /* ================= BLOCK PLACE ================= */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), event, event.getBlock().getLocation(), "place");
    }

    /* ================= WATER / LAVA ================= */
    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        handle(event.getPlayer(), event, event.getBlock().getLocation(), "place");
    }

    /* ================= ENTITIES (boats, stands, carts) ================= */
    @EventHandler
    public void onEntityPlace(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;

        Material type = event.getItem().getType();

        switch (type) {
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

        handle(
            event.getPlayer(),
            event,
            event.getClickedBlock().getLocation(),
            "place"
        );
    }

    /* ================= CORE LOGIC ================= */
    private void handle(Player player, Cancellable event, Location loc, String action) {

        String bypass = plugin.getConfig().getString("bypass-permission");
        if (bypass != null && player.hasPermission(bypass)) return;

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(loc.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(loc));

        if (regions == null || regions.size() == 0) return;

        Set<String> configRegions = plugin.getConfig()
                .getConfigurationSection("regions")
                .getKeys(false);

        // ✅ ПРАВИЛЬНОЕ ПОЛУЧЕНИЕ LocalPlayer
        LocalPlayer lp = WorldGuard.getInstance()
                .getPlatform()
                .getMatcher()
                .wrapPlayer(player);

        boolean checked = false;
        boolean allowed = false;

        for (ProtectedRegion region : regions) {

            String id = region.getId();
            if (!configRegions.contains(id)) continue;

            checked = true;

            // Владельцы и участники — всегда можно
            if (region.isOwner(lp) || region.isMember(lp)) {
                allowed = true;
                break;
            }

            boolean cfgAllowed = plugin.getConfig().getBoolean(
                    "regions." + id + "." + action,
                    false
            );

            if (cfgAllowed) {
                allowed = true;
                break;
            }
        }

        if (checked && !allowed) {
            String msg = plugin.getConfig()
                    .getString("messages." + action + "-blocked", "&cДействие запрещено.")
                    .replace("&", "§");

            player.sendMessage(msg);
            event.setCancelled(true);
        }
    }
}
