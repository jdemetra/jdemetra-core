/*
 * Copyright 2016-2017 National Bank copyOf Belgium
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
package demetra.ssf.univariate;

import demetra.ssf.ISsfLoading;
import demetra.ssf.UpdateInformation;
import demetra.data.DataBlock;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.MatrixWindow;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.SsfException;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.StateStorage;
import demetra.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import demetra.ssf.implementations.DummyInitialization;

/**
 * /**
 The fixed point smoother computes the expectations and the covariance
 matrices copyOf [M*a(fixpos) | fixpos + k]. The ordinary filter is used till
 position fixpos, where E(a(fixpos)|fixpos), Cov((a(fixpos)|fixpos)) is
 available. The moments copyOf the augmented state vector a(fixpos), Ma(fixpos)
 can then be easily derived. The augmented vector is then used to compute the
 next expectations/cov. See for instance Anderson and Moore (optimal filtering
 [1979]).
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FixedPointSmoother {

    private final ISsf ssf;
    private final int fixpos;
    private final FastMatrix M;
    private StateStorage states;

    /**
     * Returns the expectations copyOf the augmented part copyOf the filter at position
 pos, computed after the fixed point position. The size copyOf the augmented
 part is either the size copyOf the underlying state space model or the number
 copyOf rows copyOf the M matrix that defines the linear transformation copyOf the
 state vector considered in the smoothing algorithm.
     *
     * @param pos The position copyOf the requested information (corresponds to
 fixdpos+pos)
     * @return The expectations vector. Should not be modified
     */
    public StateStorage getResults() {
        return states;
    }

    /**
     * Defines the smoother. The complete state array will be considered in the
     * smoothing
     *
     * @param ssf The original state space form
     * @param fixpos The position copyOf the fixed point
     */
    public FixedPointSmoother(final ISsf ssf, final int fixpos) {
        this.ssf = ssf;
        this.fixpos = fixpos;
        M = null;
    }

    /**
     * Defines the smoother. The state array transformed by M will be considered
     * in the smoothing
     *
     * @param ssf The original state space form
     * @param fixpos The position copyOf the fixed point
     * @param M The transformation matrix. May be null; in that case, M is
     * considered to be I.
     */
    public FixedPointSmoother(final ISsf ssf, final int fixpos, final FastMatrix M) {

        if (M != null && ssf.getStateDim() != M.getColumnsCount()) {
            throw new SsfException("Invalid fixed point argument");
        }
        this.ssf = ssf;
        this.fixpos = fixpos;
        this.M = M;
    }

    public FastMatrix getTransformationMatrix() {
        return M;
    }

    public int getFixedPointPosition() {
        return fixpos;
    }

    public boolean process(ISsfData data) {
        // step 1: filtering till fixpos
        OrdinaryFilter filter = new OrdinaryFilter(new Initializer(ssf, fixpos, M));
        SsfDataWindow xdata = new SsfDataWindow(data, fixpos, data.length());
        int mdim = M == null ? ssf.getStateDim() : M.getRowsCount();
        Ssf xssf = Ssf.of(new DummyInitialization(mdim+ssf.getStateDim()), 
                new Dynamics(ssf, mdim), new Loading(ssf, mdim), ssf.measurementError());
        states = StateStorage.full(StateInfo.Concurrent);
        states.prepare(mdim, fixpos, data.length());
        Results frslts = new Results(states, ssf.getStateDim(), mdim);
        return filter.process(xssf, xdata, frslts);
    }

    static class Initializer implements OrdinaryFilter.Initializer {

        private final int fixpos;
        private final FastMatrix M;
        private final ISsf core;

        Initializer(final ISsf core, final int fixpos, final FastMatrix M) {
            this.fixpos = fixpos;
            this.M = M;
            this.core = core;
        }

        @Override
        public int initializeFilter(State state, ISsf ssf, ISsfData data) {
            DiffuseSquareRootInitializer init = new DiffuseSquareRootInitializer(null);
            OrdinaryFilter filter = new OrdinaryFilter(init);
            SsfDataWindow data0 = new SsfDataWindow(data, 0, fixpos);
            filter.process(core, data, null);
            if (init.getEndDiffusePos() > fixpos) {
                return -1;
            }
            int r = core.getStateDim();
            DataBlock a = filter.getFinalState().a();
            FastMatrix P = filter.getFinalState().P();
            state.a().range(0, r).copy(a);
            MatrixWindow cur = state.P().topLeft(r, r);
            cur.copy(P);
            if (M == null) {
                state.a().range(r, 2 * r).copy(a);
                cur.vnext(r);
                cur.copy(P);
                cur.hnext(r);
                cur.copy(P);
                cur.vprevious(r);
                cur.copy(P);
            } else {
                int m = M.getRowsCount();
                state.a().range(r, r + m).product(M.rowsIterator(), a);
                cur.vnext(r);
                cur.product(M, P);
                cur.hnext(r);
                SymmetricMatrix.XSXt(P, M, cur);
                cur.vprevious(r);
                cur.product(M, P);
            }
            return fixpos;
        }
    }

    static class Loading implements ISsfLoading{

        private final ISsfLoading core;
        private final int cdim, mdim;

        Loading(ISsf ssf, int mdim) {
            this.core = ssf.loading();
            this.mdim = mdim;
            this.cdim = ssf.getStateDim();
        }

        @Override
        public void Z(int pos, DataBlock z) {
            core.Z(pos, z.range(0, cdim));
        }

        @Override
        public double ZX(int pos, DataBlock m) {
            return core.ZX(pos, m.range(0, cdim));
        }

        @Override
        public double ZVZ(int pos, FastMatrix V) {
            return core.ZVZ(pos, V.topLeft(cdim, cdim));
        }

        @Override
        public boolean isTimeInvariant() {
            return core.isTimeInvariant();
        }

        @Override
        public void VpZdZ(int pos, FastMatrix V, double d) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }

        @Override
        public void XpZd(int pos, DataBlock x, double d) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }
    }
    
    static class Dynamics implements ISsfDynamics {

        private final ISsfDynamics core;
        private final int cdim, mdim;

        Dynamics(ISsf ssf, int mdim) {
            this.core = ssf.dynamics();
            this.mdim = mdim;
            this.cdim = ssf.getStateDim();
        }

        @Override
        public int getInnovationsDim() {
            return core.getInnovationsDim();
        }

        @Override
        public void V(int pos, FastMatrix qm) {
            core.V(pos, qm.topLeft(cdim, cdim));
        }

        @Override
        public void S(int pos, FastMatrix cm) {
            core.S(pos, cm.top(cdim));
        }

        @Override
        public boolean hasInnovations(int pos) {
            return core.hasInnovations(pos);
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return core.areInnovationsTimeInvariant();
        }

        @Override
        public void T(int pos, FastMatrix tr) {
            core.T(pos, tr.topLeft(cdim, cdim));
            tr.bottomRight(mdim, mdim).diagonal().set(1);
        }

        @Override
        public void TX(int pos, DataBlock x) {
            core.TX(pos, x.range(0, cdim));
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }

        @Override
        public void addV(int pos, FastMatrix p) {
            core.addV(pos, p.topLeft(cdim, cdim));
        }

        @Override
        public void XT(int pos, DataBlock x) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body copyOf generated methods, choose Tools | Templates.
        }


        @Override
        public boolean isTimeInvariant() {
            return core.isTimeInvariant();
        }

    }

    static class Results implements IFilteringResults {

        private StateStorage states;
        private final int start, n;

        Results(StateStorage states, final int start, final int n) {
            this.states = states;
            this.start = start;
            this.n = n;
        }

        @Override
        public void save(int t, UpdateInformation pe) {
        }

        @Override
        public void clear() {
            states.clear();
        }

        @Override
        public void save(int pos, State state, StateInfo info) {
            if (info == StateInfo.Forecast) {
                states.save(pos, state.a().extract(start, n), state.P().extract(start, n, start, n));
            }
        }

    }
}
