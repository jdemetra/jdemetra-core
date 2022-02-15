/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package jdplus.math.polynomials;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.math.Constants;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.decomposition.Householder;
import demetra.data.DoubleSeq;
import jdplus.math.linearsystem.QRLeastSquaresSolution;
import jdplus.math.linearsystem.QRLeastSquaresSolver;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 *
 * @author Jean Palate
 */
public class LeastSquaresDivision {

    public static final double EPS = Math.sqrt(Constants.getEpsilon());
    private double err;
    private double[] coeff;

    public boolean divide(Polynomial num, Polynomial denom) {
        try {
            err = 0;
            DoubleSeq N = num.coefficients();
            DoubleSeq D = denom.coefficients();
            int n = N.length(), d = D.length();
            if (d > n) {
                return false;
            }
            int q = n - d + 1;
            FastMatrix m = FastMatrix.make(n, q);
            DataBlockIterator columns = m.columnsIterator();
            int c = 0;
            while (columns.hasNext()) {
                columns.next().range(c, c + d).copy(D);
                ++c;
            }
            
            QRLeastSquaresSolution ls = QRLeastSquaresSolver.fastLeastSquares(N, m);
            this.coeff=ls.getB().toArray();
            this.err = ls.getSsqErr() / d;
            return true;
        } catch (MatrixException error) {
            return false;
        }
    }

    public Polynomial getQuotient() {
        return Polynomial.of(coeff);
    }

    public double getError() {
        return err;
    }

    public boolean isExact() {
        return err < EPS;
    }
}
