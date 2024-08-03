package com.tcoded.playerbountiesplus.hook.team;

import com.tcoded.playerbountiesplus.PlayerBountiesPlus;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class AbstractTeamHook {

    protected PlayerBountiesPlus plugin;
    protected JavaPlugin teamPlugin;

    public AbstractTeamHook(PlayerBountiesPlus plugin, Plugin teamPlugin) {
        this.plugin = plugin;
        this.teamPlugin = (JavaPlugin) teamPlugin;
    }

    public static AbstractTeamHook findTeamHook(PlayerBountiesPlus plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // RoinujNosde - SimpleClans - https://www.spigotmc.org/resources/simpleclans.71242/
        Plugin simpleClansPlugin = pluginManager.getPlugin("SimpleClans");
        if (simpleClansPlugin != null && simpleClansPlugin.isEnabled()) {
            return new SimpleClansHook(plugin, simpleClansPlugin);
        }

        return null;
    }

    public String getPluginName() {
        return this.teamPlugin.getName();
    }

    public String getAuthor() {
        List<String> authors = this.teamPlugin.getDescription().getAuthors();
        if (authors.isEmpty()) return "N/A";
        return authors.get(0);
    }

    public String[] getAuthors() {
        List<String> authors = this.teamPlugin.getDescription().getAuthors();
        if (authors.isEmpty()) return new String[0];
        return authors.toArray(new String[0]);
    }

    public abstract boolean isFriendly(Player player1, Player player2);

}
