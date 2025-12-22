package dev.cobblesword.skyblock.library.modules.remoteworlds.databases;

import dev.cobblesword.skyblock.library.modules.remoteworlds.models.SoftWorld;

import java.io.File;
import java.util.List;

public interface IWorldDatabase
{
    void createWorldFile(String worldName, File file);

    void updateWorldFile(String worldName, File fileToUpload);

    void downloadWorldFile(String worldName, File distFile);

    boolean isInDatabase(String worldName);

    List<SoftWorld> getRemoteWorlds(int page, int limit);
}
