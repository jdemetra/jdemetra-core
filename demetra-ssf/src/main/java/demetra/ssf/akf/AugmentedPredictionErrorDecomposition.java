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
package demetra.ssf.akf;

import demetra.design.Development;
import demetra.ssf.StateInfo;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.PredictionErrorDecomposition;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AugmentedPredictionErrorDecomposition extends PredictionErrorDecomposition implements IAugmentedFilteringResults {

    private int nd, ncollapsed;
    private final QAugmentation Q = new QAugmentation();

    public AugmentedPredictionErrorDecomposition(boolean res) {
        super(res);
    }

    @Override
    public void prepare(final ISsf ssf, final int n) {
        super.prepare(ssf, n);
        nd = ssf.getDynamics().getNonStationaryDim();
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
        if (pe == null || pe.isMissing()) {
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
    public DiffuseLikelihood likelihood() {
        DiffuseLikelihood ll = Q.likelihood();
        ll.add(super.likelihood());
        return ll;
    }

}
