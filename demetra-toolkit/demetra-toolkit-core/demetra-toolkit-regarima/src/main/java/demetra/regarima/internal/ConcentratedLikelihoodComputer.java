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
package demetra.regarima.internal;

import demetra.regarima.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.arima.internal.KalmanFilter;
import demetra.regarima.RegArimaModel;
import jdplus.data.DataBlock;
import demetra.design.Immutable;
import demetra.eco.EcoException;
import demetra.likelihood.ConcentratedLikelihoodWithMissing;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.decomposition.Householder;
import demetra.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.decomposition.QRDecomposition;
import demetra.maths.matrices.Matrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class ConcentratedLikelihoodComputer {

    private final ArmaFilter filter;
    private final QRDecomposition qr;
    private final boolean scaling;

    public static final ConcentratedLikelihoodComputer DEFAULT_COMPUTER
            = new ConcentratedLikelihoodComputer(null, null, true);

    public ConcentratedLikelihoodComputer(final ArmaFilter filter, final QRDecomposition qr, final boolean scaling) {
        this.filter = filter == null ? new KalmanFilter(true) : filter;
        this.qr = qr == null ? new Householder() : qr;
        this.scaling = scaling;
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
        double yfactor = 1;
        if (scaling) {
            double yn = y.norm2();
            if (yn != 0) {
                yfactor = n / yn;
                y.mul(yfactor);
            }
        }
        DataBlock yl = DataBlock.make(nl);
        filter.apply(y, yl);
        int nx = x.getColumnsCount();
        CanonicalMatrix xl;
        if (nx > 0) {
            xl = CanonicalMatrix.make(nl, nx);
            for (int i = 0; i < nx; ++i) {
                filter.apply(x.column(i), xl.column(i));
            }

            qr.decompose(xl);
            ConcentratedLikelihoodWithMissing cll;
            if (qr.rank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = filter.getLogDeterminant();
                cll = ConcentratedLikelihoodWithMissing.builder()
                        .ndata(n)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(yl)
                        .build();
                if (scaling) {
                    cll = cll.rescale(yfactor, null);
                }
                return cll;
            } else {
                DataBlock b = DataBlock.make(qr.rank());
                DataBlock res = DataBlock.make(nl - qr.rank());
                qr.leastSquares(yl, b, res);
                CanonicalMatrix R = qr.r(false);
                double ssqerr = res.ssq();
                double ldet = filter.getLogDeterminant();
                CanonicalMatrix bvar = SymmetricMatrix.UUt(UpperTriangularMatrix
                        .inverse(R));

                cll = ConcentratedLikelihoodWithMissing.builder()
                        .ndata(n)
                        .nmissing(nm)
                        .coefficients(b)
                        .unscaledCovariance(bvar)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(res)
                        .build();
                DataBlock rel = yl.deepClone();
                for (int i = 0; i < nx; ++i) {
                    rel.addAY(-b.get(i), xl.column(i));
                }
                if (scaling) {
                    cll = cll.rescale(yfactor, null);
                    rel.div(yfactor);
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
            if (scaling) {
                cll = cll.rescale(yfactor, null);
            }
            return cll;
        }
    }

}
