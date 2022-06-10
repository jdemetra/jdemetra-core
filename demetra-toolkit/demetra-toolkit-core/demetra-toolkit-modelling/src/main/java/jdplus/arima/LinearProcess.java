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
package jdplus.arima;

import jdplus.math.linearfilters.BackFilter;
import jdplus.math.linearfilters.ForeFilter;
import nbbrd.design.Development;
import nbbrd.design.Immutable;
import jdplus.math.linearfilters.IRationalFilter;
import jdplus.math.linearfilters.RationalBackFilter;
import jdplus.math.linearfilters.RationalFilter;
import jdplus.math.linearfilters.RationalForeFilter;
import jdplus.math.linearfilters.SymmetricFilter;
import jdplus.math.polynomials.Polynomial;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable(lazy = true)
public final class LinearProcess implements ILinearProcess {

    private final RationalFilter rf;
    private final double var;
    private volatile AutoCovarianceFunction acf;
    private volatile Spectrum spectrum;

    /**
     *
     * @param rf
     * @param var
     */
    public LinearProcess(final RationalFilter rf, final double var) {
        this.rf = rf;
        this.var = var;
    }

//    /**
//     *
//     * @return
//     */
    public LinearProcess doStationary() {
        RationalBackFilter rb = rf.getRationalBackFilter();
        BackFilter bdenom = rb.getDenominator();
        
        BackFilter.StationaryTransformation bst = new BackFilter.StationaryTransformation();
        if (bst.transform(bdenom)) {
            bdenom = bst.stationaryFilter;
        }

        RationalForeFilter rf = this.rf.getRationalForeFilter();
        ForeFilter fdenom = rf.getDenominator();
        ForeFilter.StationaryTransformation fst = new ForeFilter.StationaryTransformation();
        if (fst.transform(fdenom)) {
            fdenom = fst.stationaryFilter;
        }

        RationalFilter stfilter = new RationalFilter(this.rf.getNumerator(), bdenom, fdenom);
        return new LinearProcess(stfilter, var);
    }

    /**
     *
     * @return
     */
    @Override
    public RationalFilter getFilter() {
        return rf;
    }

    /**
     *
     * @return
     */
    @Override
    public double getInnovationVariance() {
        return var;
    }

    @Override
    public boolean isInvertible() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isNull() {
        return var == 0;
    }

    @Override
    public boolean isStationary() {
        return true;
    }

    @Override
    public AutoCovarianceFunction getAutoCovarianceFunction() {
        AutoCovarianceFunction tmp = acf;
        if (tmp == null) {
            synchronized (this) {
                tmp=acf;
                if (tmp == null) {
                    RationalFilter f = getFilter();
                    Polynomial n = Polynomial.ofInternal(f.getNumerator().weightsToArray());
                    Polynomial bd = f.getRationalBackFilter().getDenominator().asPolynomial();
                    Polynomial fd = f.getRationalForeFilter().getDenominator().asPolynomial();
                    tmp = new AutoCovarianceFunction(n, bd.times(fd), getInnovationVariance());
                    acf=tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public Spectrum getSpectrum() {
        Spectrum tmp = spectrum;
        if (tmp == null) {
            synchronized (this) {
                tmp=spectrum;
                if (tmp == null) {
                    IRationalFilter rf = this.getFilter();
                    tmp = new Spectrum(SymmetricFilter.convolutionOf(rf.getNumerator()).times(getInnovationVariance()), SymmetricFilter.convolutionOf(rf.getDenominator()));
                    spectrum=tmp;
                }
            }
        }
        return tmp;
    }

}
