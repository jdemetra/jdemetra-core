/*
 * Copyright 2016 National Bank ofInternal Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofInternal the Licence at:
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
import jdplus.dstats.Normal;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.random.JdkRNG;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.univariate.ISsfError;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
public class RandomGenerator {

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

    private Matrix LA;
    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final ISsfError error;
    private double svar = 1, dvar = 100;

    public RandomGenerator(ISsf ssf) {
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
        error=ssf.measurementError();
        initSsf();
    }

    public double[] newRandom(int n) {
        return new RandomData(n).getRandomData();
    }

    private double lh(int pos) {
        return error == null ? 0 : Math.sqrt(error.at(pos));
    }

    private double h(int pos) {
        return error == null ? 0 : error.at(pos);
    }

    private void initSsf() {
        int dim = ssf.getStateDim();
        LA = Matrix.square(dim);
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
        // generate diffuse elements
        double std = Math.sqrt(svar);
        a.mul(std);
        ISsfInitialization initialization = ssf.initialization();
        if (initialization.isDiffuse()) {
            DataBlock b = DataBlock.make(initialization.getDiffuseDim());
            fillRandoms(b);
            double dstd = Math.sqrt(dvar);
            b.mul(dstd);
            Matrix B= Matrix.make(a.length(), b.length());
            initialization.diffuseConstraints(B);
            a.addProduct(B.rowsIterator(), b);
        }
    }

    class RandomData {

        private final int dim, resdim, n;

        public RandomData(int n) {
            this.n = n;
            dim = ssf.initialization().getStateDim();
            resdim = dynamics.getInnovationsDim();
            if (error != null) {
                measurementErrors = new double[n];
                generateMeasurementRandoms(DataBlock.of(measurementErrors));
            } else {
                measurementErrors = null;
            }
            simulatedData = new double[n];
            generateData();
        }

        final double[] measurementErrors;
        private final double[] simulatedData;

        private void generateData() {
            DataBlock a = DataBlock.make(dim);
            generateInitialState(a);
            double std = Math.sqrt(svar);
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
                    dynamics.addSU(i - 1, a, q);
                }
                simulatedData[i] = loading.ZX(i, a);
                if (measurementErrors != null) {
                    simulatedData[i] += measurementErrors[i] * std;
                }
            }
        }

        /**
         * @return the simulatedData
         */
        public double[] getRandomData() {
            return simulatedData;
        }

    }
}
