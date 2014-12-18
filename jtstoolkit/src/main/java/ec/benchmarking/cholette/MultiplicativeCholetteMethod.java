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
@Development(status = Development.Status.Preliminary)
public class MultiplicativeCholetteMethod {

    private ISummation sum_;
    private IVariance var_;
    private double[] x_;
    private Matrix xVar_;

    /**
     * 
     * @param var
     * @param sum
     */
    public MultiplicativeCholetteMethod(IVariance var, ISummation sum) {
        sum_ = sum;
        var_ = var;
    }

    /**
     * 
     * @return
     */
    public double[] getResult() {
        return x_;
    }

    /**
     * 
     * @return
     */
    public Matrix getVar() {
        return xVar_;
    }

    /**
     * 
     * @param y
     * @param z
     * @return
     */
    public boolean process(DataBlock y, DataBlock z) {
        try {
            // (S V S' + W)^-1
            Matrix svs = SymmetricMatrix.inverse(sum_.BAB(var_));
            // S 1
            double[] One = new double[sum_.dim()];
            DataBlock C = new DataBlock(One);
            C.set(1);
            double[] sone = new double[sum_.sdim()];
            for (int i = 0; i < sone.length; ++i) {
                sone[i] = sum_.Btz(i, C);
            }
            double h = SymmetricMatrix.quadraticForm(svs, sone);

            // Y - Sq (aggregated discrepancies...)
            double[] r = new double[y.getLength()];
            y.copyTo(r, 0);
            x_ = new double[sum_.dim()];
            if (z.getLength() > 0) {
                z.copyTo(x_, 0);
                for (int i = 0; i < r.length; ++i) {
                    r[i] -= sum_.Btz(i, z);
                }
            }
            DataBlock tmp = new DataBlock(sum_.sdim());
            tmp.product(svs.rows(), new DataBlock(r));

            double s = 0;
            for (int i = 0; i < sum_.dim(); ++i) {
                s += sum_.Bx(i, tmp);
            }
            double m = -s / h;

            // q - m
            for (int i = 0; i < x_.length; ++i) {
                x_[i] -= m;
            }

            // Y - S(q-m) (aggregated discrepancies...)
            y.copyTo(r, 0);
            DataBlock XM = new DataBlock(x_);
            if (z.getLength() > 0) {
                for (int i = 0; i < r.length; ++i) {
                    r[i] -= sum_.Btz(i, XM);
                }
            } else {
                C.set(m);
                for (int i = 0; i < r.length; ++i) {
                    r[i] += sum_.Btz(i, C);
                }
            }
            tmp.product(svs.rows(), new DataBlock(r));

            double[] xtmp = new double[x_.length];
            for (int i = 0; i < xtmp.length; ++i) {
                xtmp[i] = sum_.Bx(i, tmp);
            }
            for (int i = 0; i < xtmp.length; ++i) {
                double vs = 0;
                for (int j = 0; j < xtmp.length; ++j) {
                    vs += xtmp[j] * var_.var(i, j);
                }
                x_[i] += vs;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }

    }
}
