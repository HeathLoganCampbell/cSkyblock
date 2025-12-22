package dev.cobblesword.exchange.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil
{
    private static Gson g = new Gson();
    public static Gson getGson()
    {
        if (g == null)
            g = new GsonBuilder()
                    .disableHtmlEscaping()
                    .create();
        return g;
    }
}