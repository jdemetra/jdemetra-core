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
package jdplus.regarima.outlier;

import jdplus.arima.IArimaModel;
import jdplus.arima.estimation.ResidualsComputer;
import internal.jdplus.arima.AnsleyFilter;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.design.Development;
import jdplus.linearmodel.LinearModel;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArmaModel;
import jdplus.leastsquares.QRSolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import java.util.function.Supplier;
import jdplus.leastsquares.QRSolution;
import static jdplus.math.matrices.GeneralMatrix.transpose;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.IOutlierFactory;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class ExactSingleOutlierDetector<T extends IArimaModel> extends SingleOutlierDetector<T> {

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
     * @param res
     */
    public ExactSingleOutlierDetector(RobustStandardDeviationComputer computer, ArmaFilter filter, ResidualsComputer res) {
        super(computer == null ? RobustStandardDeviationComputer.mad() : computer);
        this.filter = filter == null ? new AnsleyFilter() : filter;
        resComputer = res == null ? ResidualsComputer.defaultComputer(this.filter) : res;
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

            Xl = Matrix.make(n, regs.getColumnsCount());
            DataBlockIterator rcols = regs.columnsIterator(), drcols = Xl.columnsIterator();
            while (rcols.hasNext()) {
                filter.apply(rcols.next(), drcols.next());
            }

            QRSolution ls = QRSolver.fastLeastSquares(Yl, Xl);

            int nx = Xl.getColumnsCount();
            DoubleSeq B = ls.getB();
            b = B.toArray();
            w = new double[nx];
            L = transpose(ls.rawR()); 

            drcols.begin();
            for (int i = 0; i < Xl.getColumnsCount(); ++i) {
                w[i] = drcols.next().dot(Yl);
            }
            LowerTriangularMatrix.solveLx(L, DataBlock.of(w));

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
        BackFilter df = regArima.arima().getNonStationaryAr();
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
                    LowerTriangularMatrix.solveLx(L, M);
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
