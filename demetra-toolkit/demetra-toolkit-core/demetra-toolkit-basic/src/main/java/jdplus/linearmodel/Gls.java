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
import lombok.NonNull;
import jdplus.leastsquares.internal.AdvancedQRSolver;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.UpperTriangularMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import demetra.data.LogSign;
import jdplus.maths.matrices.LowerTriangularMatrix;
import nbbrd.service.ServiceProvider;
import jdplus.leastsquares.QRSolver;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class Gls {

    private static AtomicReference<Supplier<QRSolver>> QR_FACTORY = new AtomicReference<>(()
            -> AdvancedQRSolver.builder(new Householder()).build());

    public static void setDefaultSolver(Supplier<QRSolver> factory) {
        QR_FACTORY.set(factory);
    }

    private final QRSolver solver;

    public Gls() {
        solver = QR_FACTORY.get().get();
    }

    public Gls(@NonNull final QRSolver solver) {
        this.solver = solver;
    }

    public LeastSquaresResults compute(LinearModel model, FastMatrix cov) {

        CanonicalMatrix L = cov.deepClone();
        try {
            SymmetricMatrix.lcholesky(L);
        } catch (Exception err) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
        // yl = L^-1*y <-> L*yl = y
        DataBlock yl = DataBlock.of(model.getY());
        LowerTriangularMatrix.rsolve(L, yl);

        FastMatrix xl = model.variables();
        LowerTriangularMatrix.rsolve(L, xl);

        if (!solver.solve(yl, xl)) {
            throw new EcoException(EcoException.GLS_FAILED);
        }
        FastMatrix R = solver.R();
        CanonicalMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                .inverse(R));
        return LeastSquaresResults.builder(yl, xl)
                .mean(model.isMeanCorrection())
                .estimation(solver.coefficients(), bvar)
                .ssq(solver.ssqerr())
                .residuals(solver.residuals())
                .logDeterminant(2 * LogSign.of(L.diagonal()).getValue())
                .build();
    }

}
