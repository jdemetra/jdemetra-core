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
package ec.tstoolkit.ucarima.estimation;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.StationaryTransformation;
import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.arima.estimation.IArmaFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.ElementaryTransformations;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.ucarima.UcarimaModel;

/**
 * Estimation of the components of an UCARIMA model using the formulae proposed by McElroy.
 * <br><i>See McElroy T.S.(2008), Matrix formulae for non stationary ARIMA Signal Extraction, 
 * <a href="http://www.census.gov/ts/papers/matform3.pdf"> http://www.census.gov/ts/papers/matform3.pdf</a></i>
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class McElroyEstimates {

    private UcarimaModel ucm_;
    private double[] data_;
    // (LL')=M
    // M^-1 * K'K =F
    private Matrix[] M_, F_, L_, K_, D_;
    private double[][] cmps_, fcmps_;
    private int nf_;
    private IArmaFilter[] filters_;

    private void clear() {
        M_ = null;
        F_ = null;
        L_ = null;
        K_ = null;
        cmps_ = null;
        filters_ = null;
        // forecasts
        D_ = null;
        fcmps_ = null;
    }

    /**
     *
     * @return
     */
    public UcarimaModel getUcarimaModel() {
        return ucm_;
    }

    public void setUcarimaModel(UcarimaModel ucm) {
        ucm_ = ucm;
        clear();
    }

    public void setData(IReadDataBlock data) {
        data_ = new double[data.getLength()];
        data.copyTo(data_, 0);
        clear();
    }

    public void setData(double[] data) {
        data_ = data;
        clear();
    }

    public double[] getData() {
        return data_;
    }

    public int getForecastsCount() {
        return nf_;
    }

    public void setForecastsCount(int nf) {
        if (nf != nf_) {
            nf_ = nf;
            D_ = null;
            fcmps_ = null;
        }
    }

    public double[] getComponent(int cmp) {
        calc(cmp);
        return cmps_[cmp];
    }

    public double[] getForecasts(int cmp) {
        fcalc(cmp);
        return fcmps_[cmp];
    }

    public double[] getForecasts() {
        int n = ucm_.getComponentsCount();
        fcalc(n);
        return fcmps_[n];
    }

    public double[] stdevForecasts() {
        int n = ucm_.getComponentsCount();
        return stdevForecasts(n);
    }

    public double[] stdevForecasts(int cmp) {
        fcalc(cmp);
        Matrix m = D_[cmp];
        DataBlock var = m.diagonal();
        double[] e = new double[var.getLength()];
        var.copyTo(e, 0);
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i]);
        }
        return e;
    }

    public double[] stdevEstimates(final int cmp) {
        Matrix m = M(cmp);
        DataBlock var = m.diagonal();
        double[] e = new double[var.getLength()];
        var.copyTo(e, 0);
        for (int i = 0; i < e.length; ++i) {
            e[i] = Math.sqrt(e[i]);
        }
        return e;
    }

    public Matrix M(final int cmp) {
        calc(cmp);
        if (M_[cmp] == null) {
            Matrix L = L_[cmp];
            if (L == null) {
                return null;
            }
            // M = (L*L')^-1 or LL'M = I 
            // L X = I
            // X = L' M or M L = X'
            Matrix I = Matrix.identity(L.getColumnsCount());
            LowerTriangularMatrix.rsolve(L, I.subMatrix());
            LowerTriangularMatrix.lsolve(L, I.subMatrix().transpose());
            M_[cmp] = I;
        }
        return M_[cmp];
    }

    public Matrix F(final int cmp) {
        calc(cmp);
        if (F_[cmp] == null) {
            Matrix L = L_[cmp], K = K_[cmp];
            if (L == null || K == null) {
                return null;
            }
            // F = (LL')^-1 * K'K = L'^-1*L^-1*K'K

            // compute K'K
            Matrix KK = SymmetricMatrix.XtX(K);
            // compute X=L^-1*K'K
            // LX = K'K 
            LowerTriangularMatrix.rsolve(L, KK.subMatrix());
            // compute L'^-1 * X = F or L'F = X or F' L = X' 
            LowerTriangularMatrix.lsolve(L, KK.subMatrix().transpose());

            F_[cmp] = KK;

        }
        return F_[cmp];
    }

    private void calc(int cmp) {
        if (data_ == null || ucm_ == null) {
            return;
        }
        if (M_ == null) {
            int ncmps = ucm_.getComponentsCount();
            K_ = new Matrix[ncmps];
            L_ = new Matrix[ncmps];
            M_ = new Matrix[ncmps];
            F_ = new Matrix[ncmps];
            cmps_ = new double[ncmps][];
            filters_ = new IArmaFilter[ncmps + 1];
        } else if (cmps_[cmp] != null) {
            return;
        }
        // actual computation.
        ArimaModel signal = ucm_.getComponent(cmp);
        if (signal.isNull()) {
            return;
        }
        ArimaModel noise = ucm_.getComplement(cmp);

        // differencing matrices
        int n = data_.length;
        StationaryTransformation stS = signal.stationaryTransformation();
        StationaryTransformation stN = noise.stationaryTransformation();

        Polynomial ds = stS.unitRoots.getPolynomial();
        Polynomial dn = stN.unitRoots.getPolynomial();

        Matrix DS = new Matrix(n - ds.getDegree(), n);
        Matrix DN = new Matrix(n - dn.getDegree(), n);

        double[] c = ds.getCoefficients();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DS.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }
        c = dn.getCoefficients();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DN.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }

        AnsleyFilter S = new AnsleyFilter();
        S.initialize((IArimaModel) stS.stationaryModel, n - ds.getDegree());
        filters_[cmp] = S;
        AnsleyFilter N = new AnsleyFilter();
        N.initialize((IArimaModel) stN.stationaryModel, n - dn.getDegree());

        Matrix Q = new Matrix(n, 2 * n - ds.getDegree() - dn.getDegree());
        for (int i = 0; i < n; ++i) {
            S.filter(DS.column(i), Q.row(n - i - 1).range(0, n - ds.getDegree()));
        }
        for (int i = 0; i < n; ++i) {
            N.filter(DN.column(i), Q.row(n - i - 1).drop(n - ds.getDegree(), 0));
        }
        K_[cmp] = new Matrix(Q.subMatrix(0, n, n - ds.getDegree(), Q.getColumnsCount()));
        DataBlock yd = new DataBlock(n - dn.getDegree());
        noise.getNonStationaryAR().filter(new DataBlock(data_), yd);
        DataBlock yl = new DataBlock(yd.getLength());
        N.filter(yd, yl);
        // compute K'n x yl. Don't forget: Q is arranged in reverse order !
        // should be improved to take into account the structure of K
        double[] z = new double[n];
        for (int i = 0, j = n - 1; i < n; ++i, --j) {
            z[i] = Q.row(j).drop(n - ds.getDegree(), 0).dot(yl);
        }
        // triangularize by means of Givens rotations
        ElementaryTransformations.givensTriangularize(Q.subMatrix());
        Matrix L = new Matrix(Q.subMatrix(0, n, 0, n));
        LowerTriangularMatrix.rsolve(L, z);
        LowerTriangularMatrix.lsolve(L, z);
        L_[cmp] = L;
        cmps_[cmp] = z;
        // computes M^-1, 
        // F= M^-1, 
    }

    private IArmaFilter seriesFilter() {
        int n = data_.length;
        IArimaModel model = ucm_.getModel();
        StationaryTransformation stm = model.stationaryTransformation();
        AnsleyFilter F = new AnsleyFilter();
        F.initialize((IArimaModel) stm.stationaryModel, n - stm.unitRoots.getDegree());
        return F;
    }

    private void fcalc(int cmp) {
        int ncmps = ucm_.getComponentsCount();
        boolean fs = cmp == ncmps;
        if (!fs) {
            calc(cmp);
        }
        if (fcmps_ == null) {
            fcmps_ = new double[ncmps + 1][];
            D_ = new Matrix[ncmps + 1];
        } else if (fcmps_[cmp] != null) {
            return;
        }
        // computes D
        // actual computation.
        if (fs) {
            if (filters_ == null) {
                filters_ = new IArmaFilter[ncmps + 1];
            }
            if (filters_[cmp] == null) {
                filters_[cmp] = seriesFilter();
            }
        }
        int n = data_.length;
        IArimaModel signal = fs ? ucm_.getModel() : ucm_.getComponent(cmp);
        if (signal.isNull()) {
            return;
        }
        StationaryTransformation stS = signal.stationaryTransformation();

        Polynomial ds = stS.unitRoots.getPolynomial();

        Matrix DS = new Matrix(n - ds.getDegree(), n);

        double[] c = ds.getCoefficients();
        for (int j = 0; j < c.length; ++j) {
            DataBlock d = DS.subDiagonal(j);
            d.set(c[c.length - j - 1]);
        }

        Matrix Q = new Matrix(n - ds.getDegree(), n);
        for (int i = 0; i < n; ++i) {
            filters_[cmp].filter(DS.column(i), Q.column(i));
        }
        Matrix U = new Matrix(n - ds.getDegree(), nf_);
        double[] acf = stS.stationaryModel.getAutoCovarianceFunction().values(n - ds.getDegree() + nf_);
        for (int i = 0; i < nf_; ++i) {
            U.column(i).reverse().copyFrom(acf, i + 1);
        }
        Matrix V = new Matrix(nf_, n - ds.getDegree());
        for (int i = 0; i < nf_; ++i) {
            if (!U.column(i).isZero()) {
                filters_[cmp].filter(U.column(i), V.row(i));
            }
        }
        Matrix W = V.times(Q);
        Matrix D;
        if (ds.getDegree() > 0) {
            D = new Matrix(ds.getDegree() + nf_, n);

            D.subDiagonal(n - ds.getDegree()).set(1);
            D.subMatrix(ds.getDegree(), D.getRowsCount(), 0, n).copy(W.subMatrix());
            Matrix S = new Matrix(ds.getDegree() + nf_, ds.getDegree() + nf_);
            S.diagonal().set(1);
            for (int i = 1; i <= ds.getDegree(); ++i) {
                S.subDiagonal(-i).drop(ds.getDegree() - i, 0).set(ds.get(i));
            }
            LowerTriangularMatrix.rsolve(S, D.subMatrix());
            D = new Matrix(D.subMatrix(ds.getDegree(), D.getRowsCount(), 0, n));
        } else {
            D = W;
        }
        DataBlock f = new DataBlock(nf_);
        double[] data = fs ? data_ : getComponent(cmp);
        f.product(D.rows(), new DataBlock(data));
        fcmps_[cmp] = f.getData();
        Matrix G = SymmetricMatrix.XXt(V);
        G.chs();
        G.diagonal().add(acf[0]);
        for (int i = 1; i < nf_; ++i) {
            G.subDiagonal(i).add(acf[i]);
            G.subDiagonal(-i).add(acf[i]);
        }

        if (ds.getDegree() > 0) {
            Matrix B = new Matrix(nf_, nf_);
            RationalFunction rfe = new RationalFunction(Polynomial.ONE, ds);
            double[] coeff = rfe.coefficients(nf_);
            for (int i = 0; i < nf_; ++i) {
                B.subDiagonal(-i).set(coeff[i]);
            }
            G = SymmetricMatrix.quadraticFormT(G, B);
        }
        if (!fs) {
            Matrix m = M(cmp);
            m = SymmetricMatrix.quadraticFormT(m, D);
            G.add(m);
        }
        D_[cmp] = G;
    }
}
