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
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.RegArmaModel;
import jdplus.leastsquares.QRSolver;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import jdplus.leastsquares.QRSolution;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.UpperTriangularMatrix;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class ExactSingleOutlierDetector<T extends IArimaModel> extends SingleOutlierDetector<T> {

    private final ArmaFilter filter;
    private final ResidualsComputer resComputer;
    private Matrix U, Xl;
    private int[] pivot;
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

            QRSolution ls = QRSolver.robustLeastSquares(Yl, Xl);
            if (ls.rank() != regs.getColumnsCount()) {
                return false;
            }
            pivot = ls.pivot();
            U = ls.rawR();

            // z = yl'*Xl
            int nx = Xl.getColumnsCount();
            z = new double[nx];
            for (int i = 0; i < nx; ++i) {
                z[i] = Yl.dot(Xl.column(pivot[i]));
            }
            // z = z U^-1
            UpperTriangularMatrix.solvexU(U, DataBlock.of(z));

            DoubleSeq B = ls.getB();
            b = B.toArray();

            // calcMAD(E);
            DataBlock e = DataBlock.of(model.getY());

            // b are in the right order
            DoubleSeqCursor coeff = ls.getB().cursor();
            rcols.begin();
            while (rcols.hasNext()) {
                e.addAY(-coeff.getAndNext(), rcols.next());
            }
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

        int nx = Xl == null ? 0 : Xl.getColumnsCount();

        for (int i = lbound; i < ubound; ++i) {
            if (isAllowed(i, idx)) {
                O = DataBlock.of(od, len - i, 2 * len - d - i, 1);
                // ol
                DataBlock Ol = DataBlock.make(n);
                filter.apply(O, Ol);
                double xx = Ol.ssq(), xy = Ol.dot(Yl);

                if (U != null) {
                    // w=ol*Xl
                    double[] w = new double[b.length];
                    for (int q = 0; q < nx; ++q) {
                        w[q] = Ol.dot(Xl.column(pivot[q]));
                    }
                    DataBlock a = DataBlock.of(w);
                    // a=wU^-1
                    UpperTriangularMatrix.solvexU(U, a);
                    // q = l'A^{-1}l
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
        U = null;
        Xl = null;
        b = null;
        z = null;
    }
}
