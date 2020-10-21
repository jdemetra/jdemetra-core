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
package jdplus.ssf.multivariate;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import nbbrd.design.Development;
import jdplus.ssf.IPredictionErrorDecomposition;
import jdplus.likelihood.ResidualsCumulator;
import jdplus.ssf.State;
import jdplus.ssf.StateInfo;
import jdplus.likelihood.Likelihood;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PredictionErrorsDecomposition implements
        IPredictionErrorDecomposition, IMultivariateFilteringResults {

    private final ResidualsCumulator cumulator = new ResidualsCumulator();
    private DataBlockStorage res;
    private boolean bres;

    /**
     *
     * @param bres
     */
    public PredictionErrorsDecomposition(final boolean bres) {
        this.bres = bres;
    }

    /**
     *
     */
    @Override
    public void close() {
    }

    /**
     *
     * @return
     */
    public boolean hasResiduals() {
        return bres;
    }

    public double[] allResiduals() {
        return res.storage();
    }

    /**
     *
     * @param ssf
     * @param data
     */
    @Override
    public void open(final IMultivariateSsf ssf, final IMultivariateSsfData data) {
        cumulator.clear();
    }

//    /**
//     *
//     * @param ssf
//     * @param data
//     */
//    @Override
//    public void prepare(final IMultivariateSsf ssf, final IMultivariateSsfData data) {
//        clear();
//    }
    @Override
    public void save(final int t, final State state, final StateInfo info) {
    }

    @Override
    public void save(final int t, final MultivariateUpdateInformation pe) {
        if (pe == null) {
            return;
        }
        DataBlock diag = pe.getCholeskyFactor().diagonal();
        DataBlock err = pe.getTransformedPredictionErrors();
        for (int i = 0; i < err.length(); ++i) {
            double r = diag.get(i);
            if (r != 0) {
                cumulator.addStd(err.get(i), r);
            }
        }
    }

    @Override
    public Likelihood likelihood(boolean scalingfactor) {
        return Likelihood.builder(cumulator.getObsCount())
                .scalingFactor(scalingfactor)
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant()).build();
    }

}
