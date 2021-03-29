package com.openwebserver.utils.OpenAPI.utils;


import com.together.Pair;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class MyCollection {

    @SuppressWarnings({"unused","unchecked"})
    @SafeVarargs
    public static <T> T Concat(T ... objects) {
        int size = 0;
        for (T object : objects) {
            if (objects.getClass().isArray()) {
                size += Array.getLength(object);
            } else {
                size++;
            }
        }
        Object o = Array.newInstance(objects[0].getClass().getComponentType(),size);
        int index = 0;
        for (T object : objects) {
            if (objects.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    Array.set(o,index, Array.get(object, i));
                    index++;
                }
            } else {
                Array.set(o,index, object);
                index++;
            }
        }
        return (T) o;
    }

    @SafeVarargs
    public static <K,V> HashMap<K, V> Index(Function<V, Pair<K,V>> function, V ... objects){
        return Index(function, new ArrayList<>(Arrays.asList(objects)));
    }

    @SuppressWarnings({"unused", "unchecked"})
    public static <T> T[] ReInit(Object[] objects, Function<Object,T> parser){
        Object array = Array.newInstance(objects[0].getClass(), objects.length);
        for (int i = 0; i < objects.length; i++) {
            Array.set(array,i, parser.apply(objects[i]));
        }
        return (T[]) array;
    }

    public static <K,V> HashMap<K, V> Index(Function<V, Pair<K,V>> function, ArrayList<V> list){
        HashMap<K,V> indexed = new HashMap<>();
        list.forEach(v -> {
            Pair<K,V> keyValue = function.apply(v);
            indexed.put(keyValue.getKey(), keyValue.getValue());
        });
        return indexed;
    }

    public static <K,V> void Index(List<V> list, Function<V, K> keyFunction, BiConsumer<K,V> consumer){
        list.forEach(item -> consumer.accept(keyFunction.apply(item), item));
    }

    public static <K,V> void Index(V[] list, Function<V, K> keyFunction, BiConsumer<K,V> consumer){
        for (V v : list) {
            Index(v, keyFunction,consumer);
        }
    }

    public static <K,V> void Index(V v, Function<V, K> keyFunction, BiConsumer<K,V> consumer){
        consumer.accept(keyFunction.apply(v), v);
    }

    public static <T> void doIf(boolean condition, Consumer<T> resultConsumer, T result){
        if(condition){
            resultConsumer.accept(result);
        }
    }

    @SuppressWarnings("unused")
    public static void doIf(boolean condition, Consumer<?> resultConsumer){
        doIf(condition, resultConsumer, null);
    }

    public static <T> void doForEach(T[] objects, Function<T,Boolean> conditionFunction, Consumer<T> resultConsumer){
        for (T object : objects) {
            doIf(conditionFunction.apply(object),resultConsumer, object);
        }
    }

    public static <T> void doForEach(Collection<T> objects, Function<T,Boolean> conditionFunction, Consumer<T> resultConsumer){
        objects.forEach(t -> doIf(conditionFunction.apply(t),resultConsumer, t));
    }

}
