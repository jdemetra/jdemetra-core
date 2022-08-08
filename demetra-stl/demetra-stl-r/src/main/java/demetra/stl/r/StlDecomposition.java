/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stl.r;

import jdplus.stl.IDataGetter;
import jdplus.stl.IDataSelector;
import jdplus.stl.LoessFilter;
import demetra.stl.LoessSpec;
import jdplus.stl.StlKernel;
import demetra.stl.StlSpec;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.math.matrices.Matrix;
import demetra.stl.SeasonalSpec;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class StlDecomposition {

    public Matrix process(double[] data, int period, boolean mul, int swindow, int twindow, boolean robust) {
        StlSpec spec = (robust ? StlSpec.robustBuilder() : StlSpec.builder())
                .multiplicative(mul)
                .trendSpec(LoessSpec.defaultTrend(period, swindow))
                .seasonalSpec(new SeasonalSpec(period, swindow))
                .build();
        StlKernel stl = new StlKernel(spec);
        DoubleSeq y = DoubleSeq.of(data).cleanExtremities();

        int n = y.length();
        stl.process(y);

        double[] all = new double[n * 5];

        DoubleSeq t = DoubleSeq.of(stl.getTrend());
        DoubleSeq s = DoubleSeq.of(stl.getSeas());
        DoubleSeq i = DoubleSeq.of(stl.getIrr());
        DoubleSeq sa = mul ? DoublesMath.divide(y, s) : DoublesMath.subtract(y, s);

        y.copyTo(all, 0);
        sa.copyTo(all, n);
        t.copyTo(all, 2 * n);
        s.copyTo(all, 3 * n);
        i.copyTo(all, 4 * n);

        return Matrix.of(all, n, 5);
    }

    public double[] loess(double[] y, int window, int degree, int jump) {
        LoessSpec spec = LoessSpec.of(window, degree, jump, null);
        LoessFilter filter = new LoessFilter(spec);
        double[] z = new double[y.length];
        filter.filter(IDataGetter.of(y), null, IDataSelector.of(z));
        return z;
    }

}
