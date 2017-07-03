/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package demetra.arima.estimation;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.ssf.DiffuseFilteringResults;
import ec.tstoolkit.ssf.DiffuseVarianceFilter;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.FilteredData;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfArima.Initializer;
import java.util.List;

/**
 * Forecasts using a ssf implementation. Seems to suffer of stability issues
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SsfArimaForecasts {

    public <M extends IArimaModel> Forecasts calcForecast(final RegArimaEstimation<M> regarima,
            final List<double[]> x, final int fHorizon,
            final boolean unbiased, final int nhp) {
        if (fHorizon <= 0) {
            return null;
        }
        DataBlock y = regarima.model.getY();
        IArimaModel arima = regarima.model.getArima();
        int nx = x == null ? 0 : x.size();
        if (regarima.model.isMeanCorrection()) {
            ++nx;
        }
        Forecasts fcasts = new Forecasts(fHorizon);

        int n = regarima.model.getObsCount();
        SsfArima ssf = new SsfArima(arima);
        Filter<SsfArima> filter = new Filter<>();
        Initializer initializer = new Initializer();
        // filter.Initializer = initializer;
        filter.setSsf(ssf);

        DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
        frslts.getFilteredData().setSavingA(true);
        // filter y, extended with nf missing values
        double[] yc = new double[n + fHorizon];
        y.copyTo(yc, 0);
        for (int i = n; i < yc.length; ++i) {
            yc[i] = Double.NaN;
        }
        // reset missing values, if any
        int[] missings = regarima.model.getMissings();
        if (missings != null) {
            for (int i = 0; i < missings.length; ++i) {
                yc[missings[i]] = Double.NaN;
            }
        }

        SsfData ssfy = new SsfData(yc, null);
        filter.process(ssfy, frslts);

        DiffuseVarianceFilter vfilter = frslts.getVarianceFilter();
        FilteredData fdata = frslts.getFilteredData();


        double mvar = regarima.likelihood.getSsqErr() / regarima.likelihood.getDegreesOfFreedom(unbiased, nhp);
        for (int i = 0; i < fHorizon; ++i) {
            fcasts.f_[i] = fdata.A(n + i).get(0);
            fcasts.ef_[i] = vfilter.F(n + i) * mvar;
        }

        // compute X-LX
        if (nx > 0) {
            int pos = ssf.getNonStationaryDim();
            double[] a0 = new double[ssf.getStateDim()];
            // search the indexes
            int icur = 0, mcur = 0;
            int[] idx = new int[nx];
            if (regarima.model.isMeanCorrection()) {
                idx[icur++] = mcur++;
            }
            mcur += regarima.model.getMissingsCount();
            for (int i = 0; i < regarima.model.getXCount(); ++i) {
                idx[icur++] = mcur++;
            }

            double[] c = new double[nx];
            Matrix v = new Matrix(nx, nx);

            double[] b = regarima.likelihood.getB();
            Matrix V = regarima.likelihood.getBVar(unbiased, nhp);
            for (int i = 0; i < nx; ++i) {
                c[i] = b[idx[i]];
                for (int j = 0; j < nx; ++j) {
                    v.set(i, j, V.get(idx[i], idx[j]));
                }
            }

            Matrix xe = new Matrix(fHorizon, nx);

            for (int i = 0, ix = 0; i < nx; ++i) {
                double[] xcur = null;
                if (i == 0 && regarima.model.isMeanCorrection()) {
                    xcur = regarima.model.calcMeanReg(n + fHorizon);
                }
                else {
                    regarima.model.X(ix).copyTo(yc, 0);
                    xcur = yc;
                }

                SsfData ssfx = new SsfData(xcur, null);
                initializer.calcInitialState(ssf, new DataBlock(a0), ssfx);
                vfilter.process(fdata, pos, xcur, a0);
                for (int j = 0; j < fHorizon; ++j) {
                    xe.set(j, i, fdata.E(n + j));
                }
            }

            DataBlock C = new DataBlock(c);
            DataBlockIterator xrows = xe.rows();
            DataBlock xrow = xrows.getData();
            do {
                fcasts.f_[xrows.getPosition()] += xrow.dot(C);
                fcasts.ef_[xrows.getPosition()] += SymmetricMatrix.quadraticForm(v, xrow);
            }
            while (xrows.next());
        }

        for (int i = 0; i < fHorizon; ++i) {
            fcasts.ef_[i] = Math.sqrt(fcasts.ef_[i]);
        }
        return fcasts;
    }
}
