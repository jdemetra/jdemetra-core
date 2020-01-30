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

import jdplus.regarima.RegArmaModel;
import jdplus.arima.IArimaModel;
import internal.jdplus.arima.KalmanFilter;
import jdplus.regarima.RegArimaModel;
import jdplus.data.DataBlock;
import demetra.design.Immutable;
import demetra.eco.EcoException;
import jdplus.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.data.LogSign;
import jdplus.leastsquares.QRSolution;
import jdplus.leastsquares.QRSolver;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
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

    private final ArmaFilter filter;
    private final double rcond;

    public static final ConcentratedLikelihoodComputer DEFAULT_COMPUTER
            = new ConcentratedLikelihoodComputer(null);

    public ConcentratedLikelihoodComputer(final ArmaFilter filter) {
        this.filter = filter == null ? new KalmanFilter(true) : filter;
        this.rcond = 1e-13;
    }

    public ConcentratedLikelihoodComputer(final ArmaFilter filter, double rcond) {
        this.filter = filter == null ? new KalmanFilter(true) : filter;
        this.rcond = rcond;
    }

    public <M extends IArimaModel> ConcentratedLikelihoodWithMissing compute(RegArimaModel<M> model) {
        return compute(model.differencedModel());
    }

    public <M extends IArimaModel> ConcentratedLikelihoodWithMissing compute(RegArmaModel<M> dmodel) {
        DoubleSeq dy = dmodel.getY();
        int n = dy.length();
        int nl = filter.prepare(dmodel.getArma(), n);
        try {
            return process(dmodel.getY(), dmodel.getX(), nl, dmodel.getMissingCount());
        } catch (Exception ex) {
            throw new EcoException(EcoException.GLS_FAILED);
        }

    }

    private <M extends IArimaModel> ConcentratedLikelihoodWithMissing process(DoubleSeq dy, Matrix x, int nl, int nm) {

        DataBlock y = DataBlock.of(dy);
        int n = y.length();
        DataBlock yl = DataBlock.make(nl);
        filter.apply(y, yl);
        int nx = x.getColumnsCount();
        Matrix xl;
        if (nx > 0) {
            xl = Matrix.make(nl, nx);
            for (int i = 0; i < nx; ++i) {
                filter.apply(x.column(i), xl.column(i));
            }

            HouseholderWithPivoting hous = new HouseholderWithPivoting();
            QRDecomposition qr = hous.decompose(xl, nm);
            QRSolution ls = QRSolver.leastSquares(qr, yl, 1e-13);
            ConcentratedLikelihoodWithMissing cll;
            if (ls.rank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = filter.getLogDeterminant();
                cll = ConcentratedLikelihoodWithMissing.builder()
                        .ndata(n)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(yl)
                        .build();
                return cll;
            } else {
                double ssqerr = ls.getSsqErr();
                double ldet = filter.getLogDeterminant();
                // correction for missing
                if (nm > 0) {
                    double corr = LogSign.of(qr.rawRdiagonal().extract(0, nm)).getValue();
                    ldet += 2 * corr;
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
                        .residuals(ls.getE())
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
            double ldet = filter.getLogDeterminant();
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
