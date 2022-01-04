package com.rost.productwarehouse.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DbUtils {

    public static <T> T extract(Collection<T> values) {
        return CollectionUtils.isNotEmpty(values) ? values.iterator().next() : null;
    }

    public static <T> Optional<T> extractOptional(Collection<T> values) {
        return Optional.ofNullable(extract(values));
    }

    public static <T> T extract(Map<?, T> values) {
        return MapUtils.isNotEmpty(values) ? values.values().iterator().next() : null;
    }

    public static <T> Optional<T> extractOptional(Map<?, T> values) {
        return Optional.ofNullable(extract(values));
    }

}
