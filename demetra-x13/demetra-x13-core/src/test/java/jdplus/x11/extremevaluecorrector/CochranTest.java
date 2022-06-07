/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11.extremevaluecorrector;

import jdplus.x11.X11Context;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import demetra.data.DoubleSeq;

/**
 *
 * @author Christiane Hofer
 */
public class CochranTest {

    private static final double[] INPUT = {73.4, 101.1, 126.0, 141.2, 144.7, 147.4, 147.9, 143.3,
        153.6, 155.0, 149.1, 92.0, 60.3, 50.7, 95.8, 136.1, 139.4, 148.8, 143.7, 137.4, 153.1,
        147.5, 137.8, 103.1, 50.5, 72.2, 117.4, 128.2, 126.6, 135.3, 136.7, 125.4, 142.3,
        141.2, 133.4, 91.6, 72.9, 74.1, 109.6, 119.6, 122.3, 129.9, 130.9, 121.3, 136.0,
        132.7, 122.0, 79.1, 78.2, 67.1, 112.1, 125.9, 126.6, 133.2, 135.7, 126.6, 140.8,
        141.9, 129.4, 90.9, 73.4, 86.1, 107.7, 121.8, 125.2, 125.5, 128.3, 121.3, 132.4,
        129.3, 126.7, 90.3, 64.0, 76.9, 97.6, 111.6, 120.3, 126.3, 127.5, 121.9, 133.5,
        134.3, 123.7, 88.4, 60.9, 75.9, 105.7, 116.8, 115.1, 122.0, 122.6, 116.3, 129.6,
        125.8, 121.2, 75.3, 55.7, 56.3, 99.4, 112.3, 109.5, 118.4, 121.5, 110.6, 122.5,
        122.0, 115.7, 80.8, 50.5, 62.4, 87.6, 101.3, 102.9, 110.3, 114.3, 105.3, 116.0,
        117.1, 109.3, 76.1, 53.3, 48.2, 70.3, 99.6, 102.4, 109.0, 113.2, 105.3, 115.5,
        118.0, 108.7, 74.0, 43.7, 47.9, 69.9, 105.7, 109.8, 113.3, 119.6, 111.5, 122.4,
        121.4, 120.0, 84.9, 60.5, 66.7, 91.8, 104.6, 106.2, 112.5, 116.5, 109.9, 119.7,
        120.0, 111.7, 81.2, 65.1, 74.3, 90.1, 103.9, 107.3, 112.8, 115.3, 111.7, 120.3,
        118.6, 113.7, 79.7, 46.7, 55.8, 94.3, 110.8, 111.6, 117.0, 119.5, 118.7, 124.5,
        122.4, 115.8, 77.8, 40.1, 43.9, 91.5, 115.3, 113.3, 119.3, 123.5, 116.4, 125.5,
        129.0, 120.2, 52.5, 59.7, 73.3, 111.1, 123.5, 123.9, 122.6, 131.8, 124.3, 130.9,
        131.6, 130.7, 80.4, 62.0, 51.5, 108.5, 117.4, 120.8, 123.4, 130.1, 121.7, 132.7,
        130.3, 125.7, 75.8, 51.5, 60.1, 87.5, 121.3, 121.5, 130.4, 135.9, 127.6, 136.3,
        133.6, 131.1, 90.7, 68.8, 80.8, 112.6, 123.8, 121.6, 130.2, 133.5, 125.8, 136.3,
        135.8};

    public static final DoubleSeq INPUT_DS = DoubleSeq.of(INPUT);

    @Test
    public void CochranTest() {
        Cochran cochran;
        X11Context context = X11Context.builder()
                .mode(demetra.sa.DecompositionMode.Multiplicative)
                .period(12)
                .build();

        cochran = new Cochran(INPUT_DS, context);
        assertEquals(true, cochran.getTestResult());
        double[] actualStandardDeviation = cochran.getStandardDeviation();
        double[] expectedStandardDeviation = {3535.889, 4465.5955, 9870.1095, 13583.4685, 13937.615, 15341.996, 16068.224, 14279.8785, 17062.8645, 16839.1775, 15099.7847, 6722.4789};

        org.junit.Assert.assertArrayEquals(expectedStandardDeviation, actualStandardDeviation, 0.0001);
        assertEquals(0.116226, cochran.getTestValue(), 0.000001);
        assertEquals(0.166, cochran.getCriticalValue(), 0.000001);
        assertEquals(true, cochran.getTestResult());
    }

    @Test
    public void CochranMissingValuesTest() {
        Cochran cochran;
        X11Context context = X11Context.builder()
                .mode(demetra.sa.DecompositionMode.Multiplicative)
                .period(12)
                .build();
        DoubleSeq missingValuesStartEnd = INPUT_DS.extend(6, 6);
        cochran = new Cochran(missingValuesStartEnd, context);

        double[] actualStandardDeviation = cochran.getStandardDeviation();
        double[] expectedStandardDeviation = {16068.224, 14279.8785, 17062.8645, 16839.1775, 15099.7847, 6722.4789, 3535.889, 4465.5955, 9870.1095, 13583.4685, 13937.615, 15341.996};

        org.junit.Assert.assertArrayEquals(expectedStandardDeviation, actualStandardDeviation, 0.0001);

        assertEquals(0.116226, cochran.getTestValue(), 0.000001);
        assertEquals(0.166, cochran.getCriticalValue(), 0.000001);
        assertEquals(true, cochran.getTestResult());

    }
}
