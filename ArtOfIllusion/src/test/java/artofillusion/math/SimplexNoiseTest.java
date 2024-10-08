/* Copyright (C) 2006 by Peter Eastman
   Changes copyright (C) 2017-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.math;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Simplex Noise Test")
class SimplexNoiseTest {

    @Test
    @DisplayName("Test Range")
    void testRange() {
        for (int i = 0; i < 10000; i++) {
            double x = rand();
            double y = rand();
            double z = rand();
            double w = rand();
            double val2 = SimplexNoise.noise(x, y);
            Assertions.assertTrue(val2 >= -1 && val2 <= 1);
            double val3 = SimplexNoise.noise(x, y, z);
            Assertions.assertTrue(val3 >= -1 && val3 <= 1);
            double val4 = SimplexNoise.noise(x, y, w);
            Assertions.assertTrue(val4 >= -1 && val4 <= 1);
        }
    }

    @Test
    @DisplayName("Test Gradient 2 D")
    void testGradient2D() {
        // Estimate the gradient by finite difference, and compare that to the value returned by
        // noiseGradient.  Usually they are very close, but if the two evaluation points happen
        // to be in different cells, the result can occassionally be far off.  We therefore
        // tolerate a few bad points.
        final double DELTA = 1e-5;
        int badCount = 0;
        for (int i = 0; i < 10000; i++) {
            double x = rand();
            double y = rand();
            Vec2 grad = new Vec2();
            SimplexNoise.noiseGradient(grad, x, y);
            double center = SimplexNoise.noise(x, y);
            boolean close = (Math.abs((SimplexNoise.noise(x + DELTA, y) - center) / DELTA - grad.x) < 1.0e-3 && Math.abs((SimplexNoise.noise(x, y + DELTA) - center) / DELTA - grad.y) < 1.0e-3);
            if (!close) {
                badCount++;
            }
        }
        Assertions.assertTrue(badCount < 10);
    }

    @Test
    @DisplayName("Test Gradient 3 D")
    void testGradient3D() {
        // Estimate the gradient by finite difference, and compare that to the value returned by
        // noiseGradient.  Usually they are very close, but if the two evaluation points happen
        // to be in different cells, the result can occassionally be far off.  We therefore
        // tolerate a few bad points.
        final double DELTA = 1e-5;
        int badCount = 0;
        for (int i = 0; i < 10000; i++) {
            double x = rand();
            double y = rand();
            double z = rand();
            Vec3 grad = new Vec3();
            SimplexNoise.noiseGradient(grad, x, y, z);
            double center = SimplexNoise.noise(x, y, z);
            boolean close = (Math.abs((SimplexNoise.noise(x + DELTA, y, z) - center) / DELTA - grad.x) < 1.0e-3 && Math.abs((SimplexNoise.noise(x, y + DELTA, z) - center) / DELTA - grad.y) < 1.0e-3 && Math.abs((SimplexNoise.noise(x, y, z + DELTA) - center) / DELTA - grad.z) < 1.0e-3);
            if (!close) {
                badCount++;
            }
        }
        Assertions.assertTrue(badCount < 10);
    }

    @Test
    @DisplayName("Test Vector")
    void testVector() {
        Vec3 v = new Vec3();
        double avgLength = 0.0;
        for (int i = 0; i < 10000; i++) {
            double x = rand();
            double y = rand();
            double z = rand();
            SimplexNoise.noiseVector(v, x, y, z);
            double len = v.length();
            Assertions.assertTrue(len < 1.5);
            avgLength += len;
        }
        avgLength /= 10000;
        Assertions.assertTrue(avgLength > 0.3);
    }

    private double rand() {
        return 1000.0 * Math.random() - 500.0;
    }
}
