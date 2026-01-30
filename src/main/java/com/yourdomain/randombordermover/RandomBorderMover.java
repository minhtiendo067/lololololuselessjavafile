
package com.yourdomain.randombordermover;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class RandomBorderMover extends JavaPlugin {

    private BukkitTask scheduledTask;
    private final Random random = new Random();
    private boolean enabledScheduled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("rbm").setExecutor(new RBMCommand(this));
        getLogger().info("RandomBorderMover enabled.");
        if (getConfig().getBoolean("autostart", true)) {
            startScheduler();
        }
    }

    @Override
    public void onDisable() {
        stopScheduler();
        getLogger().info("RandomBorderMover disabled.");
    }

    public synchronized void startScheduler() {
        if (enabledScheduled) return;
        enabledScheduled = true;
        scheduleNextMove();
        getLogger().info("RandomBorderMover scheduling started.");
    }

    public synchronized void stopScheduler() {
        enabledScheduled = false;
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
        getLogger().info("RandomBorderMover scheduling stopped.");
    }

    private void scheduleNextMove() {
        if (!enabledScheduled) return;
        FileConfiguration cfg = getConfig();
        long wait = cfg.getLong("timing.wait-time-seconds", 7200L);
        // schedule performMove after wait seconds (converted to ticks)
        scheduledTask = Bukkit.getScheduler().runTaskLater(this, this::performMove, wait * 20L);
        getLogger().info("Next border move scheduled in " + wait + " seconds.");
    }

    public void performMove() {
        if (!enabledScheduled) return;
        FileConfiguration cfg = getConfig();

        int moveDistance = cfg.getInt("border.move-distance", 4500);
        double speed = cfg.getDouble("border.speed-blocks-per-sec", 2.0);
        int radius = cfg.getInt("border.radius", 2250);
        int maxRange = cfg.getInt("border.max-range", 9000);

        int oldX = cfg.getInt("border.current-x", 0);
        int oldZ = cfg.getInt("border.current-z", 0);

        // pick a random angle to get a truly random direction
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);

        int newX = oldX + (int)Math.round(dx * moveDistance);
        int newZ = oldZ + (int)Math.round(dz * moveDistance);

        // clamp within maxRange
        if (newX > maxRange) newX = maxRange;
        if (newX < -maxRange) newX = -maxRange;
        if (newZ > maxRange) newZ = maxRange;
        if (newZ < -maxRange) newZ = -maxRange;

        // compute duration (seconds) and convert to integer
        int duration = (int)Math.max(1, Math.round(moveDistance / speed));

        // dispatch worldborder command (using wb set X Z radius duration)
        String cmd = String.format("wb set %d %d %d %d", newX, newZ, radius, duration);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

        // save new center to config
        cfg.set("border.current-x", newX);
        cfg.set("border.current-z", newZ);
        saveConfig();

        getLogger().info(String.format("Border moving to (%d, %d) over %d seconds (distance %d).", newX, newZ, duration, moveDistance));

        // schedule next move after (move duration + wait time)
        long wait = cfg.getLong("timing.wait-time-seconds", 7200L);
        long delayTicks = (long)duration * 20L + wait * 20L;
        scheduledTask = Bukkit.getScheduler().runTaskLater(this, this::performMove, delayTicks);
    }
}
