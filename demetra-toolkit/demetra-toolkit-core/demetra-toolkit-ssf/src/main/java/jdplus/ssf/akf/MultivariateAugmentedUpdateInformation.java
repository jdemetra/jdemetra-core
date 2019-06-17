/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.ssf.akf;

import jdplus.data.DataBlockIterator;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.ssf.State;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.MultivariateUpdateInformation;
import demetra.data.DoubleSeq;

/**
 * The augmented state only contains information on non missing values. So, it
 * has to be considered with the corresponding data.
 *
 * @author Jean Palate
 */
public class MultivariateAugmentedUpdateInformation extends MultivariateUpdateInformation {

    /**
     * E is the "prediction error" on the diffuse constraints (=(0-Z(t)A(t)) E ~
     * ndiffuse x nvars
     */
    private final CanonicalMatrix E;

    /**
     *
     * @param ndiffuse
     * @param nvars
     * @param dim
     */
    public MultivariateAugmentedUpdateInformation(final int dim, final int nvars, final int ndiffuse) {
        super(dim, nvars);
        E = CanonicalMatrix.make(ndiffuse, nvars);
    }

    public CanonicalMatrix E() {
        return E;
    }

    public boolean isDiffuse() {
        return E.isZero(State.ZERO);
    }

    public void compute(IMultivariateSsf ssf, int t, AugmentedState state, DoubleSeq x, int[] equations) {

        super.compute(ssf, t, state, x, equations);
        // E is ndiffuse x nobs. Each column contains the diffuse effects
        // on the corresponding variable
        ZM(t, ssf.measurements(), equations, state.B(), E.transpose());
        E.chs();
        DataBlockIterator erows = E.rowsIterator();
        while (erows.hasNext()) {
            LowerTriangularMatrix.rsolve(this.getCholeskyFactor(), erows.next(), State.ZERO);
        }
    }
}
