package dev.cobblesword.skyblock.proxy.islands;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Island
{
    private UUID id;

    private String serverId;

    private UUID worldId;
}
