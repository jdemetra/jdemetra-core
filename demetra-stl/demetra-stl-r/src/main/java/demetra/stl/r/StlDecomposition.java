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
import demetra.math.matrices.Matrix;
import demetra.stl.IStlSpec;
import demetra.stl.MStlSpec;
import demetra.stl.SeasonalSpec;
import jdplus.math.matrices.FastMatrix;
import jdplus.stl.IStlKernel;
import jdplus.stl.MStlKernel;
import jdplus.stl.MStlResults;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class StlDecomposition {

    public Matrix stl(double[] data, int period, boolean mul, int swindow, int twindow, int nin, int nout) {
        if (nin <1)
            nin=1;
        if (nout<0)
            nout=0;
        if (swindow == 0) {
            swindow = 7;
        }
        if (twindow == 0) {
            twindow = LoessSpec.defaultTrendWindow(period, swindow);
        }
        StlSpec spec = StlSpec.builder()
                .innerLoopsCount(nin)
                .outerLoopsCount(nout)
                .multiplicative(mul)
                .trendSpec(LoessSpec.of(twindow, 1))
                .seasonalSpec(new SeasonalSpec(period, swindow))
                .build();
        StlKernel stl = new StlKernel(spec);
        DoubleSeq y = DoubleSeq.of(data).cleanExtremities();

        int n = y.length();
        stl.process(y);

        FastMatrix M = FastMatrix.make(n, 7);

        M.column(0).copyFrom(stl.getY(), 0);
        M.column(2).copyFrom(stl.getTrend(), 0);
        M.column(3).copyFrom(stl.getSeas(), 0);
        M.column(4).copyFrom(stl.getIrr(), 0);
        M.column(5).copyFrom(stl.getFit(), 0);
        M.column(6).copyFrom(stl.getWeights(), 0);
        M.column(1).copy(M.column(0));
        if (mul) {
            M.column(1).div(M.column(3));
        } else {
            M.column(1).sub(M.column(3));
        }
        return M;
    }

    private int max(int[] v) {
        int m = v[0];
        for (int i = 1; i < v.length; ++i) {
            if (v[i] > m) {
                m = v[i];
            }
        }
        return m;
    }

    public Matrix mstl(double[] data, int[] periods, boolean mul, int[] swindow, int twindow, int nin, int nout) {
        if (periods == null || (swindow != null && periods.length != swindow.length)) {
            return null;
        }
        if (twindow == 0) {
            twindow = LoessSpec.defaultTrendWindow(max(periods));
        }

        MStlSpec.Builder builder = MStlSpec.builder()
                .innerLoopsCount(nin)
                .outerLoopsCount(nout)
                .multiplicative(mul)
                .trendSpec(LoessSpec.of(twindow, 1));

        if (swindow == null) {
            for (int i = 0; i < periods.length; ++i) {
                builder.seasonalSpec(SeasonalSpec.createDefault(periods[i]));
            }
        } else if (swindow.length == 1) {
            for (int i = 0; i < periods.length; ++i) {
                builder.seasonalSpec(new SeasonalSpec(periods[i], swindow[0]));
            }
        } else {
            for (int i = 0; i < periods.length; ++i) {
                builder.seasonalSpec(new SeasonalSpec(periods[i], swindow[i]));
            }

        }
        MStlSpec spec = builder.build();

        MStlKernel stl = new MStlKernel(spec);
        DoubleSeq y = DoubleSeq.of(data).cleanExtremities();
        stl.process(y);

        int n = y.length();
        FastMatrix M = FastMatrix.make(n, 6 + periods.length);

        M.column(0).copyFrom(stl.getY(), 0);
        M.column(1).copy(M.column(0));
        M.column(2).copyFrom(stl.getTrend(), 0);
        int j = 3;
        for (int i = 0; i < periods.length; ++i, ++j) {
            M.column(j).copyFrom(stl.getSeason(i), 0);
            if (mul) {
                M.column(1).div(M.column(j));
            } else {
                M.column(1).sub(M.column(j));
            }
        }
        M.column(j++).copyFrom(stl.getIrr(), 0);
        M.column(j++).copyFrom(stl.getFit(), 0);
        M.column(j).copyFrom(stl.getWeights(), 0);
        return M;
    }

    public Matrix istl(double[] data, int[] periods, boolean mul, int[] swindow, int[] twindow, int nin, int nout) {
        if (periods == null || (swindow != null && periods.length != swindow.length)) {
            return null;
        }
        if (twindow != null && twindow.length!= periods.length) {
            return null;
        }

        IStlSpec.Builder builder = IStlSpec.builder()
                .innerLoopsCount(nin)
                .outerLoopsCount(nout)
                .multiplicative(mul);

            for (int i = 0; i < periods.length; ++i) {
                SeasonalSpec sspec;
                if (swindow == null)
                    sspec=SeasonalSpec.createDefault(periods[i]);
                else if (swindow.length == 1)
                    sspec=new SeasonalSpec(periods[i], swindow[0]);
                else
                    sspec=new SeasonalSpec(periods[i], swindow[i]);
               LoessSpec tspec;
                if (twindow == null)
                    tspec=LoessSpec.defaultTrend(periods[i]);
                else 
                    tspec=LoessSpec.of(twindow[i], 1);
                builder.periodSpec(new IStlSpec.PeriodSpec(tspec, sspec));
           

        }
        IStlSpec spec = builder.build();

        DoubleSeq y = DoubleSeq.of(data).cleanExtremities();
        MStlResults rslt = IStlKernel.process(y, spec);

        int n = y.length();
        FastMatrix M = FastMatrix.make(n, 6 + periods.length);

        M.column(0).copy(y);
        M.column(1).copy(M.column(0));
        M.column(2).copy(rslt.getTrend());
        int j = 2;
        for (DoubleSeq seas : rslt.getSeasons()) {
            M.column(++j).copy(seas);
            if (mul) {
                M.column(1).div(M.column(j));
            } else {
                M.column(1).sub(M.column(j));
            }
        }
        M.column(j++).copy(rslt.getIrregular());
        M.column(j++).copy(rslt.getFit());
        M.column(j).copy(rslt.getWeights());
        return M;
    }

    public double[] loess(double[] y, int window, int degree, int jump) {
        LoessSpec spec = LoessSpec.of(window, degree, jump, null);
        LoessFilter filter = new LoessFilter(spec);
        double[] z = new double[y.length];
        filter.filter(IDataGetter.of(y), null, IDataSelector.of(z));
        return z;
    }

}
