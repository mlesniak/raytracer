package com.mlesniak.raytracer.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple stopwatch for performance measurements.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Stopwatch {
    private static Map<String, Long> startTimes = new HashMap<>();

    public static void start(String id) {
        startTimes.put(id, System.currentTimeMillis());
    }

    public static long stop(String id) {
        return System.currentTimeMillis() - startTimes.get(id);
    }
}
