package com.aoelite.me.worldtester.commands;

import com.aoelite.me.worldtester.manager.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public class WorldTesterCommand implements CommandExecutor {

    private final WorldManager worldManager;

    public WorldTesterCommand(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    private final String INVALID = ChatColor.GRAY + "/worldtester <start|stop>";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] arguments) {
        //
        if (!(commandSender instanceof ConsoleCommandSender)) {
            commandSender.sendMessage(ChatColor.RED + "Only console is allowed to run this command.");
            return true;
        }
        //
        if (arguments.length == 0) {
            commandSender.sendMessage(INVALID);
            return true;
        }
        String first = arguments[0];
        if (first.equalsIgnoreCase("start")) {
            if (arguments.length == 1) {
                commandSender.sendMessage(ChatColor.GRAY + "/worldtester start <count> [tickspeed]");
                return true;
            }
            int number = getNumber(arguments[1]);
            //
            if (number <= 0) {
                commandSender.sendMessage(ChatColor.RED + "Must specify a number greater than 0");
                return true;
            }
            //
            int tickSpeed = 5;
            if (arguments.length > 2) {
                tickSpeed = getNumber(arguments[2]);
                if (tickSpeed <= 0) {
                    commandSender.sendMessage(ChatColor.RED + "Must specify a number greater than 0");
                    return true;
                }
            }
            //
            worldManager.start(number, tickSpeed);
            return true;
        } else if (first.equalsIgnoreCase("stop")) {
            worldManager.stop();
            return true;
        }
        commandSender.sendMessage(INVALID);
        return true;
    }

    private int getNumber(String string) {
        int number;
        try {
            number = Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return -1;
        }
        return number;
    }


}
