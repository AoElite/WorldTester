package com.aoelite.me.worldtester;

import com.aoelite.me.worldtester.commands.WorldTesterCommand;
import com.aoelite.me.worldtester.manager.WorldManager;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldTester extends JavaPlugin {

    @Getter private static WorldTester instance = null;

    @Override
    public void onEnable() {
        instance = this;
        WorldManager worldManager = new WorldManager();
        // Plugin startup logic
        registerCommand("worldtester", new WorldTesterCommand(worldManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private <T extends CommandExecutor> void registerCommand(String command, T executor) {
        PluginCommand pluginCommand = getCommand(command);
        if (pluginCommand == null) {
            throw new RuntimeException("The command /" + command + " is not registered in the plugin.yml!");
        }
        pluginCommand.setExecutor(executor);
        if (executor instanceof TabCompleter tabCompleter) pluginCommand.setTabCompleter(tabCompleter);
    }

}
