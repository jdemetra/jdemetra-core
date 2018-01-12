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
package demetra.arima.regarima;

import demetra.arima.regarima.internal.RegArmaModel;
import demetra.arima.IArimaModel;
import demetra.arima.estimation.IArmaFilter;
import demetra.arima.internal.KalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.design.Immutable;
import demetra.eco.EcoException;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.maths.matrices.decomposition.IQRDecomposition;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.Householder;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate
 */
@Immutable
public class ConcentratedLikelihoodComputer {

    private final IArmaFilter filter;
    private final IQRDecomposition qr;
    private final boolean scaling;
    
    public static final ConcentratedLikelihoodComputer DEFAULT_COMPUTER=
            new ConcentratedLikelihoodComputer(new KalmanFilter(true), new Householder(), true);

    public ConcentratedLikelihoodComputer() {
        this(null, null, false);
    }

    public ConcentratedLikelihoodComputer(final IArmaFilter filter, final IQRDecomposition qr, final boolean scaling) {
        this.filter = filter == null ? new KalmanFilter(true) : filter;
        this.qr = qr == null ? new Householder() : qr;
        this.scaling = scaling;
    }

    public <M extends IArimaModel> ConcentratedLikelihoodEstimation compute(RegArimaModel<M> model) {
        return compute(RegArmaModel.of(model));
    }

    public <M extends IArimaModel> ConcentratedLikelihoodEstimation<M> compute(RegArmaModel<M> dmodel) {
        DoubleSequence dy = dmodel.getY();
        int n = dy.length();
        int nl = filter.prepare(dmodel.getArma(), n);
        try {
            return process(dmodel.getY(), dmodel.getX(), nl, dmodel.getMissingCount());
        } catch (Exception ex) {
            throw new EcoException(EcoException.GLS_FAILED);
        }

    }

    private <M extends IArimaModel> ConcentratedLikelihoodEstimation<M> process(DoubleSequence dy, MatrixType x, int nl, int nm) {

        ConcentratedLikelihoodEstimation.Builder<M> builder = ConcentratedLikelihoodEstimation.builder();
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
        Matrix xl;
        if (nx > 0) {
            xl = Matrix.make(nl, nx);
            for (int i=0; i<nx; ++i){
                filter.apply(x.column(i), xl.column(i));
            }

            qr.decompose(xl);
            ConcentratedLikelihood cll;
            if (qr.rank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = filter.getLogDeterminant();
                cll = ConcentratedLikelihood.likelihood(n)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(yl)
                        .build();
                if (scaling) {
                    cll = cll.rescale(yfactor, null);
                }
                return builder.concentratedLogLikelihood(cll)
                        .residuals(cll.e())
                        .build();
            } else {
                DataBlock b = DataBlock.make(qr.rank());
                DataBlock res = DataBlock.make(nl - qr.rank());
                qr.leastSquares(yl, b, res);
                Matrix R = qr.r(false);
                double ssqerr = res.ssq();
                double ldet = filter.getLogDeterminant();

                cll = ConcentratedLikelihood.likelihood(n)
                        .coefficients(b)
                        .rfactor(R)
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
                if (nm > 0) {
                    return builder
                            .missingValues(
                                    DoubleSequence.of(b.extract(0, nm)),
                                    DoubleSequence.of(cll.unscaledCovariance().diagonal().extract(0, nm)))
                            .concentratedLogLikelihood(cll.correctForMissing(nm))
                            .residuals(rel)
                            .build();
                } else {
                    return builder
                            .concentratedLogLikelihood(cll)
                            .residuals(rel)
                            .build();
                }
            }
        } else {
            double ssqerr = yl.ssq();
            double ldet = filter.getLogDeterminant();
            ConcentratedLikelihood cll = ConcentratedLikelihood.likelihood(n)
                    .ssqErr(ssqerr)
                    .logDeterminant(ldet)
                    .residuals(yl)
                    .build();
            if (scaling) {
                cll = cll.rescale(yfactor, null);
            }
            return builder.concentratedLogLikelihood(cll)
                    .residuals(cll.e())
                    .build();
        }
    }

}
