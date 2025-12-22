package dev.cobblesword.skyblock.library.common.task;

public class Async
{
    public static BTask get()
    {
        return new BTask().async();
    }
}