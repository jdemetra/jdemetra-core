/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.ssf.akf;

import nbbrd.design.Development;
import jdplus.ssf.StateInfo;
import jdplus.ssf.UpdateInformation;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.PredictionErrorDecomposition;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class QPredictionErrorDecomposition extends PredictionErrorDecomposition implements IQFilteringResults {

    private int nd, ncollapsed;
    private final QAugmentation Q = new QAugmentation();

    public QPredictionErrorDecomposition(boolean res) {
        super(res);
    }

    @Override
    public void prepare(final ISsf ssf, final int n) {
        super.prepare(ssf, n);
        nd = ssf.getDiffuseDim();
        Q.prepare(nd, 1);
    }

    @Override
    public void close(int pos) {
    }

    @Override
    public void clear() {
        super.clear();
        Q.clear();
        res = null;
    }

    @Override
    public void save(int t, AugmentedUpdateInformation pe) {
        if (pe.getStatus() != UpdateInformation.Status.OBSERVATION) {
            return;
        }
        if (pe.isDiffuse()) {
            Q.update(pe);
        } else {
            super.save(t, pe);
        }
    }

    @Override
    public void save(final int t, final AugmentedState state, final StateInfo info) {
    }

    @Override
    public boolean canCollapse() {
        return Q.canCollapse();
    }

    @Override
    public boolean collapse(int pos, AugmentedState state) {
        if (Q.collapse(state)) {
            ncollapsed = pos;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getCollapsingPosition() {
        return ncollapsed;
    }

    @Override
    public QAugmentation getAugmentation() {
        return Q;
    }

    @Override
    public DiffuseLikelihood likelihood(boolean scalingfactor) {
        DiffuseLikelihood ll = Q.likelihood(scalingfactor);
        return ll.add(super.likelihood(scalingfactor));
    }

}
