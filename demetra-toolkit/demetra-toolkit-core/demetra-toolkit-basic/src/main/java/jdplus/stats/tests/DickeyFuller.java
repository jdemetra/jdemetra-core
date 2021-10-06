/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoublesMath;
import nbbrd.design.BuilderPattern;
import demetra.math.Constants;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import jdplus.stats.AutoCovariances;

/**
 * (Augmented) Dickey-Fuller test
 * The estimated model is
 * dy(t)=d*y(t-1)[+a][+b*t+][+e1*dy(t-1)+...+ek*dy(t-k)]+eps
 * The class contains:
 * rho = 1+d
 * ser = standard error of d (or rho)
 * test = t-stat of d, z-stat or their modified versions in philips-perron
 * pvalue = corresponding p-value (= prob[x>=test]). See DickeyFullerTable for
 * the computation of the p-value
 *
 * @author PALATEJ
 */
@lombok.Value
public class DickeyFuller {

    public static enum DickeyFullerType {

        NC, C, CT;
    }

    double rho, ser, test, pvalue;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Cointegration test
     * Computes Dickey-Fuller on e(t) where y(t)=a+b*x(t)+e(t)
     * The builder contains the residuals and additional options can be added.
     *
     * @param x
     * @param y
     * @return
     */
    public static Builder engleGranger(DoubleSeq x, DoubleSeq y) {
        if (x.allMatch(z -> Math.abs(z) < Constants.getEpsilon())
                || y.allMatch(z -> Math.abs(z) < Constants.getEpsilon())) {
            return null;
        }
        try {
            LinearModel lm = LinearModel.builder()
                    .y(y)
                    .addX(x)
                    .meanCorrection(true)
                    .build();
            LeastSquaresResults lsr = Ols.compute(lm);
            DoubleSeq e = lsr.residuals();
            return builder().data(e);
        } catch (Exception err) {
            return null;
        }
    }

    @BuilderPattern(DickeyFuller.class)
    public static class Builder {

        private int k = 0; // number of lags. 0 for simple Dickey-Fuller
        private DickeyFullerType type = DickeyFullerType.NC;
        private DoubleSeq y;
        private boolean pp;
        private boolean zstat;

        public Builder data(DoubleSeq y) {
            this.y = y;
            return this;
        }

        public Builder numberOfLags(int nlags) {
            if (k < 0) {
                throw new java.lang.IllegalArgumentException("k should be greater or equal to 0");
            }
            this.k = nlags;
            return this;
        }

        public Builder type(DickeyFullerType type) {
            this.type = type;
            return this;
        }

        public Builder phillipsPerron(boolean pp) {
            this.pp = pp;
            return this;
        }

        public Builder zstat(boolean z) {
            this.zstat = z;
            return this;
        }

        public DickeyFuller build() {
            if (k > 0 && pp) {
                throw new IllegalArgumentException("nlags should be 0 with PhilipsPerron");
            }
            int n = y.length();
            // create the model
            DoubleSeq del = DoublesMath.delta(y, 1);
            int ndata = del.length() - k;
            int ncols = k + 1;
            switch (type) {
                case C:
                    ++ncols;
                    break;
                case CT:
                    ncols += 2;
                    break;
            }
            Matrix x = Matrix.make(ndata, ncols);

            DataBlockIterator columns = x.columnsIterator();
            if (type != DickeyFullerType.NC) {
                columns.next().set(1);
            }
            if (type == DickeyFullerType.CT) {
                columns.next().set(idx -> idx);
            }
            for (int i = 1; i <= k; ++i) {
                columns.next().copy(del.extract(k - i, ndata));
            }
            columns.next().copy(y.extract(k, ndata));

            // compute the model
            DoubleSeq z = del.extract(k, ndata);
            QRSolution ls = QRSolver.fastLeastSquares(z, x);
            DoubleSeq b = DataBlock.of(ls.getB());
            double ssq = ls.getSsqErr();
            double d = b.get(ncols - 1), rho = d + 1;
            double r = Math.abs(ls.rawRDiagonal().get(ncols - 1));
            double stdev = Math.sqrt(ssq / (ndata - ncols)) / r;
            if (pp) {
                // compute the residuals
                DataBlock u = DataBlock.of(z);
                DoubleSeqCursor c = b.cursor();
                DataBlockIterator cols = x.columnsIterator();
                while (cols.hasNext()) {
                    u.addAY(-c.getAndNext(), cols.next());
                }

                int q = (int) (4 * Math.pow(ndata * .01, 2.0 / 9.0));
                double l = 0;
                for (int i = 1; i <= q; ++i) {
                    l += 2 * (1 - i / (q + 1.0)) * AutoCovariances.autoCovarianceNoMissing(u, 0.0, i);
                }
                double w = ndata / r;
                if (zstat) {
                    double stat=ndata * d - 0.5 * w * w * l;
                    return new DickeyFuller(rho, stdev, stat, DickeyFullerTable.probability(ndata, stat, type, true));
                } else {
                    double l0 = AutoCovariances.varianceNoMissing(u, 0), ll = l0 + l;
                    double stat=d / stdev * Math.sqrt(l0 / ll) - 0.5 * w * l * Math.sqrt(1 / ll);
                    return new DickeyFuller(rho, stdev, stat, DickeyFullerTable.probability(ndata, stat, type, false));
                }

            } else {
                if (zstat) {
                    double stat=ndata *d;
                    return new DickeyFuller(rho, stdev, stat, DickeyFullerTable.probability(ndata, stat, type, true));
                } else {
                    double stat=d / stdev;
                    return new DickeyFuller(rho, stdev, stat, DickeyFullerTable.probability(ndata, stat, type, false));
                }
            }
        }

    }

}
