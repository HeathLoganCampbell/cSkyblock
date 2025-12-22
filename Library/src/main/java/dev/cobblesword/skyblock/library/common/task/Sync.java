package dev.cobblesword.skyblock.library.common.task;

public class Sync
{
    public static BTask get()
    {
        return new BTask().sync();
    }
}