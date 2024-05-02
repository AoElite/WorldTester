package com.aoelite.me.worldtester.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MathUtils {

    public static <T extends Comparable<T>> T percentile(List<T> items, double percentile) {
        List<T> list = new ArrayList<>(items);
        Collections.sort(list);
        return list.get((int) Math.round(percentile / 100.0 * (list.size() - 1)));
    }

    public static <T extends Number> double calculateSD(Collection<T> numbers) {
        double sum = calculateSum(numbers);
        double mean = calculateAvg(numbers, sum);
        return calculateStdDev(numbers, mean);
    }

    public static <T extends Number> double calculateAvg(Collection<T> numbers, double sum) {
        return sum / numbers.size();
    }

    public static <T extends Number> double calculateSum(Collection<T> numbers) {
        double total = 0;
        for (T number : numbers) {
            total += number.doubleValue();
        }
        return total;
    }

    public static <T extends Number> double calculateStdDev(Collection<T> numbers, double mean) {
        double sumOfSquaredDifferences = numbers.stream().mapToDouble(num -> Math.pow(num.doubleValue() - mean, 2)).sum();
        return Math.sqrt(sumOfSquaredDifferences / numbers.size());
    }

    public static double round(double number, int place) {
        double exp = Math.pow(10, place);
        return Math.round(number * exp) / exp;
    }

    public static String convertTime(final long time) {
        StringBuilder sb = new StringBuilder();
        long ms = time;
        long seconds = (time / 1000);
        ms -= seconds * 1000;
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        long hours = minutes / 60;
        minutes -= hours * 60;
        long days = hours / 24;
        hours -= days * 24;
        long years = days / 365;
        days -= years * 365;
        if (years > 0) sb.append(years).append("y ");
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s ");
        if (ms > 0) sb.append(ms).append("ms");
        return sb.toString().trim();
    }

}
