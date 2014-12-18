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
package ec.benchmarking.denton;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.HouseholderR;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SubMatrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class DentonMethod {

    public boolean mul_ = true, mod_ = true;
    public int diff_ = 1, conv_ = 12, offset_ = 0;
    public TsAggregationType type_ = TsAggregationType.Sum;

    private void J(SubMatrix M) {
        int j = offset_;
        DataBlockIterator rows = M.rows();
        DataBlock data = rows.getData();
        do {
            switch (type_) {
                case Sum:
                case Average:
                    data.range(j, j + conv_).set(1);
                    break;
                case First:
                    data.set(j, 1);
                    break;
                case Last:
                    data.set(j + conv_ - 1, 1);
                    break;
            }
            j += conv_;
        } while (rows.next());
    }

    private Matrix D(DataBlock x) {
        Polynomial pd = UnitRoots.D(1, diff_);
        int d = pd.getDegree();
        int n = x.getLength();
        if (mul_) {
            x = x.deepClone();
            x.inv();
        }

        if (mod_) {
            Matrix D = new Matrix(n - d, n);
            for (int i = 0; i <= d; ++i) {
                if (mul_) {
                    D.subDiagonal(i).setAY(pd.get(d - i), x.drop(i, d - i));
                } else {
                    D.subDiagonal(i).set(pd.get(d - i));
                }
            }
            return D;
        } else {
            Matrix D = new Matrix(n, n);
            for (int i = 0; i <= d; ++i) {
                if (mul_) {
                    D.subDiagonal(-i).setAY(pd.get(i), x.drop(0, i));
                } else {
                    D.subDiagonal(-i).set(pd.get(i));
                }
            }
            return D;
        }
    }

    /**
     *
     * @param function
     * @param sum
     */
    public DentonMethod() {
    }

    public double[] process(IReadDataBlock highSeries, IReadDataBlock lowSeries) {

        DataBlock x = new DataBlock(highSeries), y = new DataBlock(lowSeries);
        if (type_ == TsAggregationType.Average) {
            y.mul(conv_);
        }
        int n = x.getLength();
        int ny = y.getLength();

        double xm = x.sum() / x.getLength();
        x.mul(1 / xm);

        Matrix D = D(x);

        Matrix A = new Matrix(n + ny, n + ny);

        SymmetricMatrix.XtX(D.subMatrix(), A.subMatrix(0, n, 0, n));
        J(A.subMatrix(n, n + ny, 0, n));
        Matrix B = A.clone();
        J(A.subMatrix(0, n, n, n + ny).transpose());
        B.diagonal().drop(n, 0).set(1);

        DataBlock q = new DataBlock(n + ny);
        DataBlock q0 = q.range(0, n);
        q0.copy(x);
        DataBlock q1 = q.range(n, n + ny);
        q1.product(A.subMatrix(n, n + ny, 0, n).rows(), q0);
        q1.chs();
        q1.addAY(1.0 / xm, y);

        DataBlock z = new DataBlock(n + ny);
        z.product(B.rows(), q);
        Householder qr = new Householder(true);
        qr.decompose(A);
        qr.solve(z, q);
        DataBlock rslt = q.range(0, n).deepClone();
        rslt.mul(xm);
        return rslt.getData();
    }

    public boolean isMultiplicative() {
        return mul_;
    }

    public void setMultiplicative(boolean mul) {
        this.mul_ = mul;
    }

    public boolean isModifiedDenton() {
        return mod_;
    }

    public void setModifiedDenton(boolean mod) {
        this.mod_ = mod;
    }

    public int getConversionFactor() {
        return conv_;
    }

    public void setConversionFactor(int factor) {
        conv_ = factor;
    }

    public int getDifferencingOrder() {
        return diff_;
    }

    public void setDifferencingOrder(int diff) {
        this.diff_ = diff;
    }

    public TsAggregationType getAggregationType() {
        return type_;
    }

    public void setAggregationType(TsAggregationType type) {
        type_ = type;
    }

    public int getOffset() {
        return offset_;
    }

    public void setOffset(int offset) {
        this.offset_ = offset;
    }
}
