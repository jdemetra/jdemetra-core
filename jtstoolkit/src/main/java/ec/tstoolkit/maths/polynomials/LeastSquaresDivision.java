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

package ec.tstoolkit.maths.polynomials;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.MatrixException;

/**
 *
 * @author Jean Palate
 */
public class LeastSquaresDivision {

    public static final double EPS = 1e-9;
    private double err_;
    private double[] coeff;

    public boolean divide(Polynomial num, Polynomial denom) {
        try {
            err_ = 0;
            DataBlock N = new DataBlock(num);
            DataBlock D = new DataBlock(denom);
            int n = N.getLength(), d = D.getLength();
            if (d > n) {
                return false;
            }
            int q = n - d + 1;
            coeff = new double[q];
            Matrix m = new Matrix(n, q);
            DataBlockIterator columns = m.columns();
            DataBlock column = columns.getData();
            int c = 0;
            do {
                column.range(c, c + d).copy(D);
                ++c;
            } while (columns.next());
            Householder qr = new Householder(false);
            qr.decompose(m);
            DataBlock E = new DataBlock(d - 1);
            qr.leastSquares(N, new DataBlock(coeff), E);
            this.err_ = E.nrm2() / d;
            return true;
        } catch (MatrixException err) {
            return false;
        }
    }

    public Polynomial getQuotient() {
        return Polynomial.of(coeff);
    }

    public double getError() {
        return err_;
    }

    public boolean isExact() {
        return err_ < EPS;
    }
}
