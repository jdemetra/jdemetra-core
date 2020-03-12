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
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArmaModel;
import jdplus.leastsquares.QRSolver;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.leastsquares.QRSolution;
import static jdplus.math.matrices.GeneralMatrix.transpose;
import jdplus.math.matrices.Matrix;

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
    private double[] yl, b, z;
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
            //  yl
            yl = new double[n];
            DataBlock Yl = DataBlock.of(yl);
            filter.apply(model.getY(), Yl);

            // Xl
            Matrix regs = model.getX();
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
            if (ls.rank() != regs.getColumnsCount()) {
                return false;
            }

            // calcMAD(E);
            DataBlock e = DataBlock.of(model.getY());

            DoubleSeqCursor coeff = ls.getB().cursor();
            rcols.begin();
            while (rcols.hasNext()) {
                e.addAY(-coeff.getAndNext(), rcols.next());
            }
            mad = getStandardDeviationComputer().compute((filter(e)));

            int nx = Xl.getColumnsCount();
            DoubleSeq B = ls.getB();
            b = B.toArray();
            z = new double[nx];

            // L (no pivoting !)
            L = transpose(ls.rawR());

            // z = Xl'*yl
            drcols.begin();
            for (int i = 0; i < Xl.getColumnsCount(); ++i) {
                z[i] = drcols.next().dot(Yl);
            }
            // z = L^-1*z
            LowerTriangularMatrix.solveLx(L, DataBlock.of(z));

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
            if (isAllowed(i, idx)) {
                O = DataBlock.of(od, len - i, 2 * len - d - i, 1);
                // ol
                DataBlock Ol = DataBlock.make(n);
                filter.apply(O, Ol);
                double xx = Ol.ssq(), xy = Ol.dot(Yl);

                if (L != null) {
                    // w=Xl*yl
                    double[] w = new double[b.length];
                    DataBlockIterator xcols = Xl.columnsIterator();
                    for (int q = 0; q < Xl.getColumnsCount(); ++q) {
                        w[q] = xcols.next().dot(Ol);
                    }
                    DataBlock a = DataBlock.of(w);
                    // a=L-1w
                    LowerTriangularMatrix.solveLx(L, a);
                    // q = l'A^-1l
                    double q = a.ssq();
                    //
                    double v = xx - q;
                    if (v <= 0) {
                        exclude(i, idx);
                    } else {
                        setT(i, idx, (xy - DataBlock.of(z).dot(a))
                                / (Math.sqrt(v) * mad));
                    }
                } else if (xx <= 0) {
                    exclude(i, idx);
                } else {
                    setT(i, idx, xy / (Math.sqrt(xx) * mad));
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
        z = null;
    }
}
