package com.aoelite.me.worldtester.manager;

import com.aoelite.me.worldtester.WorldTester;
import com.aoelite.me.worldtester.utils.MathUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WorldManager {

    private final WorldCreator creator;

    public WorldManager() {
        WorldCreator worldCreator = new WorldCreator("test_world");
        worldCreator.keepSpawnLoaded(TriState.FALSE);
        worldCreator.generateStructures(false);
        worldCreator.type(WorldType.FLAT);
        worldCreator.generator("minecraft:air;minecraft:the_void");
        this.creator = worldCreator;
    }

    private final List<Long> worldLoadTimes = new ArrayList<>();
    private final List<Long> worldUnloadTimes = new ArrayList<>();

    private int total = 0;
    private int current = 0;

    private World loadWorld(WorldCreator creator) {
        final String name = creator.name();
        final long time = System.nanoTime();
        World world = creator.createWorld();
        final long finished = System.nanoTime();
        final long msFinished = TimeUnit.NANOSECONDS.toMillis(finished - time);
        worldLoadTimes.add(msFinished);
        log("World " + name + " loaded in " + msFinished + "ms (" + current + "/" + total + ")");
        return world;
    }

    private void unloadWorld(World world, boolean save) {
        final String name = world.getName();
        final long time = System.nanoTime();
        Bukkit.unloadWorld(world, save);
        final long finished = System.nanoTime();
        final long msFinished = TimeUnit.NANOSECONDS.toMillis(finished - time);
        worldUnloadTimes.add(msFinished);
        log("World " + name + " unloaded in " + msFinished + "ms (" + current + "/" + total + ")");
    }


    private BukkitTask task = null;

    private long taskStarted = 0;

    public void start(final int worldsToLoad, int tickSpeed) {
        //
        if (task != null) {
            log("Task is already running.");
            return;
        } else {
            log("Loading a world every " + tickSpeed + " tick(s), " + worldsToLoad + " world(s) total");
        }
        //
        total = worldsToLoad;
        current = 0;
        taskStarted = System.nanoTime();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                current++;
                // load world
                World world = loadWorld(creator);
                // unload world
                unloadWorld(world, false);
                // reduce count
                if (current >= total) {
                    cancel();
                    task = null;
                    log("Task finished in " + MathUtils.convertTime(TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - taskStarted))));
                    showStats();
                }
            }
        }.runTaskTimer(WorldTester.getInstance(), 1, tickSpeed);
    }

    private void showStats() {
        show("World load times", worldLoadTimes);
        show("World unload times", worldUnloadTimes);

        // clear
        worldLoadTimes.clear();
        worldUnloadTimes.clear();
    }

    private void show(String name, List<Long> times) {
        double average = MathUtils.round(MathUtils.calculateAvg(times), 4);
        double sd = MathUtils.round(MathUtils.calculateSD(times), 4);
        //
        var msg = Component.text();
        msg.append(Component.text(name).color(NamedTextColor.WHITE));
        msg.append(Component.text(" | ").color(NamedTextColor.GRAY));
        msg.append(Component.text(" Average: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(average + "ms").color(NamedTextColor.AQUA));
        msg.append(Component.text(" SD: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(sd + "ms").color(NamedTextColor.AQUA));
        log(msg.build());
    }

    public void stop() {
        if (task != null) {
            log("Task stopped.");
            if (!task.isCancelled()) task.cancel();
            task = null;
            showStats();
        } else {
            log("No task is running.");
        }
    }


    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage(Component.text()
                .append(Component.text("WorldLoader").color(NamedTextColor.GREEN))
                .append(Component.text(" > ").color(NamedTextColor.GRAY))
                .append(Component.text(message).color(NamedTextColor.WHITE))
                .build());
    }

    private void log(Component message) {
        Bukkit.getConsoleSender().sendMessage(Component.text()
                .append(Component.text("WorldLoader").color(NamedTextColor.GREEN))
                .append(Component.text(" > ").color(NamedTextColor.GRAY))
                .append(message)
                .build());
    }


}
