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
package jdplus.ssf.sts.splines;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import internal.jdplus.math.functions.gsl.interpolation.CubicSplines;
import java.util.List;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.SymmetricMatrix;

/**
 *
 * @author palatej
 */
@lombok.Getter
@lombok.Builder
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SplineData {

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class PeriodInformation {

        /**
         * Q = variance of the innovations S = Cholesky factor of Q. Q, S have
         * same size, corresponding to the number of nodes (-1) (constant
         * between years) Z = loadings The number of rows in Z corresponds to
         * the number of obs. Could vary between cycles
         */
        FastMatrix Q, S, Z;
    }

    public static SplineData of(SplineDefinition definition, int nperiods) {
        Builder builder = SplineData.builder();

        DoubleSeq nodes = definition.nodes();
        int dim = nodes.length() - 1;

        CubicSplines.Spline[] splines = new CubicSplines.Spline[dim];
        double[] xi = new double[dim + 1];
        nodes.copyTo(xi, 0);
        double x0 = xi[0];
        xi[dim]=x0+definition.getPeriod();
        
        for (int i = 0; i < dim; ++i) {
            double[] f = new double[dim + 1];
            if (i == 0) {
                f[0] = 1;
                f[dim] = 1;
            } else {
                f[i] = 1;
            }
            splines[i] = CubicSplines.periodic(DoubleSeq.of(xi), DoubleSeq.of(f));
        }

        double[] wstar = new double[dim];
        for (int p = 0; p < nperiods; ++p) {
            DoubleSeq obs = definition.observations(p);
            int m = obs.length();
            FastMatrix Z = FastMatrix.make(m, dim);
            for (int i = 0; i < dim; ++i) {
                DoubleSeqCursor.OnMutable cursor = Z.column(i).cursor();
                DoubleSeqCursor ocursor = obs.cursor();
                double s = 0;
                for (int j = 0; j < m; ++j) {
                    double w = splines[i].applyAsDouble(ocursor.getAndNext());
                    cursor.setAndNext(w);
                    s += w;
                }
                wstar[i] = s;
            }
            DataBlock zh = Z.column(dim - 1);
            double wh = wstar[dim - 1];
            for (int i = 0; i < dim - 1; ++i) {
                Z.column(i).addAY(-wstar[i] / wh, zh);
            }

            DataBlock W = DataBlock.of(wstar, 0, dim);
            FastMatrix Q = FastMatrix.identity(dim - 1);
            Q.addXaXt(-1 / W.ssq(), W.drop(0, 1));
            FastMatrix S = Q.deepClone();
            SymmetricMatrix.lcholesky(S, 1e-9);
            LowerTriangularMatrix.toLower(S);
            builder.info(new PeriodInformation(Q, S, Z.dropBottomRight(0, 1)));
        }
        return builder.dim(dim-1).build();
    }

    @lombok.Singular
    private final List<PeriodInformation> infos;
    private final int dim;
}
