/*
 * Copyright 2016 National Bank ofFunction Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofFunction the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.dk;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.dstats.Normal;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.random.JdkRNG;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.univariate.ISsfError;
import demetra.dstats.RandomNumberGenerator;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DiffuseSimulationSmoother {

    private static final Normal N = new Normal();
    private static final RandomNumberGenerator RNG = JdkRNG.newRandom(0);

    private static void fillRandoms(DataBlock u) {
        synchronized (N) {
            for (int i = 0; i < u.length(); ++i) {
                u.set(i, N.random(RNG));
            }
        }
    }

    private static final double EPS = 1e-8;

    private FastMatrix LA;
    private final ISsf ssf;
    private final ISsfData data;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final ISsfError error;
    private final Smoothing smoothing;
    private final double var;

    public DiffuseSimulationSmoother(ISsf ssf, ISsfData data, boolean rescaleVariance) {
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
        error=ssf.measurementError();
        this.data = data;
        initSsf();
        smoothing = new Smoothing();
        var = rescaleVariance ? smoothing.frslts.var() : 1;
    }

    public Smoothing getReferenceSmoothing() {
        return smoothing;
    }

    public Simulation newSimulation() {
        return new Simulation();
    }

    private double lh(int pos) {
        return error == null ? 0 : Math.sqrt(error.at(pos));
    }

    private double h(int pos) {
        return error == null ? 0 : error.at(pos);
    }

    private void initSsf() {
        int dim = ssf.getStateDim();
        LA = FastMatrix.square(dim);
        ssf.initialization().Pf0(LA);
        SymmetricMatrix.lcholesky(LA, EPS);

    }

    private void generateTransitionRandoms(int pos, DataBlock u) {
        fillRandoms(u);
    }

    private void generateMeasurementRandoms(DataBlock e) {
        fillRandoms(e);
        e.mul(lh(0));
    }

    private void generateInitialState(DataBlock a) {
        fillRandoms(a);
        LowerTriangularMatrix.Lx(LA, a);
    }

    abstract class BaseSimulation {

        protected final BaseDiffuseFilteringResults frslts;
        protected final DataBlockStorage smoothedInnovations;
        protected final DataBlock esm;
        protected DataBlockStorage smoothedStates;
        protected DataBlock a0;
        protected final int dim, resdim, n, nd;
        protected DataBlock R, Ri;

        protected abstract double getError(int pos);

        protected BaseSimulation(BaseDiffuseFilteringResults frslts) {
            this.frslts = frslts;
            dim = ssf.getStateDim();
            resdim = dynamics.getInnovationsDim();
            n = data.length();
            nd = frslts.getEndDiffusePosition();
            smoothedInnovations = new DataBlockStorage(resdim, n);
            if (error != null) {
                esm = DataBlock.make(n);
            } else {
                esm = null;
            }
        }

        protected final void smooth() {
            R = DataBlock.make(dim);
            Ri = DataBlock.make(dim);
            // we reproduce here the usual iterations ofFunction the smoother
            doNormalSmoothing();
            doDiffuseSmoohing();
//            smoothedInnovations.rescale(Math.sqrt(var));
            computeInitialState();
        }

        private void doNormalSmoothing() {
            double e, v;
            DataBlock U = DataBlock.make(resdim);
            boolean missing;
            int pos = n;
            while (--pos >= nd) {
                if (dynamics.hasInnovations(pos)) {
                    dynamics.XS(pos, R, U);
                    smoothedInnovations.save(pos, U);
                }
                // Get info
                e = getError(pos);
                v = frslts.errorVariance(pos);
                missing = !Double.isFinite(e);
                // Iterate R
                dynamics.XT(pos, R);
                if (!missing && e != 0) {
                    // RT
                    double c = (e - R.dot(frslts.M(pos))) / v;
                    loading.XpZd(pos, R, c);
                }
                // Computes esm, U
                if (esm != null) {
                    if (!missing) {
                        esm.set(pos, h(pos));
                    } else {
                        esm.set(pos, Double.NaN);
                    }
                }
            }
        }

        private void doDiffuseSmoohing() {
            double e, f, fi, c;
            DataBlock C = DataBlock.make(dim), Ci = DataBlock.make(dim);
            DataBlock U = DataBlock.make(resdim);
            boolean missing;
            int pos = nd;
            while (--pos >= 0) {
                if (dynamics.hasInnovations(pos)) {
                    dynamics.XS(pos, R, U);
                    smoothedInnovations.save(pos, U);
                }
                // Get info
                e = getError(pos);
                f = frslts.errorVariance(pos);
                fi = frslts.diffuseNorm2(pos);
                C.copy(frslts.M(pos));
                if (fi != 0) {
                    Ci.copy(frslts.Mi(pos));
                    Ci.mul(1 / fi);
                    C.addAY(-f, Ci);
                    C.mul(1 / fi);
                } else if (f != 0) {
                    C.mul(1 / f);
                    Ci.set(0);
                }
                missing = !Double.isFinite(e);
                // Iterate R
                dynamics.XT(pos, R);
                dynamics.XT(pos, Ri);
                if (fi == 0) {
                    if (!missing && f != 0) {
                        c = e / f - R.dot(C);
                        loading.XpZd(pos, R, c);
                    }
                } else if (!missing && f != 0) {
                    c = -Ri.dot(Ci);
                    double ci = e / fi + c - R.dot(C);
                    loading.XpZd(pos, Ri, ci);
                    double cf = -R.dot(Ci);
                    loading.XpZd(pos, R, cf);
                }
                // Computes esm, U
                if (esm != null) {
                    if (!missing) {
                        esm.set(pos, h(pos));
                    } else {
                        esm.set(pos, Double.NaN);
                    }
                }
            }
        }

        private void computeInitialState() {
            // initial state
            a0 = DataBlock.make(dim);
            FastMatrix Pf0 = FastMatrix.square(dim);
            ISsfInitialization initializer = ssf.initialization();
            initializer.a0(a0);
            initializer.Pf0(Pf0);
            // stationary initialization
            a0.addProduct(R, Pf0.columnsIterator());

            // non stationary initialisation
            if (initializer.isDiffuse()) {
                FastMatrix Pi0 = FastMatrix.square(dim);
                initializer.Pi0(Pi0);
                a0.addProduct(Ri, Pi0.columnsIterator());
            }
        }

        public DataBlock getSmoothedInnovations(int pos) {
            return smoothedInnovations.block(pos);
        }

        public DataBlock getSmoothedState(int pos) {
            if (smoothedStates == null) {
                generatesmoothedStates();
            }
            return smoothedStates.block(pos);
        }

        public DataBlockStorage getSmoothedInnovations() {
            return smoothedInnovations;
        }

        public DataBlockStorage getSmoothedStates() {
            if (smoothedStates == null) {
                generatesmoothedStates();
            }
            return smoothedStates;
        }

        private void generatesmoothedStates() {
            smoothedStates = new DataBlockStorage(dim, n);
            smoothedStates.save(0, a0);
            int cur = 1;
            DataBlock a = DataBlock.of(a0);
            
            while (cur<n) {
                // next: a(t+1) = T(t) a(t) + S*r(t)
                dynamics.TX(cur-1, a);
                if (dynamics.hasInnovations(cur-1)) {
                    DataBlock u=smoothedInnovations.block(cur-1);
                    dynamics.addSU(cur-1, a, u);
                }
                smoothedStates.save(cur++, a);
            } 
        }
    }

    public class Smoothing extends BaseSimulation {

        Smoothing() {
            super(DkToolkit.sqrtFilter(ssf, data, false));
            smooth();
        }

        @Override
        protected double getError(int pos) {
            return frslts.error(pos);
        }

    }

    public class Simulation extends BaseSimulation {

        public Simulation() {
            super(smoothing.frslts);
            states = new DataBlockStorage(dim, n);
            transitionInnovations = new DataBlockStorage(resdim, n);
            if (error != null) {
                measurementErrors = new double[n];
                generateMeasurementRandoms(DataBlock.copyOf(measurementErrors));
            } else {
                measurementErrors = null;
            }
            simulatedData = new double[n];
            generateData();
            filter();
            smooth();
        }

        final DataBlockStorage states;
        private DataBlockStorage simulatedStates, simulatedInnovations;
        final DataBlockStorage transitionInnovations;
        final double[] measurementErrors;
        private double[] ferrors;
        private final double[] simulatedData;

        private void generateData() {
            double std = Math.sqrt(var);
            DataBlock a0f = DataBlock.make(dim);
            generateInitialState(a0f);
            a0f.mul(std);
            DataBlock a = DataBlock.make(dim);
            ssf.initialization().a0(a);
            a.add(a0f);
            states.save(0, a);
            simulatedData[0] = loading.ZX(0, a);
            if (measurementErrors != null) {
                simulatedData[0] += measurementErrors[0] * std;
            }
            // a0 = a(1|0) -> y[1) = Z*a[1|0) + e(1)
            // a(2|1) = T a(1|0) + S * q(1)...
            DataBlock q = DataBlock.make(resdim);
            for (int i = 1; i < simulatedData.length; ++i) {
                dynamics.TX(i, a);
                if (dynamics.hasInnovations(i - 1)) {
                    generateTransitionRandoms(i - 1, q);
                    q.mul(std);
                    transitionInnovations.save(i - 1, q);
                    dynamics.addSU(i-1, a, q);
                }
                states.save(i, a);
                simulatedData[i] = loading.ZX(i, a);
                if (measurementErrors != null) {
                    simulatedData[i] += measurementErrors[i] * std;
                }
            }
        }

        private void filter() {
            DkFilter f = new DkFilter(ssf, frslts, new ResultsRange(0, n), false);
            ferrors = simulatedData.clone();
            f.filter(DataBlock.copyOf(ferrors));
        }

        /**
         * @return the simulatedData
         */
        public double[] getSimulatedData() {
            return simulatedData;
        }

        public DataBlockStorage getGeneratedStates() {
            return states;
        }

        @Override
        protected double getError(int pos) {
            return ferrors[pos];
        }

        public DoubleSeq getErrors() {
            return DoubleSeq.of(ferrors);
        }

        public DataBlockStorage getSimulatedStates() {
            if (simulatedStates == null) {
                computeSimulatedStates();
            }
            return simulatedStates;
        }

        private void computeSimulatedStates() {
            simulatedStates = new DataBlockStorage(dim, n);
            DataBlockStorage sm = smoothing.getSmoothedStates();
            DataBlockStorage ssm = getSmoothedStates();
            DataBlock a = DataBlock.make(dim);
            for (int i = 0; i < n; ++i) {
                a.copy(sm.block(i));
                a.sub(ssm.block(i));
                a.add(states.block(i));
                simulatedStates.save(i, a);
            }
        }

        public DataBlockStorage getSimulatedInnovations() {
            if (simulatedInnovations == null) {
                computeSimulatedInnovations();
            }
            return simulatedInnovations;
        }

        private void computeSimulatedInnovations() {
            simulatedInnovations = new DataBlockStorage(resdim, n);
            DataBlockStorage sm = smoothing.getSmoothedInnovations();
            DataBlockStorage ssm = getSmoothedInnovations();
            DataBlock u = DataBlock.make(dim);
            for (int i = 0; i < n; ++i) {
                u.copy(sm.block(i));
                u.sub(ssm.block(i));
                if (dynamics.hasInnovations(i)) {
                    u.add(transitionInnovations.block(i));
                }
                simulatedInnovations.save(i, u);
            }
        }
    }
}
