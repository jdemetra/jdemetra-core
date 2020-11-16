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
package jdplus.arima.ssf;

import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlock;
import nbbrd.design.Development;
import jdplus.math.linearfilters.BackFilter;
import jdplus.ssf.ckms.CkmsFilter;
import jdplus.ssf.univariate.PredictionErrorDecomposition;
import jdplus.ssf.univariate.SsfData;
import jdplus.ssf.univariate.Ssf;
import jdplus.arima.estimation.ArimaForecasts;
import demetra.data.DoubleSeq;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ExactArimaForecasts implements ArimaForecasts {

    private IArimaModel arima;
    private Ssf ssf;
    private BackFilter bar;
    private double mean;
    private boolean bmean;

    @Override
    /**
     * Returns the mean. The mean correction is defined by 
     * phi(B)y=mean+theta(B)e
     * So, it is not equal to the mean correction computed by regression (for 
     * instance in TRAMO)(phi(B)(y-meanc*C)=theta(B)e).
     * mean = phi(1)*meanc (we omit the non stationary polynomial)
     */
    public double getMean() {
        return mean;
    }

    /**
     *
     */
    public ExactArimaForecasts() {
    }

    @Override
    public boolean prepare(final IArimaModel model, boolean bmean) {
        this.bmean = bmean;
        IArimaModel cmodel = model;
        if (bmean) {
            BackFilter ar = model.getStationaryAr(), ur = model
                    .getNonStationaryAr(), ma = model.getMa();
            bar = ar.times(ur);
            double var = model.getInnovationVariance();
            BackFilter D = BackFilter.D1;
            cmodel = new ArimaModel(ar, ur.times(D), ma.times(D), var);
        } else {
            bar = model.getAr();
        }
        arima = cmodel;
        ssf = Ssf.of(SsfArima.of(cmodel), SsfArima.defaultLoading());
        return true;
    }

    private double[] fcasts(SsfData sd, int nf) {
        CkmsFilter filter = new CkmsFilter(SsfArima.fastInitializer(arima));
        PredictionErrorDecomposition perr = new PredictionErrorDecomposition(false);
        filter.process(ssf, sd, perr);
        // the first forecasts are produced by the state vector...
        double[] f = new double[nf];
        if (nf >= ssf.getStateDim()) {
            DataBlock a = filter.getFinalState().a();
            a.copyTo(f, 0);
            // complete the forecasts (just run the filter)...
            int last = a.length() - 1;
            for (int i = ssf.getStateDim(); i < nf; ++i) {
                ssf.dynamics().TX(0, a);
                f[i] = a.get(last);
                }
        } else {
            filter.getFinalState().a().range(0, nf).copyTo(f, 0);
        }
        if (bmean) {
            DataBlock a = filter.getFinalState().a();
            DataBlock s = DataBlock.make(a.length() - bar.getDegree());
            bar.apply(a, s);
            mean = s.get(s.length() - 1);
        }
        return f;
    }

    /**
     *
     * @param data
     * @param nf
     * @return
     */
    @Override
    public DoubleSeq forecasts(DoubleSeq data, int nf) {
        SsfData sd = new SsfData(data);
        return DoubleSeq.of(fcasts(sd, nf));
    }

    @Override
    public boolean prepare(IArimaModel model, double mean) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
