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
package jdplus.regarima;

import demetra.data.DoubleSeq;
import java.util.List;
import jdplus.arima.IArimaModel;
import jdplus.arima.ssf.SsfArima;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.dk.DkFilter;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaForecasts {

    @lombok.Value
    public static class Result {

        double[] forecasts, forecastsStdev;
    }

    public <M extends IArimaModel> Result calcForecast(final RegArimaModel<M> regarima, final ConcentratedLikelihoodWithMissing cl, final int nf, boolean unbiased, int nhp) {
        // use dummy matrix
        return calcForecast(regarima, cl, Matrix.make(nf, 1), unbiased, nhp);
    }

    public <M extends IArimaModel> Result calcForecast(final RegArimaModel<M> regarima, final ConcentratedLikelihoodWithMissing cl, final Matrix Xf, boolean unbiased, int nhp) {
        DoubleSeq y = regarima.getY();
        int nf = Xf.getRowsCount(), n = y.length();
        int nall = n + nf;
        double[] yall = new double[nall];
        y.copyTo(yall, 0);
        for (int i = n; i < nall; ++i) {
            yall[i] = Double.NaN;
        }
        // reset missing values, if any
        int[] missings = regarima.missing();
        if (missings != null) {
            for (int i = 0; i < missings.length; ++i) {
                yall[missings[i]] = Double.NaN;
            }
        }

        Ssf ssf = SsfArima.ssf(regarima.arima());
        DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.full();
        fr.prepare(ssf, 0, nall);
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(fr);
        OrdinaryFilter of = new OrdinaryFilter(initializer);
        of.process(ssf, new SsfData(yall), fr);
        ResultsRange range = new ResultsRange(0, nall);
        DkFilter filter = new DkFilter(ssf, fr, range, false);

        int nx = regarima.getVariablesCount();

        // get forecasts of the series
        double[] f = new double[nf];
        double[] vf = new double[nf];

        fr.getComponent(0).drop(n, 0).copyTo(f, 0);
        fr.getComponentVariance(0).drop(n, 0).copyTo(vf, 0);

        int ndf = unbiased ? cl.dim() - cl.nx() - nhp : cl.dim();
        double sig2 = cl.ssq() / ndf;
        for (int i = 0; i < nf; ++i) {
            vf[i] *= sig2;
        }
//        // compute X-LX
        if (nx > 0) {
            DoubleSeq b = cl.coefficients();
            Matrix v = cl.covariance(nhp, unbiased);

            Matrix xall = Matrix.make(nall, nx);
            int j = 0;
            if (regarima.isMean()) {
                double[] xm = RegArimaUtility.meanRegressionVariable(regarima.arima().getNonStationaryAr(), nall);
                xall.column(j++).copyFrom(xm, 0);
            }
            List<DoubleSeq> x = regarima.getX();
            if (!x.isEmpty()) {
                xall.extract(n, nf, j, nx - j).copy(Xf);

                for (DoubleSeq xcur : x) {
                    xall.column(j++).range(0, n).copy(xcur);
                }
            }

            filter.filter(xall);
            Matrix dx = xall.extract(n, nf, 0, nx);

            DataBlockIterator xrows = dx.rowsIterator();
            j = 0;
            while (xrows.hasNext()) {
                DataBlock xrow = xrows.next();
                f[j] += xrow.dot(b);
                vf[j] += QuadraticForm.apply(v, xrow);
                j++;
            }
        }
        for (int i = 0; i < nf; ++i) {
            vf[i] = Math.sqrt(vf[i]);
        }
        return new Result(f, vf);
    }

}
