package dev.cobblesword.skyblock.library.common.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyWorldGenerator extends ChunkGenerator {
    @Override
    public ChunkData generateChunkData(World world, java.util.Random random, int x, int z, BiomeGrid biome)
    {
        return createChunkData(world);
    }
}