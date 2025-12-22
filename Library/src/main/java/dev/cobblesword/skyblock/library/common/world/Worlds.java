package dev.cobblesword.skyblock.library.common.world;

import org.bukkit.*;

public class Worlds
{
    private static final EmptyWorldGenerator EMPTY_CHUNK = new EmptyWorldGenerator();

    /**
     * Apply optimizations for static/arena worlds
     */
    public static void initStaticWorld(World world, boolean pvp)
    {
        world.setAutoSave(false);
        world.setAmbientSpawnLimit(0);
        world.setAnimalSpawnLimit(0);
        world.setDifficulty(Difficulty.HARD);
        world.setKeepSpawnInMemory(false);
        world.setMonsterSpawnLimit(0);
        world.setPVP(pvp);
        world.setTicksPerAnimalSpawns(Integer.MAX_VALUE);
        world.setTicksPerMonsterSpawns(Integer.MAX_VALUE);
        world.setWaterAnimalSpawnLimit(0);
        world.setSpawnFlags(false, false);
        if (world.isThundering())
            world.setThunderDuration(Integer.MAX_VALUE);

        world.setWeatherDuration(Integer.MAX_VALUE);
    }

    /**
     * Apply optimizations specifically for skyblock island worlds
     */
    public static void optimizeIslandWorld(World world)
    {
        // Disable weather
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
        world.setClearWeatherDuration(Integer.MAX_VALUE);
        
        // Keep spawn out of memory (we teleport players manually)
        world.setKeepSpawnInMemory(false);
        
        // Disable fire spread and other tick-heavy features
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 5);
        
        // Paper-specific: Set view distance low (only chunks near players)
        // Use reflection since these methods don't exist in Spigot API
        try {
            world.getClass().getMethod("setViewDistance", int.class).invoke(world, 4);
            world.getClass().getMethod("setSimulationDistance", int.class).invoke(world, 4);
        } catch (Exception ignored) {
            // Not Paper or method doesn't exist, skip
        }
        
        world.setAutoSave(true);  // Enable for island data
    }

    public static World createEmptyWorld(String name)
    {
        WorldCreator creator = new WorldCreator(name);
        creator.environment(World.Environment.NORMAL);
        creator.generateStructures(false);
        creator.seed(1L);
        creator.type(WorldType.FLAT);
        creator.keepSpawnInMemory(false);
        creator.generator(EMPTY_CHUNK);
        return creator.createWorld();
    }
}