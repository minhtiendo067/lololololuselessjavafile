
package com.yourdomain.randombordermover;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RBMCommand implements CommandExecutor {

    private final RandomBorderMover plugin;

    public RBMCommand(RandomBorderMover plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("&cUsage: /rbm <start|stop|force|status|set>"); 
            return true;
        }

        String a = args[0].toLowerCase();
        switch (a) {
            case "start" -> {
                plugin.startScheduler();
                sender.sendMessage("[RBM] Scheduler started.");
            }
            case "stop" -> {
                plugin.stopScheduler();
                sender.sendMessage("[RBM] Scheduler stopped.");
            }
            case "force" -> {
                plugin.performMove();
                sender.sendMessage("[RBM] Forced a border move."); 
            }
            case "status" -> {
                sender.sendMessage("[RBM] Status: scheduler " + (plugin != null ? "running" : "stopped")); 
            }
            case "set" -> {
                if (args.length >= 3) {
                    try {
                        int x = Integer.parseInt(args[1]);
                        int z = Integer.parseInt(args[2]);
                        plugin.getConfig().set("border.current-x", x);
                        plugin.getConfig().set("border.current-z", z);
                        plugin.saveConfig();
                        sender.sendMessage("[RBM] Center set to ("+x+", "+z+")");
                    } catch (NumberFormatException ex) {
                        sender.sendMessage("[RBM] Invalid numbers."); 
                    }
                } else {
                    sender.sendMessage("[RBM] Usage: /rbm set <x> <z>"); 
                }
            }
            default -> sender.sendMessage("[RBM] Unknown command. Use start|stop|force|status|set");
        }
        return true;
    }
}
