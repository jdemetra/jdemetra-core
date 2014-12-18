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

package ec.tstoolkit.arima.estimation;

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
import ec.tstoolkit.ssf.FilteringResults;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.VarianceFilter;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ssf.arima.SsfArima.Initializer;
import java.util.List;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Forecasts {

    double[] ef_, f_;
    int nf_ = 0;

    public int getForecastsCount() {
        return nf_;
    }
    
    public double[] getForecasts(){
        return f_;
    }

    public double forecast(final int idx) {
        return f_[idx];
    }

    public double forecastStdev(final int idx) {
        return ef_[idx];
    }

     public double[] getForecastStdevs(){
        return ef_;
    }

     Forecasts(int nf) {
        nf_ = nf;
        f_ = new double[nf];
        ef_ = new double[nf];
    }

    public Forecasts(){}

    /**
     * Computes the forecasts of a RegArima model, using a Kalman filter.
     * The diffuse initialiser of Koopman is used for the non stationary part.
     * @param <M> The type of the Arima model
     * @param gls The gls model
     * @param x the extended values of the deterministic components (without mean)
     * @param fHorizon The length of the forecasts
     * @param nhp The number of hyper parameters count. This parameter is used in the
     * computation of the standard error.
     */
    public <M extends IArimaModel> void calcForecast(final RegArimaEstimation<M> gls,
            final List<DataBlock> x, final int fHorizon, final int nhp) {
        DataBlock y = gls.model.getY();
        IArimaModel arima = gls.model.getArima();
        int nx = x == null ? 0 : x.size();
        if (gls.model.isMeanCorrection()) {
            ++nx;
        }

        nf_ = fHorizon;
        if (nf_ <= 0) {
            return;
        }
        ef_ = new double[nf_];
        f_ = new double[nf_];

        int n = gls.model.getObsCount();
        SsfArima ssf = new SsfArima(arima);
        Filter<SsfArima> filter = new Filter<>();
        Initializer initializer = new Initializer();
        DiffuseFilteringResults frslts = new DiffuseFilteringResults(true);
        filter.setSsf(ssf);
        //ec.tstoolkit.ssf.arima.SsfArima.Initializer initializer=
        //        new ec.tstoolkit.ssf.arima.SsfArima.Initializer();
        filter.setInitializer(initializer);

        //FilteringResults frslts = new FilteringResults(true);
        frslts.getFilteredData().setSavingA(true);
        // filter y, extended with nf missing values
        double[] yc = new double[n + nf_];
        y.copyTo(yc, 0);
        for (int i = n; i < yc.length; ++i) {
            yc[i] = Double.NaN;
        }
        // reset missing values, if any
        int[] missings = gls.model.getMissings();
        if (missings != null) {
            for (int i = 0; i < gls.model.getMissingsCount(); ++i) {
                yc[missings[i]] = Double.NaN;
            }
        }

        SsfData ssfy = new SsfData(yc, null);
        filter.process(ssfy, frslts);

        DiffuseVarianceFilter vfilter = frslts.getVarianceFilter();
//        VarianceFilter vfilter = frslts.getVarianceFilter();
        FilteredData fdata = frslts.getFilteredData();

        double mvar = gls.likelihood.getSsqErr()
                / gls.likelihood.getDegreesOfFreedom(true, nhp);

        for (int i = 0; i < nf_; ++i) {
            f_[i] = fdata.A(n + i).get(0);
            ef_[i] = vfilter.F(n + i) * mvar;
        }

        // compute X-LX
        if (nx > 0) {
            int pos = ssf.getNonStationaryDim();
            double[] a0 = new double[ssf.getStateDim()];
            // search the indexes
            int icur = 0, mcur = 0;
            int[] idx = new int[nx];
            if (gls.model.isMeanCorrection()) {
                idx[icur++] = mcur++;
            }
            mcur += gls.model.getMissingsCount();
            for (int i = 0; i < gls.model.getXCount(); ++i) {
                idx[icur++] = mcur++;
            }

            double[] c = new double[nx];
            Matrix v = new Matrix(nx, nx);

            double[] b = gls.likelihood.getB();
            Matrix V = gls.likelihood.getBVar(true, nhp);
            for (int i = 0; i < nx; ++i) {
                c[i] = b[idx[i]];
                for (int j = 0; j < nx; ++j) {
                    v.set(i, j, V.get(idx[i], idx[j]));
                }
            }

            Matrix xe = new Matrix(nf_, nx);

            for (int i = 0, ix = 0; i < nx; ++i) {
                double[] xcur = null;
                if (i == 0 && gls.model.isMeanCorrection()) {
                    xcur = gls.model.calcMeanReg(n + nf_);
                } else {
                    gls.model.X(ix).copyTo(yc, 0);
                    x.get(ix++).copyTo(yc, n);
                    xcur = yc;
                }

                SsfData ssfx = new SsfData(xcur, null);
                initializer.calcInitialState(ssf, new DataBlock(a0), ssfx);
                vfilter.process(fdata, pos, xcur, a0);
                for (int j = 0; j < nf_; ++j) {
                    xe.set(j, i, fdata.E(n + j));
                }
            }

            DataBlock C = new DataBlock(c);
            DataBlockIterator xrows = xe.rows();
            DataBlock xrow = xrows.getData();
            do {
                f_[xrows.getPosition()] += xrow.dot(C);
                ef_[xrows.getPosition()] += SymmetricMatrix.quadraticForm(v,
                        xrow);
            } while (xrows.next());
        }

        for (int i = 0; i < ef_.length; ++i) {
            ef_[i] = Math.sqrt(ef_[i]);
        }
    }
   
    public <M extends IArimaModel> void calcForecast2(final RegArimaEstimation<M> gls,
            final List<DataBlock> x, final int fHorizon, final int nhp) {
        DataBlock y = gls.model.getY();
        IArimaModel arima = gls.model.getArima();
        int nx = x == null ? 0 : x.size();
        if (gls.model.isMeanCorrection()) {
            ++nx;
        }

        nf_ = fHorizon;
        if (nf_ <= 0) {
            return;
        }
        ef_ = new double[nf_];
        f_ = new double[nf_];

        int n = gls.model.getObsCount();
        SsfArima ssf = new SsfArima(arima);
        Filter<SsfArima> filter = new Filter<>();
        filter.setSsf(ssf);
        ec.tstoolkit.ssf.arima.SsfArima.Initializer initializer=
                new ec.tstoolkit.ssf.arima.SsfArima.Initializer();
        filter.setInitializer(initializer);

        FilteringResults frslts = new FilteringResults(true);
        frslts.getFilteredData().setSavingA(true);
        // filter y, extended with nf missing values
        double[] yc = new double[n + nf_];
        y.copyTo(yc, 0);
        for (int i = n; i < yc.length; ++i) {
            yc[i] = Double.NaN;
        }
        // reset missing values, if any
        int[] missings = gls.model.getMissings();
        if (missings != null) {
            for (int i = 0; i < gls.model.getMissingsCount(); ++i) {
                yc[missings[i]] = Double.NaN;
            }
        }

        SsfData ssfy = new SsfData(yc, null);
        filter.process(ssfy, frslts);

        VarianceFilter vfilter = frslts.getVarianceFilter();
        FilteredData fdata = frslts.getFilteredData();

        double mvar = gls.likelihood.getSsqErr()
                / gls.likelihood.getDegreesOfFreedom(true, nhp);

        for (int i = 0; i < nf_; ++i) {
            f_[i] = fdata.A(n + i).get(0);
            ef_[i] = vfilter.F(n + i) * mvar;
        }

        // compute X-LX
        if (nx > 0) {
            int pos = ssf.getNonStationaryDim();
            double[] a0 = new double[ssf.getStateDim()];
            // search the indexes
            int icur = 0, mcur = 0;
            int[] idx = new int[nx];
            if (gls.model.isMeanCorrection()) {
                idx[icur++] = mcur++;
            }
            mcur += gls.model.getMissingsCount();
            for (int i = 0; i < gls.model.getXCount(); ++i) {
                idx[icur++] = mcur++;
            }

            double[] c = new double[nx];
            Matrix v = new Matrix(nx, nx);

            double[] b = gls.likelihood.getB();
            Matrix V = gls.likelihood.getBVar(true, nhp);
            for (int i = 0; i < nx; ++i) {
                c[i] = b[idx[i]];
                for (int j = 0; j < nx; ++j) {
                    v.set(i, j, V.get(idx[i], idx[j]));
                }
            }

            Matrix xe = new Matrix(nf_, nx);

            for (int i = 0, ix = 0; i < nx; ++i) {
                double[] xcur = null;
                if (i == 0 && gls.model.isMeanCorrection()) {
                    xcur = gls.model.calcMeanReg(n + nf_);
                } else {
                    gls.model.X(ix).copyTo(yc, 0);
                    x.get(ix++).copyTo(yc, n);
                    xcur = yc;
                }

                SsfData ssfx = new SsfData(xcur, null);
                initializer.calcInitialState(ssf, new DataBlock(a0), ssfx);
                vfilter.process(fdata, pos, xcur, a0);
                for (int j = 0; j < nf_; ++j) {
                    xe.set(j, i, fdata.E(n + j));
                }
            }

            DataBlock C = new DataBlock(c);
            DataBlockIterator xrows = xe.rows();
            DataBlock xrow = xrows.getData();
            do {
                f_[xrows.getPosition()] += xrow.dot(C);
                ef_[xrows.getPosition()] += SymmetricMatrix.quadraticForm(v,
                        xrow);
            } while (xrows.next());
        }

        for (int i = 0; i < ef_.length; ++i) {
            ef_[i] = Math.sqrt(ef_[i]);
        }
    }
}
