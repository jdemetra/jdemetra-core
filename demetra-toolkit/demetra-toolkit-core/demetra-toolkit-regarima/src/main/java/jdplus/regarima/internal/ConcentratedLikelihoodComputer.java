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
package jdplus.regarima.internal;

import jdplus.arima.IArimaModel;
import internal.jdplus.arima.KalmanFilter;
import jdplus.data.DataBlock;
import nbbrd.design.Immutable;
import demetra.eco.EcoException;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.math.matrices.Matrix;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.data.LogSign;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.math.matrices.decomposition.QRDecomposition;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArmaModel;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihoodComputer {

    public static final double RCOND = 1e-12;
    
    public static Builder builder(){return new Builder();}

    public static class Builder {

        private ArmaFilter filter;
        private double rcond = RCOND;
        private boolean xfixed;
        private boolean fullResiduals = false;

        public Builder filter(ArmaFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder rankCondition(double rcond) {
            this.rcond = rcond;
            return this;
        }

        public Builder noPivoting(boolean xfixed) {
            this.xfixed = xfixed;
            return this;
        }

        /**
         * Remark: if we use pivoting, we shall use full residuals (otherwise,
         * the optimization procedure will get into trouble)
         *
         * @param full
         * @return
         *
         */
        public Builder fullResiduals(boolean full) {
            this.fullResiduals = full;
            return this;
        }

        public ConcentratedLikelihoodComputer build() {
            return new ConcentratedLikelihoodComputer(filter, rcond, xfixed, fullResiduals || !xfixed);
        }

    }

    private final ArmaFilter filter;
    private final double rcond;
    private final boolean xfixed;
    private final boolean fullResiduals;

    public static final ConcentratedLikelihoodComputer DEFAULT_COMPUTER = new ConcentratedLikelihoodComputer(null, RCOND, true, false);
    public static final ConcentratedLikelihoodComputer DEFAULT_FULL_COMPUTER = new ConcentratedLikelihoodComputer(null, RCOND, true, true);

    public ConcentratedLikelihoodComputer(final ArmaFilter filter, double rcond, boolean xfixed, boolean fullResiduals) {
        this.filter = filter;
        this.rcond = rcond;
        this.xfixed = xfixed;
        this.fullResiduals = fullResiduals;
    }

    public <M extends IArimaModel> ConcentratedLikelihoodWithMissing compute(RegArimaModel<M> model) {
        return compute(model.differencedModel());
    }

    public <M extends IArimaModel> ConcentratedLikelihoodWithMissing compute(RegArmaModel<M> dmodel) {
        DoubleSeq dy = dmodel.getY();
        int n = dy.length();
        Matrix x = dmodel.getX();
        int nx = x.getColumnsCount();
        ArmaFilter curFilter = filter == null ? new KalmanFilter(nx > 0) : filter;
        int nl = curFilter.prepare(dmodel.getArma(), n);
        try {
            return process(curFilter, dmodel.getY(), dmodel.getX(), nl, dmodel.getMissingCount());
        } catch (Exception ex) {
            throw new EcoException(EcoException.GLS_FAILED);
        }

    }

    private <M extends IArimaModel> ConcentratedLikelihoodWithMissing process(ArmaFilter curFilter, DoubleSeq dy, Matrix x, int nl, int nm) {

        DataBlock y = DataBlock.of(dy);
        int n = y.length();
        DataBlock yl = DataBlock.make(nl);
        curFilter.apply(y, yl);
        int nx = x.getColumnsCount();
        Matrix xl;
        if (nx > 0) {
            xl = Matrix.make(nl, nx);
            for (int i = 0; i < nx; ++i) {
                curFilter.apply(x.column(i), xl.column(i));
            }
            HouseholderWithPivoting hous = new HouseholderWithPivoting();
            QRDecomposition qr = hous.decompose(xl, xfixed ? nx : nm);
            QRSolution ls = QRSolver.leastSquares(qr, yl, rcond);
            ConcentratedLikelihoodWithMissing cll;
            if (xfixed && ls.rank() != nx) {
                throw new EcoException(EcoException.GLS_FAILED);
            }
            if (ls.rank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = curFilter.getLogDeterminant();
                cll = ConcentratedLikelihoodWithMissing.builder()
                        .ndata(n)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(yl)
                        .build();
                return cll;
            } else {
                double ssqerr = ls.getSsqErr();
                double ldet = curFilter.getLogDeterminant();
                // correction for missing
                if (nm > 0) {
                    double corr = LogSign.of(qr.rawRdiagonal().extract(0, nm)).getValue();
                    ldet += 2 * corr;
                }

                DoubleSeq e;
                if (fullResiduals) {
                    DoubleSeqCursor b = ls.getB().cursor();
                    for (int i = 0; i < nx; ++i) {
                        yl.addAY(-b.getAndNext(), xl.column(i));
                    }
                    e = yl.unmodifiable();
                } else {
                    e = ls.getE();
                }

                Matrix bvar = ls.unscaledCovariance();
                DoubleSeq b = ls.getB();
                cll = ConcentratedLikelihoodWithMissing.builder()
                        .ndata(n)
                        .nmissing(nm)
                        .coefficients(b)
                        .unscaledCovariance(bvar)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(e)
                        .build();
                DataBlock rel = yl.deepClone();
                DoubleSeqCursor cursor = b.cursor();
                for (int i = 0; i < nx; ++i) {
                    rel.addAY(-cursor.getAndNext(), xl.column(i));
                }
                return cll;
            }
        } else {
            double ssqerr = yl.ssq();
            double ldet = curFilter.getLogDeterminant();
            ConcentratedLikelihoodWithMissing cll = ConcentratedLikelihoodWithMissing.builder()
                    .ndata(n)
                    .ssqErr(ssqerr)
                    .logDeterminant(ldet)
                    .residuals(yl)
                    .build();
            return cll;
        }
    }

}
