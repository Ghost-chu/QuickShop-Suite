package com.ghostchu.quickshopsuite.list;


import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            Component component = LegacyComponentSerializer.legacySection().deserialize(plugin.getConfig().getString("lang.coord"));
            component = Helper.replaceArgs(component, "{num}", String.valueOf(i + 1));
            component = Helper.replaceArgs(component, "{name}", Util.getItemStackName(shop.getItem()));
            List<String> hover = plugin.getConfig().getStringList("lang.hover");
            List<Component> description = hover.stream().map(str -> LegacyComponentSerializer.legacyAmpersand().deserialize(str)).collect(Collectors.toList());
            Component showComponent = Component.empty();

            for (int j = 0; j < description.size(); j++) {
                Component component1 = description.get(j);
                component1 = Helper.replaceArgs(component1, "{name}", Util.getItemStackName(shop.getItem()));
                component1 = Helper.replaceArgs(component1, "{world}", shop.getLocation().getWorld().getName());
                component1 = Helper.replaceArgs(component1, "{x}", String.valueOf(shop.getLocation().getBlockX()));
                component1 = Helper.replaceArgs(component1, "{y}", String.valueOf(shop.getLocation().getBlockY()));
                component1 = Helper.replaceArgs(component1, "{z}", String.valueOf(shop.getLocation().getBlockZ()));
                component1 = Helper.replaceArgs(component1, "{price}", QuickShop.getInstance().getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency()));
                component1 = Helper.replaceArgs(component1, "{type}", shop.isSelling() ? plugin.getConfig().getString("lang.selling") : plugin.getConfig().getString("lang.buying"));
                showComponent = showComponent.append(component1);
                if (j != description.size() - 1)
                    showComponent = showComponent.append(Component.newline());
            }
            plugin.getAudience().player(commandSender).sendMessage(component.hoverEvent(HoverEvent.showText(showComponent.compact())));
        }
    }
}
