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
package demetra.arima.regarima;

import demetra.arima.IArimaModel;
import demetra.arima.estimation.IArmaFilter;
import demetra.arima.internal.KalmanFilter;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.data.LogSign;
import demetra.design.Development;
import demetra.leastsquares.IQRSolver;
import demetra.likelihood.ConcentratedLikelihood;
import demetra.linearmodel.LinearModel;
import demetra.maths.matrices.IQRDecomposition;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.matrices.UpperTriangularMatrix;
import demetra.maths.matrices.internal.Householder;


/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class ConcentratedLikelihoodEstimation {


    private final IArmaFilter filter;
    private final IQRDecomposition qr;
    private boolean scaling = false;
    private ConcentratedLikelihood cll;
     private DoubleSequence el, bmissing, vmissing;

    /**
     *
     * @param filter
     * @param solver
     */
    public ConcentratedLikelihoodEstimation(IArmaFilter filter, final IQRDecomposition qr) {
        this.filter = filter == null ?new KalmanFilter(true) :filter ;
        this.qr= qr== null ? new Householder() : qr;
    }

     public void setScaling(boolean scaling) {
        this.scaling = scaling;
    }

    public boolean isScaling() {
        return scaling;
    }

    /**
     *
     * @param <S>
     * @param model
     * @return
     */
    public <S extends IArimaModel> boolean estimate(final RegArimaModel<S> model) {
        RegArmaModel dmodel = model.differencedModel();
        return estimate(dmodel, model.getDifferencingOrder(), model.missing());
    }

    /**
     *
     * @param dmodel
     * @param d
     * @param missings
     * @param arma
     * @return
     */
    public boolean estimate(RegArmaModel dmodel, int d, int[] missings) {
        DoubleSequence dy = dmodel.getLinearModel().getY();
        int n = dy.length();
        int nl = filter.prepare(dmodel.getArma(), n);
        try {
            return process(dmodel.getLinearModel(), nl, d, missings);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     *
     * @return
     */
    public ConcentratedLikelihood getLikelihood() {
        return cll;
    }

    /**
     *
     * @return
     */
    public DoubleSequence getResiduals() {
        return el;
    }

    public DoubleSequence getMissingEstimates() {
        return bmissing;
    }

    public DoubleSequence getMissingEstimatesUnscaledVariance() {
        return vmissing;
    }

    private boolean process(LinearModel lm, int nl, int nd, int[] missings) {
        DataBlock y=DataBlock.of(lm.getY());
        int n = y.length();
        double[] factors = null;
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
        Matrix x=lm.variables();
        int nx = lm.getVariablesCount();
        Matrix xl;
        if (nx > 0) {
            if (scaling) {
                factors = new double[nx];
                for (int i = 0; i < nx; ++i) {
                    DataBlock cur = x.column(i);
                    double xn = cur.norm2();
                    if (xn != 0) {
                        double w = n / xn;
                        factors[i] = w;
                        cur.mul(w);
                    } else {
                        factors[i] = 1;
                    }
                }
            }
            xl = Matrix.make(nl, x.getColumnsCount());
            DataBlockIterator xcols = x.columnsIterator();
            DataBlockIterator xlcols = xl.columnsIterator();
            while (xcols.hasNext()) {
                filter.apply(xcols.next(), xlcols.next());
            } 

            qr.decompose(xl);
            if (qr.rank() == 0) {
                double ssqerr = yl.ssq();
                double ldet = filter.getLogDeterminant();
               cll= ConcentratedLikelihood.likelihood(n)
                        .logDeterminant(ldet)
                        .ssqErr(ssqerr)
                        .residuals(yl)
                        .build();
                if (scaling) {
                    cll=cll.rescale(yfactor, null);
                }
                el = cll.e();
                return true;
            } else {
                DataBlock b = DataBlock.make(qr.rank());
                DataBlock res = DataBlock.make(nl - qr.rank());
                qr.leastSquares(yl, b, res);
                Matrix R = qr.r(false);
                double ssqerr = res.ssq();
                int nm = missings == null ? 0 : missings.length;
                double ldet = filter.getLogDeterminant();

                cll=ConcentratedLikelihood.likelihood(n)
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
                    cll=cll.rescale(yfactor, factors);
                    rel.div(yfactor);
                }
                el = rel;
                if (nm > 0){
                    bmissing=DoubleSequence.of(b.extract(0, nm));
                    vmissing=DoubleSequence.of(cll.unscaledCovariance().diagonal().extract(0, nm));
                    cll=cll.correctForMissing(nm);
                }
                return true;
            }
        } else {
            double ssqerr = yl.ssq();
            double ldet = filter.getLogDeterminant();
            cll=ConcentratedLikelihood.likelihood(n)
                    .ssqErr(ssqerr)
                    .logDeterminant(ldet)
                    .residuals(yl)
                    .build();
            if (scaling) {
                cll=cll.rescale(yfactor, null);
            }
            el = cll.e();
            return true;
        }
    }

}
