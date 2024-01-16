package com.aoelite.me.worldtester.utils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class MathUtils {

    public static double calculateSD(Collection<? extends Number> numbers) {
        return Math.sqrt(calculateVariance(numbers));
    }

    public static double calculateVariance(final Collection<? extends Number> data) {
        int count = 0;
        double sum = 0, variance = 0, average;
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }
        average = sum / count;
        for (final Number number : data) variance += Math.pow(number.doubleValue() - average, 2.0);
        return variance;
    }


    public static double calculateAvg(final Collection<? extends Number> numbers) {
        return numbers.stream().mapToDouble(Number::doubleValue).average().orElse(0D);
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
