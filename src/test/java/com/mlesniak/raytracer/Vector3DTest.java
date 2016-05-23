package com.mlesniak.raytracer;

import com.mlesniak.raytracer.math.Vector3D;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Mathematical tests for vector computations.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Vector3DTest {
    private static final double DELTA = 0.000001;

    @Test
    public void length() {
        final int x = 1;
        final int y = 2;
        final int z = 3;
        Vector3D vec = new Vector3D(x, y, z);
        double len = vec.length();

        final double expected = 3.7416573867739413;
        assertEquals(expected, len, DELTA);
    }
}
