/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.functions.analysis;

import java.util.Arrays;
import jdplus.math.functions.CubicSpline;

/**
 *
 * @author PALATEJ
 */
public class PiecewiseBicubicSplineInterpolatingFunction {

    /**
     * The minimum number of points that are needed to compute the function.
     */
    private static final int MIN_NUM_POINTS = 5;
    /**
     * Samples x-coordinates
     */
    private final double[] xval;
    /**
     * Samples y-coordinates
     */
    private final double[] yval;
    /**
     * Set of cubic splines patching the whole data grid
     */
    private final double[][] fval;

    /**
     * @param x Sample values of the x-coordinate, in increasing order.
     * @param y Sample values of the y-coordinate, in increasing order.
     * @param f Values of the function on every grid point. the expected number
     * of elements.
     */
    public PiecewiseBicubicSplineInterpolatingFunction(double[] x,
            double[] y,
            double[][] f) {
        int xLen = x.length;
        int yLen = y.length;

        this.xval = x;
        this.yval = y;
        this.fval = f;
    }

    /**
     * {@inheritDoc}
     */
    public double value(double x,
            double y) {
        final int offset = 2;
        final int count = offset + 3;
        final int i = searchIndex(x, xval, offset, count);
        final int j = searchIndex(y, yval, offset, count);

        final double xArray[] = new double[count];
        final double yArray[] = new double[count];
        final double zArray[] = new double[count];
        final double interpArray[] = new double[count];

        for (int index = 0; index < count; index++) {
            xArray[index] = xval[i + index];
            yArray[index] = yval[j + index];
        }

        for (int zIndex = 0; zIndex < count; zIndex++) {
            for (int index = 0; index < count; index++) {
                zArray[index] = fval[i + index][j + zIndex];
            }
            double fx = CubicSpline.of(xArray, zArray).applyAsDouble(x);
            interpArray[zIndex] = fx;
        }
        return CubicSpline.of(yArray, interpArray).applyAsDouble(y);
    }

    /**
     * Indicates whether a point is within the interpolation range.
     *
     * @param x First coordinate.
     * @param y Second coordinate.
     * @return {@code true} if (x, y) is a valid point.
     * @since 3.3
     */
    public boolean isValidPoint(double x,
            double y) {
        if (x < xval[0]
                || x > xval[xval.length - 1]
                || y < yval[0]
                || y > yval[yval.length - 1]) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param c Coordinate.
     * @param val Coordinate samples.
     * @param offset how far back from found value to offset for querying
     * @param count total number of elements forward from beginning that will be
     * queried
     * @return the index in {@code val} corresponding to the interval containing
     * {@code c}.
     */
    private int searchIndex(double c,
            double[] val,
            int offset,
            int count) {
        int r = Arrays.binarySearch(val, c);

        if (r == -1 || r == -val.length - 1) {
            return -1;
        }

        if (r < 0) {
            // "c" in within an interpolation sub-interval, which returns
            // negative
            // need to remove the negative sign for consistency
            r = -r - offset - 1;
        } else {
            r -= offset;
        }

        if (r < 0) {
            r = 0;
        }

        if ((r + count) >= val.length) {
            // "c" is the last sample of the range: Return the index
            // of the sample at the lower end of the last sub-interval.
            r = val.length - count;
        }

        return r;
    }
}
