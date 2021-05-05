/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts.r;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.LengthOfPeriod;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.MatrixWindow;
import jdplus.modelling.regression.Regression;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.implementations.RegSsf;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BsmData;
import jdplus.sts.SsfBsm;
import jdplus.sts.internal.BsmKernel;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Bsm {

    public MatrixType forecast(TsData series, String model, int nf) {
        double[] y = extend(series, nf);
        Matrix X = variables(model, series.getDomain().extend(0, nf));

        // estimate the model
        BsmEstimationSpec espec = BsmEstimationSpec.builder()
                .diffuseRegression(true)
                .build();

        BsmKernel kernel = new BsmKernel(espec);
        boolean ok = kernel.process(series.getValues(), X == null ? null : X.extract(0, series.length(), 0, X.getColumnsCount()), series.getAnnualFrequency(), BsmSpec.DEFAULT);
        BsmData result = kernel.getResult();
        // create the final ssf
        SsfBsm bsm = SsfBsm.of(result);
        Ssf ssf;
        if (X == null) {
            ssf = bsm;
        } else {
            ssf = RegSsf.ssf(bsm, X);
        }
        DefaultDiffuseSquareRootFilteringResults frslts = DkToolkit.sqrtFilter(ssf, new SsfData(y), true);
        double[] fcasts = new double[nf * 2];

        ISsfLoading loading = ssf.measurement().loading();
        for (int i = 0, j = series.length(); i < nf; ++i, ++j) {
            fcasts[i] = loading.ZX(j, frslts.a(j));
            double v = loading.ZVZ(j, frslts.P(j));
            fcasts[nf + i] = v <= 0 ? 0 : Math.sqrt(v);
        }
        return MatrixType.of(fcasts, nf, 2);
    }

    private double[] extend(TsData series, int nf) {
        int n = series.length();
        double[] y = new double[n + nf];
        series.getValues().copyTo(y, 0);
        for (int i = 0; i < nf; ++i) {
            y[n + i] = Double.NaN;
        }
        return y;
    }

    private ITsVariable[] variables(String model) {
        switch (model) {
            case "td2":
            case "TD2":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD2)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };
            case "td3":
            case "TD3":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };
            case "td7":
            case "TD7":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD7)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear)
                };

            case "full":
            case "Full":
                return new ITsVariable[]{
                    new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3)),
                    new LengthOfPeriod(LengthOfPeriodType.LeapYear),
                    EasterVariable.builder()
                    .duration(6)
                    .endPosition(-1)
                    .meanCorrection(EasterVariable.Correction.Theoretical)
                    .build()
                };

            default:
                return null;
        }
    }

    private Matrix variables(String model, TsDomain domain) {
        ITsVariable[] variables = variables(model);
        if (variables == null) {
            return null;
        }
        return Regression.matrix(domain, variables);
    }
}
