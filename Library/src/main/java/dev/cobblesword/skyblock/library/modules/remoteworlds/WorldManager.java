package dev.cobblesword.skyblock.library.modules.remoteworlds;

import dev.cobblesword.skyblock.library.modules.remoteworlds.databases.IWorldDatabase;
import dev.cobblesword.skyblock.library.modules.remoteworlds.databases.MySQLWorldDatabase;
import dev.cobblesword.skyblock.library.common.files.ZipUtil;
import dev.cobblesword.skyblock.library.common.world.Worlds;
import dev.cobblesword.skyblock.library.modules.remoteworlds.models.SoftWorld;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class WorldManager
{
    private IWorldDatabase worldDatabase;

    public WorldManager() {
        this.worldDatabase = new MySQLWorldDatabase();
    }

    public void loadOrCreateWorld(String worldName)
    {
        boolean isNewWorld = true;
        File zippedWorldFile = new File(worldName + ".zip");
        this.worldDatabase.downloadWorldFile(worldName, zippedWorldFile);

        if(zippedWorldFile.exists())
        {
            isNewWorld = false;

            try {
                String parent = new File(worldName).getParent();
                ZipUtil.unzipFile(worldName + ".zip", parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        World islandWorld = Worlds.createEmptyWorld(worldName);

        if(isNewWorld)
        {
            islandWorld.setSpawnLocation(0, 100, 0);

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    islandWorld.getBlockAt(x, 99, z).setType(Material.GRASS_BLOCK);
                    islandWorld.getBlockAt(x, 98, z).setType(Material.DIRT);
                    islandWorld.getBlockAt(x, 97, z).setType(Material.DIRT);
                }
            }

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    islandWorld.getBlockAt(x + 3, 99, z).setType(Material.GRASS_BLOCK);
                    islandWorld.getBlockAt(x + 3, 98, z).setType(Material.DIRT);
                    islandWorld.getBlockAt(x + 3, 97, z).setType(Material.DIRT);
                }
            }

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    islandWorld.getBlockAt(x, 99, z + 3).setType(Material.GRASS_BLOCK);
                    islandWorld.getBlockAt(x, 98, z + 3).setType(Material.DIRT);
                    islandWorld.getBlockAt(x, 97, z + 3).setType(Material.DIRT);
                }
            }

            islandWorld.generateTree(new Location(islandWorld, 4, 100, 1), TreeType.BIRCH);
            islandWorld.getBlockAt(1, 100, 4).setType(Material.CHEST);
            Chest chestState = (Chest) islandWorld.getBlockAt(1, 100, 4).getState();
            Inventory chestInv = chestState.getInventory();
            chestInv.addItem(new ItemStack[] {
                    new ItemStack(Material.LAVA_BUCKET),
                    new ItemStack(Material.ICE)
            });
        }
    }

    public void unloadWorld(String worldName) {
        Bukkit.unloadWorld(worldName, true);
    }

    public void save(String worldName)
    {
        World world = Bukkit.getWorld(worldName);
        world.save();

        try {
            ZipUtil.zipFile(worldName, worldName + ".zip");

            saveWorldFile(worldName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveWorldFile(String worldName) throws IOException {
        if(worldDatabase.isInDatabase(worldName))
        {
            System.out.println("Already found " + worldName + " in database, gonna update it");
            worldDatabase.updateWorldFile(worldName, new File(worldName + ".zip"));
        }
        else
        {
            System.out.println("new world " + worldName + " gonna create a new row");
            worldDatabase.createWorldFile(worldName, new File(worldName + ".zip"));
        }

        deleteDirectory(Path.of(worldName + ".zip"));
    }

    public void unloadAndSave(String worldName)
    {
        World world = Bukkit.getWorld(worldName);

        for (Player player : world.getPlayers()) {
            player.kickPlayer("unloading world");
        }

        Bukkit.unloadWorld(worldName, true);
        try {
            ZipUtil.zipFile(worldName, worldName + ".zip");
            deleteDirectory(Path.of(worldName));

            saveWorldFile(worldName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<SoftWorld> getRemoteWorlds(int page, int limit)
    {
        for (SoftWorld remoteWorld : worldDatabase.getRemoteWorlds(page, limit))
        {
            World world = Bukkit.getWorld(remoteWorld.getWorldName());
            if(world != null)
            {
                remoteWorld.setLoaded(true);
            }
        }

        return worldDatabase.getRemoteWorlds(page, limit);
    }

    public static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        Files.walkFileTree(directoryToBeDeleted, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
