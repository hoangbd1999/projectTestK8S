package com.elcom.adminconsolebackend.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public final class StreamUtils {

    public static <E1, E2, C extends Collection<E2>> C map(
            Collection<E1> source,
            Function<? super E1, ? extends E2> mapper,
            Supplier<C> targetFactory
    ) {
        source = source == null ? Collections.emptyList() : source;
        return source.stream()
                .map(mapper)
                .collect(Collectors.toCollection(targetFactory));
    }

    public static <E, C extends Collection<E>> C filter(
            Collection<E> source,
            Predicate<? super E> predicate,
            Supplier<C> targetFactory
    ) {
        source = source == null ? Collections.emptyList() : source;
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toCollection(targetFactory));

    }

    public static <E1, E2> List<E2> toList(Collection<E1> source, Function<? super E1, ? extends E2> mapper) {
        if (source == null) {
            return Collections.emptyList();
        }
        return source.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <E1, E2> List<E2> toList(E1[] source, Function<? super E1, ? extends E2> mapper) {
        if (source == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(source)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <E1, E2> Set<E2> toSet(Collection<E1> source, Function<? super E1, ? extends E2> mapper) {
        if (source == null) {
            return Collections.emptySet();
        }
        return source.stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> source,
                                            Function<? super T, K> keyMapper,
                                            Function<? super T, V> valueMapper) {
        if (source == null) {
            return Collections.emptyMap();
        }
        return source.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <T, K> Map<K, T> selfMap(Collection<T> source, Function<? super T, K> keyMapper) {
        return toMap(source, keyMapper, Function.identity());
    }

    public static <E1, E2, C extends Collection<E2>> C filterThenMap(
            Collection<E1> source,
            Predicate<? super E1> predicate,
            Function<? super E1, ? extends E2> mapper,
            Supplier<C> targetFactory
    ) {
        source = source == null ? Collections.emptyList() : source;
        return source.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toCollection(targetFactory));
    }

    public static <E1> List<E1> filterThenToList(
            Collection<E1> source,
            Predicate<? super E1> predicate
    ) {
        if (source == null) {
            return Collections.emptyList();
        }
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static <E1, E2> List<E2> filterThenToList(
            Collection<E1> source,
            Predicate<? super E1> predicate,
            Function<? super E1, ? extends E2> mapper
    ) {
        if (source == null) {
            return Collections.emptyList();
        }
        return source.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <E1> Set<E1> filterThenToSet(
            Collection<E1> source,
            Predicate<? super E1> predicate
    ) {
        if (source == null) {
            return Collections.emptySet();
        }
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toSet());
    }

    public static <E1, E2> Set<E2> filterThenToSet(
            Collection<E1> source,
            Predicate<? super E1> predicate,
            Function<? super E1, ? extends E2> mapper
    ) {
        if (source == null) {
            return Collections.emptySet();
        }
        return source.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    public static <E, K, V> Map<K, V> filterThenToMap(
            Collection<E> source,
            Predicate<? super E> predicate,
            Function<? super E, ? extends K> keyMapper,
            Function<? super E, ? extends V> valueMapper
    ) {
        if (source == null) {
            return Collections.emptyMap();
        }
        return source.stream()
                .filter(predicate)
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <E, K> Map<K, E> filterThenSelfMap(
            Collection<E> source,
            Predicate<? super E> predicate,
            Function<? super E, ? extends K> keyMapper
    ) {
        return filterThenToMap(source, predicate, keyMapper, Function.identity());
    }

    public static <E1, E2, K, C extends Collection<E2>> Map<K, C> groupingBy(
            Collection<E1> source,
            Function<? super E1, ? extends K> keyMapper,
            Function<? super E1, ? extends E2> valueItemMapper,
            Supplier<C> valueCollectionFactory
    ) {
        if (source == null) {
            return Collections.emptyMap();
        }
        return source.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.mapping(valueItemMapper, Collectors.toCollection(valueCollectionFactory))
                ));
    }

    public static <E, K, C extends Collection<E>> Map<K, C> groupingBy(
            Collection<E> source,
            Function<? super E, ? extends K> keyMapper,
            Supplier<C> valueCollectionFactory
    ) {
        return groupingBy(source, keyMapper, Function.identity(), valueCollectionFactory);
    }

    public static <E, K> Map<K, List<E>> groupingBy(
            Collection<E> source,
            Function<? super E, ? extends K> keyMapper
    ) {
        return groupingBy(source, keyMapper, Function.identity(), () -> new ArrayList<>(source.size()));
    }

    public static <E1, E2, K, C extends List<? extends E2>> Map<K, C> groupingBy(
            Collection<E1> source,
            Function<? super E1, ? extends K> keyMapper,
            Function<? super E1, ? extends E2> valueItemMapper
    ) {
        return (Map<K, C>) groupingBy(source, keyMapper, valueItemMapper, ArrayList::new);
    }

    public static <E> Page<E> paging(Collection<E> source, Pageable pageable) {
        List<E> content = source.stream().skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, source.size());
    }

}