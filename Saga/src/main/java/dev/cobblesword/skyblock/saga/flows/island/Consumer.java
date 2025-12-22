package dev.cobblesword.skyblock.saga.flows.island;

public interface Consumer<T> {
    void accept(T t);
}
