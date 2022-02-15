/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.linearmodel;

import demetra.data.DoubleSeq;
import demetra.stats.StatisticalTest;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class HeteroskedasticityTest {

    public static HeteroskedasticityTest builder(LinearModel lm) {
        return new HeteroskedasticityTest(Ols.compute(lm));
    }

    public static HeteroskedasticityTest builder(LeastSquaresResults lsr) {
        return new HeteroskedasticityTest(lsr);
    }

    public static enum Type {
        BreuschPagan,
        White
    }

    private Type type = Type.BreuschPagan;
    private boolean fisher = false;
    private boolean studentized = false;
    private final LeastSquaresResults lsr;
    private LeastSquaresResults lsr2;

    private HeteroskedasticityTest(LeastSquaresResults lsr) {
        this.lsr = lsr;
    }

    public HeteroskedasticityTest studentizedResiduals(boolean studentized) {
        this.studentized = studentized;
        return this;
    }

    public HeteroskedasticityTest type(Type type) {
        this.type = type;
        return this;
    }

    public HeteroskedasticityTest fisherTest(boolean fisher) {
        this.fisher = fisher;
        return this;
    }

    public StatisticalTest build() {
        if (!lsr.isMean()) {
            throw new IllegalArgumentException("lm should contain a mean correction");
        }
        Matrix X = lsr.X();
        DoubleSeq e = studentized ? lsr.studentizedResiduals() : lsr.residuals();
        // Be careful: X contains the mean correction
        int n = X.getRowsCount(), m = X.getColumnsCount();
        LinearModel.Builder builder = LinearModel.builder()
                .y(e.fastOp(z -> z * z))
                .addX(X.extract(0, n, 1, m - 1))
                .meanCorrection(true);
        if (type == Type.White) {
            // cross products
            for (int i = 1; i < m; ++i) {
                for (int j = i; j < m; ++j) {
                    builder.addX(X.column(i).fastOp(X.column(j), (xi, xj) -> xi * xj));
                }
            }
        }
        lsr2 = Ols.compute(builder.build());
        if (fisher) {
            return lsr2.Ftest();
        } else {
            return lsr2.Khi2Test();
        }
    }
    
    public LeastSquaresResults getLeastSquaresResultsOnSquaredResiduals(){
        return lsr2;
    }

}
