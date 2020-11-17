/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11;

import demetra.data.DoubleSeq;
import ec.satoolkit.x11.DefaultX11Utilities;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class X11ContextTest {

    public static double DELTA = 1e-14;

    @Test
    public void MakePositivityTest_pos() {
        double[] data = {0, 2, 3, 3, 3, 3, 3, 5, 58, 8, 8, 8, 8, 8, 74, 5, 98, 5, 85, 8, 8, 8, 8, 8, 85, 5, 5, 5, 5, 5, 5, 5, 5, 65, 5, 5, 55, 5};
        makePos("Error for positive Ts", data);
    }

    @Test
    public void MakePositivityTest_neg_1() {
        double[] data = {0, 2, 3, 3, 3, -3, -3, -10000, 58, 8, 8, 8, 8, 8, 74, 5, 98, 5, 85, 8, 8, 8, 8, 8, 85, 5, 5, 5, 5, 5, 5, 5, 5, 65, 5, 5, 55, 5};
        makePos("Error for neg Ts_1", data);
    }

    @Test
    public void MakePositivityTest_neg_2() {
        double[] data = {0, 2, 3, 3, 3, -3, Double.NaN, -10000, 58, 8, 8, 8, 8, 8, 74, 5, 98, 5, 85, 8, 8, 8, 8, 8, 85, 5, 5, 5, 5, 5, 5, 5, 5, 65, 5, 5, 55, 5};
        makePos("Error for neg Ts_2", data);

    }

    @Test
    public void MakePositivityTest_neg_3() {
        double[] data = {Double.NaN, Double.NaN, Double.NaN, 3, 3, -3, -3, -10000, 58, 8, 8, 8, 8, 8, 74, 5, 98, 5, 85, 8, 8, 8, 8, 8, 85, 5, 5, 5, 5, 5, 5, 5, 5, 65, 5, 5, 55, 5};
        makePos("Error for NA Ts_3", data);
    }

    @Test
    public void MakePositivityTest_NA() {
        double[] data = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        makePos("Error for NA Ts_Na", data);
    }

    private void makePos(String messages, double[] data) {
        DoubleSeq dsResults = X11Context.makePositivity(DoubleSeq.of(data));
        ec.satoolkit.x11.IX11Utilities x11Utilities = new DefaultX11Utilities();
        ec.tstoolkit.timeseries.simplets.TsData oldTs = new ec.tstoolkit.timeseries.simplets.TsData(TsFrequency.Monthly, 1900, 0, data, true);
        x11Utilities.checkPositivity(oldTs);
        Assert.assertArrayEquals(messages, oldTs.internalStorage(), dsResults.toArray(), DELTA);

    }

}
