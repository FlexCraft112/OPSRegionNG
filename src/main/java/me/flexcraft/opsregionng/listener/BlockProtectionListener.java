package me.flexcraft.opsregionng.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;

import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockProtectionListener implements Listener {

    private final OPSRegionNG plugin;
    private final WorldGuardPlugin worldGuard;

    public BlockProtectionListener(OPSRegionNG plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    /* ===============================
       ЛОМАНИЕ БЛОКОВ
       =============================== */
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        check(e.getPlayer(), e.getBlock(), e, true);
    }

    /* ===============================
       УСТАНОВКА БЛОКОВ
       =============================== */
    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        check(e.getPlayer(), e.getBlockPlaced(), e, false);
    }

    /* ===============================
       ВСЕ ОБХОДЫ (лодки, стойки, ведра и т.д.)
       =============================== */
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        if (!e.hasItem() || e.getClickedBlock() == null) return;

        ItemStack item = e.getItem();
        Material type = item.getType();
        String name = type.name();

        if (
                // armor stand
                type == Material.ARMOR_STAND ||

                // ВСЕ лодки / плоты / chest-версии (включая bamboo)
                name.endsWith("_BOAT") ||
                name.endsWith("_CHEST_BOAT") ||
                name.endsWith("_RAFT") ||

                // вагонетки
                name.endsWith("_MINECART") ||

                // рамки и картины
                type == Material.ITEM_FRAME ||
                type == Material.GLOW_ITEM_FRAME ||
                type == Material.PAINTING ||

                // ведра
                type == Material.WATER_BUCKET ||
                type == Material.LAVA_BUCKET
        ) {
            check(e.getPlayer(), e.getClickedBlock(), e, false);
        }
    }

    /* ===============================
       ОСНОВНАЯ ПРОВЕРКА WG
       =============================== */
    private void check(Player player, Block block, Event event, boolean breaking) {

        if (player.hasPermission("opsregion.bypass")) return;
        if (worldGuard == null) return;

        RegionManager rm = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(block.getWorld()));

        if (rm == null) return;

        Location wgLoc = BukkitAdapter.adapt(block.getLocation());
        ApplicableRegionSet regions = rm.getApplicableRegions(wgLoc);

        for (ProtectedRegion region : regions) {

            String id = region.getId().toLowerCase();

            // РАЗРЕШЕННЫЕ регионы (автошахта, лесорубка и т.п.)
            if (id.contains("mine") || id.contains("forest") || id.contains("lumber")) {
                return;
            }

            // СПАВН
            if (id.contains("spawn")) {
                cancel(event, player);
                return;
            }
        }
    }

    private void cancel(Event e, Player p) {
        if (e instanceof BlockBreakEvent) ((BlockBreakEvent) e).setCancelled(true);
        if (e instanceof BlockPlaceEvent) ((BlockPlaceEvent) e).setCancelled(true);
        if (e instanceof PlayerInteractEvent) ((PlayerInteractEvent) e).setCancelled(true);

        p.sendMessage("§cВы не можете взаимодействовать в этом регионе.");
    }
}
