package com.ghostchu.quickshopsuite.list;


import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.shade.net.kyori.adventure.text.Component;
import com.ghostchu.quickshop.shade.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ListCommand implements CommandHandler<Player> {
    private QuickShopSuiteList plugin;

    public ListCommand(QuickShopSuiteList plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(Player commandSender, String s, String[] strings) {
        UUID playerToSee;
        if (strings.length < 1) {
            playerToSee = commandSender.getUniqueId();
        } else {
            if (!commandSender.hasPermission("quickshop.list.others")) {
                plugin.getApi().getTextManager().of(commandSender, "no-permission").send();
                return;
            }
            playerToSee = Bukkit.getOfflinePlayer(strings[0]).getUniqueId();
        }
        List<Shop> shops = plugin.getApi().getShopManager().getPlayerAllShops(playerToSee);
        commandSender.sendMessage(plugin.getConfig().getString("lang.prefix").replace("{total}", String.valueOf(shops.size())));
        if (shops.isEmpty()) {
            commandSender.sendMessage(plugin.getConfig().getString("lang.nothing"));
            return;
        }
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            String message = plugin.getConfig().getString("lang.coord").replace("{num}", String.valueOf(i + 1)).replace("{name}", LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(shop.getItem())));
            Component component = LegacyComponentSerializer.legacySection().deserialize(message);
            List<String> lores = new ArrayList<>();
            String title = null;
            List<String> hover = plugin.getConfig().getStringList("lang.hover");

            for (int j = 0; j < hover.size(); j++) {
                if (j == 0) {
                    title = format(shop, hover.get(j));
                    continue;
                }
                lores.add(format(shop, hover.get(j)));
            }
            ItemStack stack = new ItemStack(Material.STONE);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(title);
            meta.setLore(lores);
            stack.setItemMeta(meta);
            plugin.getAudience().player(commandSender).sendMessage(component.hoverEvent(QuickShop.getInstance().getPlatform().getItemStackHoverEvent(stack).asHoverEvent()));
        }
    }

    private String format(Shop shop, String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw.replace("{name}", LegacyComponentSerializer.legacySection().serialize(Util.getItemStackName(shop.getItem())))
                .replace("{world}", shop.getLocation().getWorld().getName())
                .replace("{x}", String.valueOf(shop.getLocation().getBlockX()))
                .replace("{y}", String.valueOf(shop.getLocation().getBlockY()))
                .replace("{z}", String.valueOf(shop.getLocation().getBlockZ()))
                .replace("{price}", QuickShop.getInstance().getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency()))
                .replace("{type}", shop.isSelling() ? plugin.getConfig().getString("lang.selling") : plugin.getConfig().getString("lang.buying")));

    }
}
