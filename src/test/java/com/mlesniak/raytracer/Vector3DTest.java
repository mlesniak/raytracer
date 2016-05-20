package com.mlesniak.raytracer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Mathematical tests for vector computations.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Vector3DTest {
    @Test
    public void length() {
        Vector3D vec = new Vector3D(1, 2, 3);
        double len = vec.length();
        assertEquals(3.7416573867739413, len, 0.000001);
    }
}
