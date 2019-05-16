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
package demetra.linearmodel;

import demetra.data.analysis.WindowFunction;
import jdplus.maths.matrices.SymmetricMatrix;
import demetra.stats.RobustCovarianceComputer;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class RobustCovarianceEstimators {

    public CanonicalMatrix hac(final LinearModel model, final DoubleSeq olsCoefficients, final WindowFunction w, final int truncationLag) {
        CanonicalMatrix x = model.variables();
        DoubleSeq u = model.calcResiduals(olsCoefficients);
        CanonicalMatrix xx = SymmetricMatrix.XtX(x);
        int n = x.getRowsCount();
        xx.div(n);
        CanonicalMatrix ixx = SymmetricMatrix.inverse(xx);
        // multiply the columns of x by e
        x.applyByColumns(c -> c.apply(u, (a, b) -> a * b));
        CanonicalMatrix phi = RobustCovarianceComputer.covariance(x, w, truncationLag);

        // sandwich estimator
        return sandwich(phi, ixx, n);
    }

    public CanonicalMatrix sandwich(FastMatrix meat, FastMatrix bread, int n) {
        CanonicalMatrix omega = SymmetricMatrix.XtSX(meat, bread);
        omega.div(n);
        return omega;
    }
    
    public CanonicalMatrix hc(final LinearModel model, final DoubleSeq olsCoefficients, final IntToDoubleFunction w) {

        CanonicalMatrix x = model.variables();
        DoubleSeq u = model.calcResiduals(olsCoefficients);
        CanonicalMatrix xx = SymmetricMatrix.XtX(x);
        int n = x.getRowsCount();
        xx.div(n);
        CanonicalMatrix ixx = SymmetricMatrix.inverse(xx);
        // multiply the columns of x by e
        CanonicalMatrix phi = CanonicalMatrix.square(x.getColumnsCount());
        for (int i = 0; i < n; ++i) {
            double z=w.applyAsDouble(i);
            phi.addXaXt(z*z, x.row(i));
        }
        phi.div(n);
        // sandwich estimator
        return sandwich(phi, ixx, n);
    }
}
