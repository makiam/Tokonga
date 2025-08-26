/* Copyright (C) 1999-2000 by Peter Eastman
 *  Changes copyright 2023 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.math;

import Jama.*;
import org.ejml.simple.SimpleMatrix;

/**
 * The SVD class defines methods for solving sets of linear equations by singular value
 * decomposition. It uses classes from the Java Matrix (JAMA) package to factor the
 * matrix. The complete JAMA package, including documentation and source, can be obtained
 * from <a href="http://math.nist.gov/javanumerics/jama/">...</a>
 */
public class SVD {

    private static final double DEFAULT_TOLERANCE = 1.0e-8;

    /**
     * Solve a set of M linear equations in N unknowns. The parameters are:
     * a: an array of size [M][N] containing the matrix of coefficients.
     * b: an array of length max(M,N) containing the right-hand side vector in its first M
     * elements. On exit, the first N elements are overwritten with the solution vector.
     * tol: any singular values smaller than tol*(largest singular value) are set to 0.
     * If tol is omitted, it defaults to 1.0e-8.
     */
    public static void solve(double[][] a, double[] b) {
        solve(a, b, DEFAULT_TOLERANCE);
    }

    public static void solve(double[][] input, double[] b, double tolerance) {

        int matrixRows = input.length;
        int matrixColumns = input[0].length;


        // Factor the matrix.
        SingularValueDecomposition svd = new Matrix(input, matrixRows, matrixColumns).svd();
        double[][] u = svd.getU().getArray();
        double[][] v = svd.getV().getArray();
        double[] s = svd.getSingularValues();
        double cutoff = s[0] * tolerance;
        double[] temp = new double[matrixColumns];

        // Do the back substitution to find the solution vector.

        double d;
        for (int i = 0; i < matrixColumns && s[i] > cutoff; i++) {
            d = 0.0;
            for (int j = 0; j < matrixRows; j++) {
                d += u[j][i] * b[j];
            }
            temp[i] = d / s[i];
        }
        for (int i = 0; i < matrixColumns; i++) {
            d = 0.0;
            for (int j = 0; j < matrixColumns; j++) {
                d += v[i][j] * temp[j];
            }
            b[i] = d;
        }
    }

    public static void solve2(double[][] input, double[] b) {
        solve2(input, b, DEFAULT_TOLERANCE);
    }

    public static void solve2(double[][] input, double[] b, double tolerance) {
        int matrixRows = input.length;
        int matrixColumns = input[0].length;

        var svd = new SimpleMatrix(input).svd();
        var u = svd.getU();
        var v = svd.getU();
        var s = svd.getSingularValues();

        var cutoff = s[0] * tolerance;
        double[] temp = new double[matrixColumns];


        // Do the back substitution to find the solution vector.
        double d;
        for (int i = 0; i < matrixColumns && s[i] > cutoff; i++) {
            d = 0.0;
            for (int j = 0; j < matrixRows; j++) {
                d += u.get(i, j) * b[j];
            }
            temp[i] = d / s[i];
        }

        for (int i = 0; i < matrixColumns; i++) {
            d = 0.0;
            for (int j = 0; j < matrixColumns; j++) {
                d += v.get(i, j) * temp[j];
            }
            b[i] = d;
        }
    }
}
