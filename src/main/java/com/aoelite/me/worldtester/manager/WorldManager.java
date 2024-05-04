package com.aoelite.me.worldtester.manager;

import com.aoelite.me.worldtester.WorldTester;
import com.aoelite.me.worldtester.utils.MathUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
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

    private final List<Long> worldLoadTimes = new ArrayList<>(); // world loading times in nanoseconds
    private final List<Long> worldUnloadTimes = new ArrayList<>(); // world unloading times in nanoseconds

    private int total = 0; // total worlds
    private int current = 0; // current world

    private World loadWorld(WorldCreator creator, boolean initial) {
        final String name = creator.name();
        final long time = System.nanoTime();
        World world = creator.createWorld();
        final long finished = System.nanoTime();
        final long msFinished = TimeUnit.NANOSECONDS.toMillis(finished - time);
        if (!initial) {
            worldLoadTimes.add(msFinished);
            log("World " + name + " loaded in " + msFinished + "ms (" + current + "/" + total + ")");
        }
        return world;
    }

    private void unloadWorld(World world, boolean save, boolean initial) {
        final String name = world.getName();
        final long time = System.nanoTime();
        Bukkit.unloadWorld(world, save);
        final long finished = System.nanoTime();
        final long msFinished = TimeUnit.NANOSECONDS.toMillis(finished - time);
        if (!initial) {
            worldUnloadTimes.add(msFinished);
            log("World " + name + " unloaded in " + msFinished + "ms (" + current + "/" + total + ")");
        }
    }


    private BukkitTask task = null; // task running the test
    private long taskStarted = 0; // time started
    private boolean initialized = false; // if the first world has been initialized or not

    public void start(final int worldsToLoad, int tickSpeed) {
        //
        if (task != null) {
            log("Task is already running.");
            return;
        } else {
            log("Loading a world every " + tickSpeed + " tick(s), " + worldsToLoad + " world(s) total");
        }
        // initialize the first world
        if (!initialized) {
            initialized = true;
            initialize();
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
                World world = loadWorld(creator, false);
                // unload world
                unloadWorld(world, false, false);
                // reduce count
                if (current >= total) {
                    cancel();
                    task = null;
                    showStats();
                }
            }
        }.runTaskTimer(WorldTester.getInstance(), 1, tickSpeed);
    }

    /*
     Loading one world beforehand is beneficial to allow the JVM to warm up slightly.
     This helps ensure that the statistics are less skewed and enables us to configure
     some of the world's settings, such as its spawn location, ultimately resulting
     in faster world loading.
     */
    private void initialize() {
        log("Initializing first world & setting defaults.");
        final long start = System.nanoTime();
        World world = loadWorld(creator, true);
        world.setSpawnLocation(new Location(world, 0, 60, 0));
        world.setAutoSave(false);
        unloadWorld(world, false, true);
        log("Initialization & unloading of first world done in " + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)) + "ms");
    }

    private void showStats() {
        final long completedIn = TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - taskStarted));
        log(worldLoadTimes.size() + " worlds were loaded in " + MathUtils.convertTime(completedIn));
        // show statistics
        show("World load times", worldLoadTimes);
        show("World unload times", worldUnloadTimes);
        // clear
        worldLoadTimes.clear();
        worldUnloadTimes.clear();
    }

    public final static int ROUNDING = 4;

    private void show(String name, List<Long> times) {
        final double sum = MathUtils.calculateSum(times);
        double average = MathUtils.round(MathUtils.calculateAvg(times, sum), ROUNDING);
        double sd = MathUtils.round(MathUtils.calculateStdDev(times, average), ROUNDING);
        double percentile90 = MathUtils.round(MathUtils.percentile(times, 90), ROUNDING);
        double percentile99 = MathUtils.round(MathUtils.percentile(times, 99), ROUNDING);
        //
        var msg = Component.text();
        msg.append(Component.text(name).color(NamedTextColor.WHITE));
        msg.append(Component.text(" |").color(NamedTextColor.GRAY));
        // average
        msg.append(Component.text(" Mean: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(average + "ms").color(NamedTextColor.AQUA));
        // standard deviation
        msg.append(Component.text(" SD: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(sd + "ms").color(NamedTextColor.AQUA));
        // 90th percentile
        msg.append(Component.text(" 90%ile: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(percentile90 + "ms").color(NamedTextColor.AQUA));
        // 99th percentile
        msg.append(Component.text(" 99%ile: ").color(NamedTextColor.WHITE));
        msg.append(Component.text(percentile99 + "ms").color(NamedTextColor.AQUA));
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
        Bukkit.getConsoleSender().sendMessage(Component.text().append(Component.text("WorldTester").color(NamedTextColor.GREEN)).append(Component.text(" -> ").color(NamedTextColor.GRAY)).append(Component.text(message).color(NamedTextColor.WHITE)).build());
    }

    private void log(Component message) {
        Bukkit.getConsoleSender().sendMessage(Component.text().append(Component.text("WorldTester").color(NamedTextColor.GREEN)).append(Component.text(" -> ").color(NamedTextColor.GRAY)).append(message).build());
    }


}
