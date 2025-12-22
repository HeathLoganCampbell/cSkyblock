package dev.cobblesword.skyblock.islands.modules.properties.properties.farms;

import org.bukkit.Location;

public class EggFarmProperty
{
    private double eggsPerMinute = 0.1;
    private long lastDropTimestamp = System.currentTimeMillis();

    public void onLoad()
    {

    }

    public void onUnload()
    {

    }

    public void onTick()
    {
        if(System.currentTimeMillis() - lastDropTimestamp < 60_000L)
        {
            return;
        }

        int eggsToGive = 0;

        long lastDropDurationMilli = System.currentTimeMillis() - lastDropTimestamp;
        long numberOfMinutesPast = lastDropDurationMilli / 60_000L;
        eggsToGive = (int) Math.floor(eggsPerMinute * numberOfMinutesPast);

        if(eggsToGive > 0)
        {
            // yay give stuff
        }
    }

    public void getItem()
    {

    }

    protected void onAdd(Location placedLocation)
    {

    }

    protected void onRemove()
    {

    }

    public void UpdateTree()
    {
        // Rate

        // Special

        //
    }
}
