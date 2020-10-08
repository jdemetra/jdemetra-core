/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.DoubleSeq;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;

/**
 *
 * @author PALATEJ
 */
public class StsOutliersDetectionTest {

    public StsOutliersDetectionTest() {
    }

    @Test
    public void testSomeMethod() {
        TsData y = TsData.ofInternal(TsPeriod.monthly(1974, 1), sugar);
        StsOutliersDetection.Results rslt = StsOutliersDetection.process(y, 1, 1, 1, "HarrisonStevens", null, 0, 0, "Score", "Point");
        System.out.println(rslt.getComponents());
        System.out.println("");
        System.out.println(rslt.getInitialTau());
        System.out.println("");
        System.out.println(rslt.getRegressors());
        System.out.println("");
        System.out.println(rslt.getLinearized());
    }

    double[] sugar = new double[]{34.8, 21, 32.2, 22.4, 18.2, 15.9, 13.3, 16.7, 19, 91.5, 246.5, 258.3, 126.5, 19.7, 24.8, 16.7, 12.2, 14.3, 10.9, 14.9, 17.6, 211.7, 358.3, 201,
        4, 34.6, 31.3, 17, 16.1, 16.8, 9.6, 18.1, 18.5, 215.4, 329.6, 244.3, 21.4, 16.9, 37.5, 16.8, 14.4, 30, 12, 14.3, 17.4, 242.3, 323.4, 257.3, 16,
        13.6, 18.9, 28.8, 20.4, 43.1, 7.8, 17.2, 23.3, 240.2, 371.5, 272.5, 24.5, 22.1, 25.6, 29.8, 47.3, 16.4, 10, 17.8, 24.3, 322.1, 380, 318.9, 31,
        30.8, 31.5, 42.3, 22.6, 18.2, 9.3, 21, 27.3, 305.6, 352.2, 251.2, 17.2, 17.9, 34.4, 31.7, 28.7, 21.2, 9, 16.1, 57.5, 351.1, 379.1, 358.5, 108.2,
        20, 34.2, 28.2, 27.1, 17.5, 9.2, 16.3, 107.8, 373.5, 399.3, 381, 26.7, 15.4, 18.8, 39.7, 21.6, 21.1, 6.6, 19.1, 18.8, 257.9, 419.3, 234, 16.9,
        19.1, 19.5, 28.6, 23.5, 21.5, 17.6, 19, 10, 279.3, 406.3, 291.2, 19, 16.1, 24.6, 30.1, 18.9, 19.2, 5, 12.3, 34.6, 367.8, 423.5, 229.5, 17, 12.6,
        14.7, 48.4, 24.9, 23.2, 18.6, 17.1, 33.9, 310.1, 365.1, 177.1, 64.1, 74.1, 82.5, 75.7, 70.7, 78.2, 25.6, 78.3, 96.5, 138.1, 137.2, 93.6, 65.9, 71.6,
        79.4, 67.8, 64, 75.4, 16.1, 74.2, 90.7, 134.3, 132.1, 82, 56.7, 61, 63.7, 63.2, 57.7, 68.5, 16.1, 64, 80.9, 115.7, 115.8, 91.3, 56, 60.3, 65.6, 53.5,
        59.5, 61.4, 17.3, 64.1, 82.8, 112.1, 108.9, 79.6, 60.1, 56.8, 62.2, 59.1, 54.4, 59.1, 17.2, 56.6, 70.7, 111.2, 107.1, 74.2, 53.8, 53.1, 55.3, 50.1,
        47.3, 53.1, 19.5, 47.8, 72.6, 99.6, 95.5, 64.2, 48.6, 50.3, 57.6, 46.1, 43.2, 52.8, 13.9, 48.2, 71.1, 91.9, 89.3, 73, 37.1, 42.9, 51.1, 39.8, 37,
        49.1, 17.3, 45.2, 52.5, 72.6, 73.5, 61.3};

    @Test
    public void testSeasonalBreaks() {
        TsData y = TsData.ofInternal(TsPeriod.monthly(1967, 1), sugar);
        double[] rslt = StsOutliersDetection.seasonalBreaks(y.log(), 1, 1, 1, "HarrisonStevens", null);
        System.out.println(DoubleSeq.of(rslt));
    }

}
