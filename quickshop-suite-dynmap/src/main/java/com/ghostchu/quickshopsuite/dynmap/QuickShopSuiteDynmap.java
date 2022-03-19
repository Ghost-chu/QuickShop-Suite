package com.ghostchu.quickshopsuite.dynmap;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopDeleteEvent;
import com.ghostchu.quickshop.api.event.ShopPriceChangeEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.GenericMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public final class QuickShopSuiteDynmap extends JavaPlugin implements Listener {
    private DynmapAPI api;
    private MarkerSet quickShopSet;
    private QuickShopAPI quickShopAPI;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("QuickShop Dynmap Addon - Loading...");
        saveDefaultConfig();
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        Plugin quickshop = Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        if (dynmap == null || quickshop == null) {
            getLogger().severe("Plugin won't work because dynmap or quickshop not setup correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!dynmap.isEnabled() || !quickshop.isEnabled()) {
            getLogger().severe("Dynmap or QuickShop not enabled!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.quickShopAPI = (QuickShopAPI) quickshop;
        api = (DynmapAPI) dynmap;
        quickShopSet = api.getMarkerAPI().getMarkerSet("quickshop");
        if (quickShopSet == null) {
            quickShopSet = api.getMarkerAPI().createMarkerSet("quickshop", getConfig().getString("marker-name"), null, false);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                updateMarkers();
            }
        }.runTaskTimer(this, 1, 20 * 120 * 60);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        MarkerAPI markerAPI = api.getMarkerAPI();
        if (markerAPI == null) {
            getLogger().warning("Dynmap marker api not ready, skipping...");
            return;
        }
        quickShopSet.deleteMarkerSet();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopRemoved(ShopDeleteEvent event) {
        Bukkit.getScheduler().runTask(this, this::updateMarkers);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopCreated(ShopCreateEvent event) {
        Bukkit.getScheduler().runTask(this, this::updateMarkers);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopPriceChanged(ShopPriceChangeEvent event) {
        Bukkit.getScheduler().runTask(this, this::updateMarkers);
    }

    private void updateMarkers() {
        if (api.getMarkerAPI() == null) {
            getLogger().warning("Dynmap marker api not ready, skipping...");
            return;
        }
        quickShopSet.getMarkers().forEach(GenericMarker::deleteMarker);
        for (Shop shop : quickShopAPI.getShopManager().getAllShops()) {
            if (shop.isDeleted()) {
                return;
            }
            Marker marker = quickShopSet.createMarker(shop.getRuntimeRandomUniqueId().toString(),
                    Util.getItemStackName(shop.getItem()) + " - " + (shop.isSelling() ? getConfig().getString("lang.selling") : getConfig().getString("lang.buying")) + " - " + shop.getPrice()
                    , shop.getLocation().getWorld().getName()
                    , shop.getLocation().getBlockX()
                    , shop.getLocation().getBlockY()
                    , shop.getLocation().getBlockZ()
                    , api.getMarkerAPI().getMarkerIcon("chest"), false);
            String desc = getConfig().getString("lang.description");
            if (shop.isSelling()) {
                desc = fillArgs(desc,
                        ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(shop.getItem()))),
                        Bukkit.getOfflinePlayer(shop.getOwner()).getName(),
                        getConfig().getString("lang.selling"),
                        fillArgs(getConfig().getString("lang.stockdesc"), String.valueOf(shop.getRemainingStock())),
                        String.valueOf(shop.getPrice()),
                        String.valueOf(shop.getLocation().getBlockX()),
                        String.valueOf(shop.getLocation().getBlockY()),
                        String.valueOf(shop.getLocation().getBlockZ())
                );
            } else {
                desc = fillArgs(desc,
                        ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(shop.getItem()))),
                        Bukkit.getOfflinePlayer(shop.getOwner()).getName(),
                        getConfig().getString("lang.buying"),
                        fillArgs(getConfig().getString("lang.spacedesc"), String.valueOf(shop.getRemainingSpace())),
                        String.valueOf(shop.getPrice()),
                        String.valueOf(shop.getLocation().getBlockX()),
                        String.valueOf(shop.getLocation().getBlockY()),
                        String.valueOf(shop.getLocation().getBlockZ())
                );
            }
            marker.setDescription(desc);
        }
    }

    /**
     * Replace args in raw to args
     *
     * @param raw  text
     * @param args args
     * @return filled text
     */
    public static String fillArgs(String raw, String... args) {
        if (raw == null) {
            return "Invalid message: null";
        }
        if (raw.isEmpty()) {
            return "";
        }
        if (args == null) {
            return raw;
        }
        for (int i = 0; i < args.length; i++) {
            raw = StringUtils.replace(raw, "{" + i + "}", args[i] == null ? "" : args[i]);
        }
        return raw;
    }
}
