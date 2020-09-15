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
import jdplus.dstats.Normal;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsfError;
import jdplus.random.RandomNumberGenerator;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;

/**
 *
 * @author Jean Palate
 */
public class RandomSsfGenerator {

    private final Normal N;

    private void fillRandoms(DataBlock u, RandomNumberGenerator rng) {
        for (int i = 0; i < u.length(); ++i) {
            u.set(i, N.random(rng));
        }
    }

    private double random(RandomNumberGenerator rng) {
        return N.random(rng);
    }

    private Matrix LA;
    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfLoading loading;
    private final ISsfError error;
    private final int n;

    public RandomSsfGenerator(ISsf ssf, double var, int n) {
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
        error = ssf.measurementError();
        initSsf();
        this.n = n;
        this.N = new Normal(0, Math.sqrt(var));
    }

    public void newSimulation(DataBlock rslt, RandomNumberGenerator rng) {

        int dim = ssf.getStateDim();
        DataBlock a0f = DataBlock.make(dim);
        generateInitialState(a0f, rng);
        DataBlock a = DataBlock.make(dim);
        ssf.initialization().a0(a);
        DataBlock q = DataBlock.make(dynamics.getInnovationsDim());
        double y = loading.ZX(0, a);
        if (error != null) {
            y += generateMeasurementRandom(0, rng);
        }
        DoubleSeqCursor.OnMutable cursor = rslt.cursor();
        cursor.setAndNext(y);
        for (int i = 1; i < rslt.length(); ++i) {
            dynamics.TX(i - 1, a);
            if (dynamics.hasInnovations(i - 1)) {
                generateTransitionRandoms(q, rng);
                dynamics.addSU(i - 1, a, q);
            }
            y = loading.ZX(i, a);
            if (error != null) {
                y += generateMeasurementRandom(i, rng);
            }
            cursor.setAndNext(y);
        }
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
        SymmetricMatrix.lcholesky(LA, 1e-9);

    }

    private void generateTransitionRandoms(DataBlock u, RandomNumberGenerator rng) {
        fillRandoms(u, rng);
    }

    private double generateMeasurementRandom(int pos, RandomNumberGenerator rng) {
        double lh = lh(pos);
        return lh == 0 ? 0 : lh * random(rng);
    }

    private void generateInitialState(DataBlock a, RandomNumberGenerator rng) {
        fillRandoms(a, rng);
        LowerTriangularMatrix.Lx(LA, a);
    }

}
