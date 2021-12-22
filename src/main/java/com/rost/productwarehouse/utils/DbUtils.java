package com.rost.productwarehouse.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

public class DbUtils {

    public static <T> T extract(Collection<T> values) {
        return CollectionUtils.isNotEmpty(values) ? values.iterator().next() : null;
    }

}
