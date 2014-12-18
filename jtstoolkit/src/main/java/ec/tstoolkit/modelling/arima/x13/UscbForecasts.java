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

package ec.tstoolkit.modelling.arima.x13;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.MaLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.polynomials.Polynomial;

/**
 * Computes the forecasts of an Arima model using the approach followed in X12/X13.
 * 
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class UscbForecasts {

    private final IArimaModel arima;
    private final Polynomial ar, ma;
    private final double mean;

    /**
     * 
     * @param model
     */
    public UscbForecasts(IArimaModel model) {
        arima = model;
        ar = arima.getAR().getPolynomial();
        ma = arima.getMA().getPolynomial();
        mean = 0;
    }

    public UscbForecasts(IArimaModel model, double mu) {
        arima = model;
        ar = arima.getAR().getPolynomial();
        ma = arima.getMA().getPolynomial();
        //mean=mu;
        if (model.getStationaryARCount() > 0) {
            Polynomial c = model.getStationaryAR().getPolynomial();
            double s = 0;
            for (int i = 0; i <= c.getDegree(); ++i) {
                s += c.get(i);
            }
            mean = mu * s;
        }
        else {
            mean = mu;
        }
    }

    /**
     * 
     * @param data
     * @param nf
     * @return
     */
    public double[] forecasts(IReadDataBlock data, int nf) {
        try {
            DataBlock res = residuals(data);
            // residuals i correspond to t=i+p-q
            double[] fcasts = new double[nf];
            int p = ar.getDegree();
            double[] y = new double[p];
            // copy the last obs, in reverse order.
            int last = data.getLength() - 1;
            for (int i = 0; i < p; ++i) {
                y[i] = data.get(last - i);
            }
            // copy the last residuals in reverse order
            int q = ma.getDegree();
            double[] e = new double[q];
            // copy the last obs, in reverse order.
            last = res.getLength() - 1;
            for (int i = 0; i < q; ++i) {
                e[i] = res.get(last - i);
            }
            for (int i = 0; i < nf; ++i) {
                double s = mean;
                for (int j = 0; j < p; ++j) {
                    s -= ar.get(j + 1) * y[j];
                }
                for (int j = i; j < q; ++j) {
                    s += ma.get(j + 1) * e[j - i];
                }
                for (int j = p - 1; j > 0; --j) {
                    y[j] = y[j - 1];
                }
                if (p > 0) {
                    y[0] = s;
                }
                fcasts[i] = s;
            }
            return fcasts;
        }
        catch (Exception err) {
            return null;
        }
    }

    // computes the residuals;
    private DataBlock residuals(IReadDataBlock data) {
        DataBlock w = new DataBlock(data);
        try {
            // step 1. AR filter w, if necessary
            DataBlock z = w;

            int p = ar.getDegree();
            int q = ma.getDegree();
            if (p > 0) {
                z = new DataBlock(w.getLength() - p);
                DataBlock x = w.drop(p, 0);
                z.copy(x);
                for (int i = 1; i <= p; ++i) {
                    x.move(-1);
                    z.addAY(ar.get(i), x);
                }
            }
            if (mean != 0) {
                z.sub(mean);
            }
            // filter z (pure ma part)
            if (q > 0) {
                MaLjungBoxFilter malb = new MaLjungBoxFilter();
                int nwl = malb.initialize((IArimaModel) arima.stationaryTransformation().stationaryModel, z.getLength());
                DataBlock wl = new DataBlock(nwl);
                malb.filter(z, wl);
                return wl;
            }
            else {
                return z;
            }
        }
        catch (Exception err) {
            return w;
        }
    }
}
