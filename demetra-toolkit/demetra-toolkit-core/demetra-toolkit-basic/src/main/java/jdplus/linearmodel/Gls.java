/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.linearmodel;

import jdplus.data.DataBlock;
import demetra.eco.EcoException;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.data.LogSign;
import jdplus.leastsquares.QRSolution;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Gls {

    public Gls() {
    }

    public LeastSquaresResults compute(LinearModel model, Matrix cov) {

        Matrix L = cov.deepClone();
        try {
            SymmetricMatrix.lcholesky(L);
        // yl = L^-1*y <-> L*yl = y
        DataBlock yl = DataBlock.of(model.getY());
        LowerTriangularMatrix.solveLx(L, yl);

        Matrix xl = model.variables();
        LowerTriangularMatrix.solveLX(L, xl);

        QRSolution solution = QRSolver.process(yl, xl);
        Matrix R = solution.getR();
        Matrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        return LeastSquaresResults.builder(yl, xl)
                .mean(model.isMeanCorrection())
                .estimation(solution.getB(), bvar)
                .ssq(solution.getSsqErr())
                .residuals(solution.getE())
                .logDeterminant(2 * LogSign.of(L.diagonal()).getValue())
                .build();
        } catch (Exception err) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
 }

}
