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
import jdplus.arima.IArimaModel;
import jdplus.ssf.arima.SsfArima;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.dk.FastDkFilter;
import jdplus.ssf.dk.sqrt.DefaultDiffuseSquareRootFilteringResults;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.SsfData;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.math.matrices.Matrix;

/**
 * f = X'b + L(y-X'b) = Ly + (X - LX)'b
 * var(f) = var (Ly)+ (X - LX)' var(b)(X - LX)
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaForecasts {

    @lombok.Value
    public static class Result {

        double[] forecasts, forecastsStdev;
    }

    /**
     *
     * @param <M>
     * @param arima
     * @param y
     * @param X X should contain the regression variables extended with
     * forecasts
     * @param b
     * @param varB
     * @param sig2
     * @return
     */
    public <M extends IArimaModel> Result calcForecast(final @NonNull IArimaModel arima, final @NonNull DoubleSeq y, final @NonNull Matrix X, final @NonNull DoubleSeq b, final @NonNull Matrix varB, final double sig2) {
        return calcForecast(arima, y, X.getRowsCount() - y.length(), X, b, varB, sig2);
    }

    /**
     *
     * @param <M>
     * @param arima
     * @param y
     * @param nf
     * @param sig2
     * @return
     */
    public <M extends IArimaModel> Result calcForecast(final @NonNull IArimaModel arima, final @NonNull DoubleSeq y, final int nf, final double sig2) {
        return calcForecast(arima, y, nf, null, null, null, sig2);
    }

    private <M extends IArimaModel> Result calcForecast(final IArimaModel arima, final DoubleSeq y, final int nf, final Matrix X, final DoubleSeq b, final Matrix varb, final double sig2) {

        int n = y.length(), nall = n + nf;
        double[] yall = new double[nall];
        y.copyTo(yall, 0);
        for (int i = n; i < nall; ++i) {
            yall[i] = Double.NaN;
        }

        Ssf ssf = SsfArima.ssf(arima);
        DefaultDiffuseSquareRootFilteringResults fr = DefaultDiffuseSquareRootFilteringResults.full();
        fr.prepare(ssf, 0, nall);
        DiffuseSquareRootInitializer initializer = new DiffuseSquareRootInitializer(fr);
        OrdinaryFilter of = new OrdinaryFilter(initializer);
        of.process(ssf, new SsfData(yall), fr);
        FastDkFilter filter = new FastDkFilter(ssf, fr, false);

        int nx = X == null ? 0 : X.getColumnsCount();

        // get forecasts of the series
        double[] f = new double[nf];
        double[] vf = new double[nf];

        fr.getComponent(0).drop(n, 0).copyTo(f, 0);
        fr.getComponentVariance(0).drop(n, 0).copyTo(vf, 0);

        for (int i = 0; i < nf; ++i) {
            vf[i] *= sig2;
        }
        if (nx > 0) {
            // compute DX = X-LX 
            FastMatrix xall = FastMatrix.of(X);
             filter.filter(xall);
             FastMatrix dx = xall.extract(n, nf, 0, nx);
//          dx.sub(xall.extract(n, nf, 0, nx));
            // F += DX * b, varF += DX*varB*DX'
            DataBlockIterator xrows = dx.rowsIterator();
            int j = 0;
            FastMatrix V = FastMatrix.of(varb);
            while (xrows.hasNext()) {
                DataBlock xrow = xrows.next();
                f[j] += xrow.dot(b);
                vf[j] += QuadraticForm.apply(V, xrow);
                j++;
            }
        }
        for (int i = 0; i < nf; ++i) {
            vf[i] = Math.sqrt(vf[i]);
        }
        return new Result(f, vf);
    }

}
