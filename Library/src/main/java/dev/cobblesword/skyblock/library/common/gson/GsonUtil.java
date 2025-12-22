package dev.cobblesword.skyblock.library.common.gson;

import com.google.gson.Gson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;

public class GsonUtil
{
    private static Gson g = new Gson();
    private static Gson prettyGson;

    public static Gson getGson()
    {
        if (g == null)
            g = new GsonBuilder()
                    .addSerializationExclusionStrategy(new ExposeExlusion())
                    .addDeserializationExclusionStrategy(new ExposeExlusion())
                    .registerTypeAdapter(Location.class, new LocationGsonAdapter(g))
                    .disableHtmlEscaping()
                    .create();
        return g;
    }

    public static Gson getPrettyGson ()
    {
        if (prettyGson == null)
            prettyGson = new GsonBuilder()
                    .addSerializationExclusionStrategy(new ExposeExlusion())
                    .addDeserializationExclusionStrategy(new ExposeExlusion())
                    .registerTypeAdapter(Location.class, new LocationGsonAdapter(g))
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        return prettyGson;
    }

    public static Gson getSimplePrettyGson ()
    {
        if (prettyGson == null)
            prettyGson = new GsonBuilder()
                    .addSerializationExclusionStrategy(new ExposeExlusion())
                    .addDeserializationExclusionStrategy(new ExposeExlusion())
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .create();
        return prettyGson;
    }
}