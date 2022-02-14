package com.ghostchu.quickshopsuite.shopitemonly;


import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class QuickShopSuiteShopItemOnly extends JavaPlugin implements Listener {
    private String message;
    private QuickShopAPI quickShopAPI;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        parseColours(getConfig());
        quickShopAPI= QuickShop.getInstance();
        this.message = getConfig().getString("messages.item-dropped");

        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll((JavaPlugin)this);
    }

    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void invClose(InventoryCloseEvent event){
        //noinspection ConstantConditions
        if(event.getInventory() == null){ //Stupid CMIGUI plugin
            return;
        }
        if(event.getInventory().getLocation() == null){
            return;
        }
        Shop shop = quickShopAPI.getShopManager().getShopIncludeAttached(Objects.requireNonNull(event.getInventory().getLocation()));
        if(shop == null){
            return;
        }
        List<ItemStack> pendingForRemoval = new ArrayList<>();
        for(ItemStack stack : event.getInventory().getStorageContents()){
            if(QuickShop.getInstance().getItemMatcher().matches(shop.getItem(),stack)){
                continue;
            }
            if(stack == null || stack.getType() == Material.AIR){
                continue;
            }
            pendingForRemoval.add(stack);
        }
        if(!pendingForRemoval.isEmpty()) {
            Objects.requireNonNull(event.getPlayer().getWorld());
            for (ItemStack item : pendingForRemoval) {
                event.getInventory().remove(item);
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), item);
            }
            event.getPlayer().sendMessage(this.message);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
    public void invMove(InventoryMoveItemEvent event){
        //noinspection ConstantConditions
        if(event.getDestination() == null){ //Stupid CMIGUI plugin
            return;
        }
        if(event.getDestination().getLocation() == null){
            return;
        }
        Shop shop = quickShopAPI.getShopManager().getShopIncludeAttached(Objects.requireNonNull(event.getDestination().getLocation()));
        if(shop == null){
            return;
        }
        if(QuickShop.getInstance().getItemMatcher().matches(shop.getItem(),event.getItem())){
            return;
        }
        event.setCancelled(true);
    }

    public static void parseColours(FileConfiguration config) {
        Set<String> keys = config.getKeys(true);
        for (String key : keys) {
            String filtered = config.getString(key);
            if (filtered == null) {
                continue;
            }
            if (filtered.startsWith("MemorySection")) {
                continue;
            }
            filtered = parseColours(filtered);
            config.set(key, filtered);
        }
    }

    public static String parseColours(String text) {
        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

}
