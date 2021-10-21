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
package jdplus.stats;

import demetra.stats.AutoCovariances;
import jdplus.data.analysis.WindowFunction;
import jdplus.math.matrices.SymmetricMatrix;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.GeneralMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class RobustCovarianceComputer {

    /**
     * Computes a robust covariance estimate of X 
     * Cov = 1/n sum(w(k)x(0...n-k-1;)'x(k...n-1;)), k in ]-truncationLag, truncationLag[
     *
     * @param x Input matrix
     * @param winFunction Window function
     * @param truncationLag Truncation lag (excluded from the computation)
     * @return
     */
    public Matrix covariance(Matrix x, WindowFunction winFunction, int truncationLag) {
        DoubleUnaryOperator w = winFunction.window();
        int n = x.getRowsCount(), nx = x.getColumnsCount();
        Matrix s = SymmetricMatrix.XtX(x);
        s.mul(w.applyAsDouble(0));
        double q = 1+truncationLag;
        for (int l = 1; l <= truncationLag; ++l) {
            double wl = w.applyAsDouble(l / q);
            Matrix m = x.extract(0, n - l, 0, nx);
            Matrix ml = x.extract(l, n - l, 0, nx);
            Matrix ol=GeneralMatrix.AtB(m, ml);
            s.addAY(wl, ol);
            s.addAYt(wl, ol);
        }
        s.div(n);
        return s;
    }

    public double covariance(DoubleSeq x, WindowFunction winFunction, int truncationLag) {
        DoubleUnaryOperator w = winFunction.window();
        DoubleSeq y=DoublesMath.removeMean(x);
        IntToDoubleFunction acf = AutoCovariances.autoCovarianceFunction(y, 0);
        double s = acf.applyAsDouble(0);
        double q = 1+truncationLag;
        for (int l = 1; l <= truncationLag; ++l) {
            double wl = w.applyAsDouble(l / q);
            s += 2*wl * acf.applyAsDouble(l);
        }
        return s;
    }

}
