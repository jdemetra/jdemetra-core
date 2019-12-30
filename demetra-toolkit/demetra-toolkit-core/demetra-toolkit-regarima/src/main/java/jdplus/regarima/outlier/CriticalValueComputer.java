/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.regarima.outlier;

import jdplus.data.DataBlock;
import jdplus.dstats.Normal;
import demetra.stats.ProbabilityType;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.Householder;
import java.util.function.IntToDoubleFunction;
import jdplus.linearsystem.LinearSystemSolver;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class CriticalValueComputer {

    public IntToDoubleFunction simpleComputer() {
        return len -> {
            double cv;
            if (len <= 50) {
                cv = 3.3;
            } else if (len < 450) {
                cv = 3.3 + 0.0025 * (len - 50);
            } else {
                cv = 4.3;
            }
            return cv;
        };
    }

    public IntToDoubleFunction advancedComputer() {
        return advancedComputer(0.05);
    }

    public IntToDoubleFunction advancedComputer(final double threshold) {

        return len -> {
            Normal normal = new Normal();
            if (len == 1) {
                return normal.getProbabilityInverse(threshold / 2,
                        ProbabilityType.Upper);
            }
            double n = len;
            double[] y = new double[3];
            int[] x = new int[]{2, 100, 200};
            Matrix X = Matrix.square(3);

            for (int i = 0; i < 3; ++i) {
                X.set(i, 0, 1);
                X.set(i, 2, Math.sqrt(2 * Math.log(x[i])));
                X.set(i, 1, (Math.log(Math.log(x[i])) + Math.log(4 * Math.PI))
                        / (2 * X.get(i, 2)));
            }

            y[0] = normal.getProbabilityInverse((1 + Math.sqrt(1 - threshold)) / 2,
                    ProbabilityType.Lower);
            for (int i = 1; i < 3; ++i) {
                y[i] = calcVAL(x[i], threshold);
            }
            // solve X b = y
            LinearSystemSolver.robustSolver().solve(X, DataBlock.of(y));

            double acv = Math.sqrt(2 * Math.log(n));
            double bcv = (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                    / (2 * acv);
            return y[0] + y[1] * bcv + y[2] * acv;
        };
    }

    private static double calcVAL(int nvals, double eps) {
        if (nvals == 1) {
            return 1.96; // normal distribution
        }
        double n = nvals;
        double pmod = 2 - Math.sqrt(1 + eps);
        double acv = Math.sqrt(2 * Math.log(n));
        double bcv = acv - (Math.log(Math.log(n)) + Math.log(4 * Math.PI))
                / (2 * acv);
        double xcv = -Math.log(-.5 * Math.log(pmod));
        return xcv / acv + bcv;
    }

}
