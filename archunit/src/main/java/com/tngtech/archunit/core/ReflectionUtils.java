package com.tngtech.archunit.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Collections2.filter;
import static com.tngtech.archunit.core.GuavaConversion.toGuava;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static <T> T newInstanceOf(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ReflectionException(e);
        }
    }

    public static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    public static Set<Class<?>> getAllSuperTypes(Class<?> type) {
        if (type == null) {
            return Collections.emptySet();
        }

        ImmutableSet.Builder<Class<?>> result = ImmutableSet.<Class<?>>builder()
                .add(type)
                .addAll(getAllSuperTypes(type.getSuperclass()));
        for (Class<?> c : type.getInterfaces()) {
            result.addAll(getAllSuperTypes(c));
        }
        return result.build();
    }

    public static Collection<Constructor<?>> getAllConstructors(Class<?> owner, FluentPredicate<? super Constructor<?>> predicate) {
        return filter(getAll(owner, new Collector<Constructor<?>>() {
            @Override
            protected Collection<? extends Constructor<?>> extractFrom(Class<?> type) {
                return ImmutableList.copyOf(type.getDeclaredConstructors());
            }
        }), toGuava(predicate));
    }

    public static Collection<Field> getAllFields(Class<?> owner, FluentPredicate<? super Field> predicate) {
        return filter(getAll(owner, new Collector<Field>() {
            @Override
            protected Collection<? extends Field> extractFrom(Class<?> type) {
                return ImmutableList.copyOf(type.getDeclaredFields());
            }
        }), toGuava(predicate));
    }

    public static Collection<Method> getAllMethods(Class<?> owner, FluentPredicate<? super Method> predicate) {
        return filter(getAll(owner, new Collector<Method>() {
            @Override
            protected Collection<? extends Method> extractFrom(Class<?> type) {
                return ImmutableList.copyOf(type.getDeclaredMethods());
            }
        }), toGuava(predicate));
    }

    private static <T> List<T> getAll(Class<?> type, Collector<T> collector) {
        for (Class<?> t : getAllSuperTypes(type)) {
            collector.collectFrom(t);
        }
        return collector.collected;
    }

    private static abstract class Collector<T> {
        private final List<T> collected = new ArrayList<>();

        void collectFrom(Class<?> type) {
            collected.addAll(extractFrom(type));
        }

        protected abstract Collection<? extends T> extractFrom(Class<?> type);
    }
}