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
package ec.tstoolkit.arima;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.IRationalFilter;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.utilities.Ref;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractLinearModel implements ILinearModel {

    private Spectrum spectrum_;
    private AutoCovarianceFunction acgf_;

    /**
     *
     */
    protected AbstractLinearModel() {
    }

    /**
     * 
     * @param m
     */
    protected AbstractLinearModel(final AbstractLinearModel m) {
        acgf_ = m.acgf_;
        spectrum_ = m.spectrum_;
    }

    /**
     *
     */
    protected void clearCachedObjects() {
        spectrum_ = null;
        acgf_ = null;
    }

    @Override
    protected Object clone() {
        try {
            AbstractLinearModel model = (AbstractLinearModel) super.clone();
            return model;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    @Override
    public AutoCovarianceFunction getAutoCovarianceFunction() {
        if (!isStationary()) {
            return null;
        }
        if (acgf_ == null) {
            acgf_ = initAcgf();
        }
        return acgf_;
    }

    @Override
    public Spectrum getSpectrum() {
        if (spectrum_ == null) {
            spectrum_ = initSpectrum();
        }
        return spectrum_;
    }

    /**
     * 
     * @return
     */
    protected AutoCovarianceFunction initAcgf() {
        IRationalFilter rf = getFilter();
        Polynomial num = Polynomial.copyOf(rf.getNumerator().getWeights());
        Polynomial denom = Polynomial.copyOf(rf.getDenominator().getWeights()); 
        Ref<Polynomial> ndenom = new Ref<>(null);
        ec.tstoolkit.maths.linearfilters.Utilities.stabilize(denom, ndenom);
        return new AutoCovarianceFunction(num, ndenom.val, getInnovationVariance());

    }

    /**
     * 
     * @return
     */
    protected Spectrum initSpectrum() {
        IRationalFilter rf = this.getFilter();
        return new Spectrum(SymmetricFilter.createFromFilter(rf.getNumerator()).times(getInnovationVariance()), SymmetricFilter.createFromFilter(rf.getDenominator()));
    }
}
