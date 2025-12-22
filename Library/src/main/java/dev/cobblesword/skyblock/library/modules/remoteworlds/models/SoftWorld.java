package dev.cobblesword.skyblock.library.modules.remoteworlds.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoftWorld
{
    private int worldId;

    private String worldName;

    private String activeOnServerId;

    private boolean isLoaded = false;
}
