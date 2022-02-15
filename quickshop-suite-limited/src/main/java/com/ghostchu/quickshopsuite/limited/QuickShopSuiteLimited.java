package com.ghostchu.quickshopsuite.limited;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshop.api.event.CalendarEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickShopSuiteLimited extends JavaPlugin implements Listener {

    public static QuickShopSuiteLimited instance;
    private CommandContainer container;
    private QuickShopAPI api;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        this.api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        this.container = CommandContainer.builder()
                .prefix("limit")
                .permission("quickshop.limited")
                .description((locale)->LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("command-description")))
                .executor(new ShopLimitedCommand(api))
                .build();
        QuickShop.getInstance().getCommandManager().registerCmd(container);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        QuickShop.getInstance().getCommandManager().unregisterCmd(container);
    }

    @EventHandler(ignoreCancelled = true)
    public void shopPurchase(ShopPurchaseEvent event) {
        Shop shop = event.getShop();
        ConfigurationSection storage = shop.getExtra(this);
        if (storage.getInt("limit") < 1) {
            return;
        }
        int limit = storage.getInt("limit");
        int playerUsedLimit = storage.getInt("data." + event.getPlayer().getUniqueId(), 0);
        if (playerUsedLimit + event.getAmount() > limit) {
            event.getPlayer().sendMessage(ChatColor.RED + MsgUtil.fillArgs(getConfig().getString("reach-the-limit"), String.valueOf(limit - playerUsedLimit), String.valueOf(event.getAmount())));
            event.setCancelled(true,"Trade limit reached");
            return;
        }
        playerUsedLimit += event.getAmount();
        storage.set("data." + event.getPlayer().getUniqueId(), playerUsedLimit);
        shop.setExtra(QuickShopSuiteLimited.instance,storage);
        event.getPlayer().sendTitle(ChatColor.GREEN + getConfig().getString("message.title"),
                ChatColor.AQUA + MsgUtil.fillArgs(getConfig().getString("message.subtitle"),String.valueOf(limit - playerUsedLimit)));
    }

    @EventHandler(ignoreCancelled = true)
    public void scheduleEvent(CalendarEvent event) {
        api.getShopManager().getAllShops().forEach(shop -> {
            ConfigurationSection manager = shop.getExtra(this);
            int limit = manager.getInt("limit");
            if (limit < 1) {
                return;
            }
            if (StringUtils.isEmpty(manager.getString("period"))) {
                return;
            }
            try {
                if (event.getCalendarTriggerType().ordinal() >= CalendarEvent.CalendarTriggerType.valueOf(manager.getString("period")).ordinal()) {
                    manager.set("data", null);
                    shop.setExtra(QuickShopSuiteLimited.instance,manager);
                    Util.debugLog("Limit data has been reset. Shop -> " + shop);
                }
            }catch (IllegalArgumentException ignored){
                Util.debugLog("Limit data failed to reset. Shop -> " + shop+" type "+manager.getString("period")+" not exists.");
                manager.set("period", null);
                shop.setExtra(QuickShopSuiteLimited.instance,manager);
            }
        });
    }
}
