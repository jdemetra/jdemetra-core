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

package ec.benchmarking.cholette;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Summation implements ISummation {

    private int beg_, end_, conv_, n_;

    /**
     * 
     * @param n
     * @param iconv
     */
    public Summation(int n, int iconv) {
        beg_ = 0;
        n_ = n;
        conv_ = iconv;
        end_ = n - n % iconv;
    }

    /**
     * 
     * @param var
     * @return
     */
    public Matrix BAB(IVariance var) {
        int m = sdim();
        Matrix M = new Matrix(m, m);
        for (int r = 0; r < m; ++r) {
            for (int c = 0; c <= r; ++c) {
                double s = 0;
                int R0 = beg_ + conv_ * r;
                int C0 = beg_ + conv_ * c;
                for (int i = R0; i < R0 + conv_; ++i) {
                    for (int j = C0; j < C0 + conv_; ++j) {
                        s += var.var(i, j);
                    }
                }
                M.set(r, c, s);
            }
        }
        SymmetricMatrix.fromLower(M);
        return M;
    }

    /**
     * 
     * @param i
     * @param z
     * @return
     */
    public double Btz(int i, DataBlock z) {
        int i0 = beg_ + i * conv_;
        DataBlock x = z.range(i0, i0 + conv_);
        return x.sum();
    }

    /**
     * 
     * @param i
     * @param x
     * @return
     */
    public double Bx(int i, DataBlock x) {
        if (i < beg_ || i >= end_) {
            return 0;
        }
        return x.get((i - beg_) / conv_);
    }

    /**
     * 
     * @return
     */
    public int dim() {
        return n_;
    }

    /**
     * 
     * @return
     */
    public int sdim() {
        return (end_ - beg_) / conv_;
    }
}
