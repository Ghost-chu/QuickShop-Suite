package com.ghostchu.quickshopsuite.list;

import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.command.CommandContainer;
import com.ghostchu.quickshopsuite.common.Util;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickShopSuiteList extends JavaPlugin {
    private QuickShopAPI api;
    private BukkitAudiences audience;

    @Override
    public void onEnable() {
        // Plugin startup logic
        audience = BukkitAudiences.create(this);
        saveDefaultConfig();
        reloadConfig();
        api = (QuickShopAPI) Bukkit.getPluginManager().getPlugin("QuickShop-Hikari");
        Util.parseColours(getConfig());
        getLogger().info("QuickShop List Addon loading...");
        api.getCommandManager().registerCmd(CommandContainer.builder().executor(new ListCommand(this))
                .permission("quickshop.list")
                .prefix("list")
                .description((locale)->LegacyComponentSerializer.legacySection().deserialize(getConfig().getString("lang.desc")))
                .build());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public QuickShopAPI getApi() {
        return api;
    }

    public BukkitAudiences getAudience() {
        return audience;
    }
}
