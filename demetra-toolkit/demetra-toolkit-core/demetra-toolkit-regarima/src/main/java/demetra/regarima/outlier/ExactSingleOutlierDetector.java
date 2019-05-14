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
package demetra.regarima.outlier;

import demetra.arima.IArimaModel;
import demetra.arima.estimation.ResidualsComputer;
import demetra.arima.internal.AnsleyFilter;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.design.Development;
import demetra.leastsquares.QRSolvers;
import demetra.linearmodel.LinearModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArmaModel;
import demetra.leastsquares.QRSolver;
import javax.annotation.Nonnull;
import demetra.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.CanonicalMatrix;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class ExactSingleOutlierDetector<T extends IArimaModel> extends SingleOutlierDetector<T> {

    public static class Builder {

        private ArmaFilter filter = null;
        private ResidualsComputer resComputer = null;
        private RobustStandardDeviationComputer madComputer = RobustStandardDeviationComputer.mad();

        public Builder armaFilter(@Nonnull ArmaFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder robustStandardDeviationComputer(@Nonnull RobustStandardDeviationComputer mad) {
            this.madComputer = mad;
            return this;
        }

        public Builder residualsComputer(@Nonnull ResidualsComputer res) {
            this.resComputer = res;
            return this;
        }

        public ExactSingleOutlierDetector build() {
            ArmaFilter f=filter == null ? new AnsleyFilter() : filter;
            ResidualsComputer r = resComputer == null ? ResidualsComputer.defaultComputer(f) : resComputer;
            return new ExactSingleOutlierDetector(madComputer, f, r);
        }
    }
    
    public static Builder builder(){
        return new Builder();
    }

    private ArmaFilter filter;
    private final ResidualsComputer resComputer;
    private Matrix L, Xl;
    private double[] yl, b, w;
    private int n;
    private double mad;

    /**
     *
     * @param computer
     * @param filter
     */
    private ExactSingleOutlierDetector(@Nonnull RobustStandardDeviationComputer computer, @Nonnull ArmaFilter filter, @Nonnull ResidualsComputer res) {
        super(computer);
        this.filter = filter;
        resComputer = res;
    }

    public ExactSingleOutlierDetector(RobustStandardDeviationComputer computer, ResidualsComputer resComputer, ArmaFilter filter) {
        super(computer);
        if (filter == null) {
            this.filter = new AnsleyFilter();
        } else {
            this.filter = filter;
        }
        this.resComputer = resComputer;
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
        try {
            if (getOutlierFactoriesCount() == 0 || ubound <= lbound) {
                return false;
            }
            RegArmaModel<T> dmodel = this.getRegArima().differencedModel();
            n = filter.prepare(dmodel.getArma(), dmodel.getY().length());
            if (!initialize(dmodel)) {
                return false;
            }
            for (int i = 0; i < getOutlierFactoriesCount(); ++i) {
                processOutlier(i);
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    /**
     *
     * @param model
     * @return
     */
    protected boolean initialize(RegArmaModel<T> model) {
        try {
            LinearModel lm = model.asLinearModel();
            yl = new double[n];
            DataBlock Yl = DataBlock.of(yl);
            filter.apply(model.getY(), Yl);

            Matrix regs = lm.variables();
            if (regs.isEmpty()) {
                mad = getStandardDeviationComputer().compute(filter(model.getY()));
                return true;
            }

            Xl = CanonicalMatrix.make(n, regs.getColumnsCount());
            DataBlockIterator rcols = regs.columnsIterator(), drcols = Xl.columnsIterator();
            while (rcols.hasNext()) {
                filter.apply(rcols.next(), drcols.next());
            }

            QRSolver qr = QRSolvers.fastSolver();

            if (!qr.solve(Yl, Xl)) {
                return false;
            }

            int nx = Xl.getColumnsCount();
            DoubleSeq B = qr.coefficients();
            b = B.toArray();
            w = new double[nx];
            L = qr.R().transpose();

            drcols.begin();
            for (int i = 0; i < Xl.getColumnsCount(); ++i) {
                w[i] = drcols.next().dot(Yl);
            }
            LowerTriangularMatrix.rsolve(L, DataBlock.of(w));

//	    calcMAD(E);
            DataBlock e = lm.calcResiduals(B);
            mad = getStandardDeviationComputer().compute((filter(e)));
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     *
     * @param idx
     */
    protected void processOutlier(int idx) {
        RegArimaModel<T> regArima = getRegArima();
        int len = regArima.getY().length();
        BackFilter df = regArima.arima().getNonStationaryAR();
        int d = df.getDegree();
        double[] o = new double[2 * len];
        DataBlock O = DataBlock.of(o);
        getOutlierFactory(idx).fill(len, O);
        double[] od = new double[o.length - d];
        DataBlock OD = DataBlock.of(od);
        df.apply(O, OD);
        DataBlock Yl = DataBlock.of(yl);

        for (int i = lbound; i < ubound; ++i) {
            O = DataBlock.of(od, len - i, 2 * len - d - i, 1);
            if (isAllowed(i, idx)) {
                DataBlock Ol = DataBlock.make(n);
                filter.apply(O, Ol);
                double xx = Ol.ssq(), xy = Ol.dot(Yl);

                if (L != null) {
                    double[] l = new double[b.length];
                    DataBlockIterator xcols = Xl.columnsIterator();
                    for (int q = 0; q < Xl.getColumnsCount(); ++q) {
                        l[q] = xcols.next().dot(Ol);
                    }
                    DataBlock M = DataBlock.of(l);
                    // K=A^-1*L
                    // lA * lA' * K = L
                    // l'AA^-1l = |l' * lA'^-1|
                    LowerTriangularMatrix.rsolve(L, M);
                    // q = l'A^-1l
                    double q = M.ssq();
                    //
                    double c = xx - q;
                    if (c <= 0) {
                        exclude(i, idx);
                    } else {
                        setT(i, idx, (xy - DataBlock.of(w).dot(M))
                                / (Math.sqrt(c)) / mad);
                    }
                } else if (xx <= 0) {
                    exclude(i, idx);
                } else {
                    setT(i, idx, (xy / (Math.sqrt(xx)) / mad));
                }
            }
        }
    }

    protected DoubleSeq filter(DoubleSeq res) {
        return resComputer.residuals(this.getRegArima().differencedModel().getArma(), res);
    }

    @Override
    protected void clear(boolean all) {
        super.clear(all);
        L = null;
        Xl = null;
        b = null;
        w = null;
    }
}
