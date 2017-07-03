/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.arima;

import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.IRationalFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.maths.linearfilters.SymmetricFilter;

/**
 * This class caches all the properties of the final ArimaModel
 *
 * @author Jean Palate
 */
public abstract class AbstractArimaModel implements IArimaModel {

    private volatile RationalBackFilter pi, psi;
    private volatile Spectrum spectrum;
    private volatile AutoCovarianceFunction acf;

    protected abstract SymmetricFilter symmetricMA();

    protected abstract SymmetricFilter symmetricAR();

    @Override
    public Spectrum getSpectrum() {
        Spectrum s = spectrum;
        if (s == null) {
            synchronized (this) {
                s = spectrum;
                if (s == null) {
                    s = new Spectrum(symmetricMA(), symmetricAR());
                    spectrum = s;
                }
            }
        }
        return s;
    }

    @Override
    public AutoCovarianceFunction getAutoCovarianceFunction() {
        if (!isStationary()) {
            throw new ArimaException(ArimaException.NONSTATIONARY);
        }
        AutoCovarianceFunction fn = acf;
        if (fn == null) {
            synchronized (this) {
                fn = acf;
                if (fn == null) {
                    fn = new AutoCovarianceFunction(getMA().asPolynomial(), getStationaryAR().asPolynomial(), getInnovationVariance());
                    acf = fn;
                }
            }
        }
        return fn;
    }

    @Override
    public RationalBackFilter getPiWeights() {
        RationalBackFilter filter = pi;
        if (filter == null) {
            synchronized (this) {
                filter = pi;
                if (filter == null) {
                    filter = new RationalBackFilter(getAR(), getMA(), 0);
                    pi = filter;
                }
            }
        }
        return filter;
    }

    @Override
    public RationalBackFilter getPsiWeights() {
        RationalBackFilter filter = psi;
        if (filter == null) {
            synchronized (this) {
                filter = psi;
                if (filter == null) {
                    filter = new RationalBackFilter(getMA(), getAR(), 0);
                    psi = filter;
                }
            }
        }
        return filter;
    }

    @Override
    public IRationalFilter getFilter() throws ArimaException {
        return getPsiWeights();
    }

    @Override
    public boolean isStationary() {
        return getNonStationaryARDegree() > 0;
    }

}
