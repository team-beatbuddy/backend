package com.ceos.beatbuddy.domain.vector.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorUtils {

    public static List<Double> parseVector(String vectorString) {
        return Arrays.stream(vectorString.replaceAll("[\\[\\]\\s]", "").split(","))
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    public static List<String> getSelectedItems(List<Double> vector, List<String> allItems) {
        return IntStream.range(0, vector.size())
                .filter(i -> i < allItems.size() && vector.get(i) == 1.0)
                .mapToObj(allItems::get)
                .collect(Collectors.toList());
    }
}