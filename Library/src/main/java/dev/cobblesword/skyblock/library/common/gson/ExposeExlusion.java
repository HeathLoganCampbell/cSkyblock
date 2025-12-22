package dev.cobblesword.skyblock.library.common.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.Expose;

public class ExposeExlusion implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        final Expose expose = fieldAttributes.getAnnotation(Expose.class);
        return expose != null && (!expose.serialize() || !expose.deserialize());
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}