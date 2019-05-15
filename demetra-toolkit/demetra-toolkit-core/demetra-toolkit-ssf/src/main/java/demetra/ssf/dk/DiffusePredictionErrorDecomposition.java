/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.dk;

import demetra.ssf.likelihood.DiffuseLikelihood;
import jd.data.DataBlock;
import demetra.likelihood.DeterminantalTerm;
import demetra.ssf.StateInfo;
import demetra.ssf.akf.AugmentedState;
import demetra.ssf.dk.sqrt.IDiffuseSquareRootFilteringResults;
import demetra.ssf.univariate.PredictionErrorDecomposition;
import demetra.likelihood.Likelihood;

/**
 *
 * @author Jean Palate
 */
public class DiffusePredictionErrorDecomposition extends PredictionErrorDecomposition implements IDiffuseFilteringResults, IDiffuseSquareRootFilteringResults {

    private final DeterminantalTerm ddet = new DeterminantalTerm();
    private int nd, enddiffuse;

    public DiffusePredictionErrorDecomposition(boolean res) {
        super(res);
    }

    @Override
    public DiffuseLikelihood likelihood() {
        return DiffuseLikelihood.builder(nd + cumulator.getObsCount(), nd)
                .ssqErr(cumulator.getSsqErr())
                .logDeterminant(cumulator.getLogDeterminant())
                .diffuseCorrection(ddet.getLogDeterminant())
                .residuals(bres? res:null).build();
    }

    @Override
    public void close(int pos) {
        enddiffuse = pos;
    }

    @Override
    public void clear() {
        super.clear();
        ddet.clear();
        nd = 0;
        enddiffuse = 0;
    }

    @Override
    public void save(int t, DiffuseUpdateInformation pe) {
        if (pe == null || pe.isMissing()) {
            return;
        }
        double d = pe.getDiffuseVariance();
        if (d != 0) {
            ++nd;
            ddet.add(d);
        } else {
            double e = pe.get();
            cumulator.add(e, pe.getVariance());
            if (bres) {
                res.set(t, e / pe.getStandardDeviation());
            }
        }
    }

    @Override
    public void save(final int pos, final DiffuseState state, final StateInfo info) {
    }

    @Override
    public void save(final int pos, final AugmentedState state, final StateInfo info) {
    }
 
    @Override
    public int getEndDiffusePosition() {
        return enddiffuse;
    }

}
