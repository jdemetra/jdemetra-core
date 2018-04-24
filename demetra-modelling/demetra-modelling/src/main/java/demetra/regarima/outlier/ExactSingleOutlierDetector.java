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
import demetra.arima.IArmaFilter;
import demetra.arima.IResidualsComputer;
import demetra.arima.internal.AnsleyFilter;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.leastsquares.IQRSolver;
import demetra.linearmodel.LinearModel;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.internal.Householder;
import demetra.regarima.RegArimaModel;
import demetra.regarima.RegArmaModel;
import demetra.sarima.SarimaModel;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class ExactSingleOutlierDetector<T extends IArimaModel> extends AbstractSingleOutlierDetector<T> {

    private IArmaFilter filter;
    private final IResidualsComputer resComputer;
    private Matrix L, Xl;
    private double[] yl, b, w;
    private int n;
    private double mad;

    public ExactSingleOutlierDetector() {
        this(IRobustStandardDeviationComputer.mad());
    }

    public ExactSingleOutlierDetector(IRobustStandardDeviationComputer computer) {
        this(computer, null);
    }

    /**
     *
     * @param computer
     * @param filter
     */
    public ExactSingleOutlierDetector(IRobustStandardDeviationComputer computer, IArmaFilter filter) {
        super(computer);
        if (filter == null) {
            this.filter = new AnsleyFilter();
        } else {
            this.filter = filter;
        }
        resComputer = IResidualsComputer.defaultComputer(this.filter);
    }

    public ExactSingleOutlierDetector(IRobustStandardDeviationComputer computer, IResidualsComputer resComputer, IArmaFilter filter) {
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
            DataBlock Yl = DataBlock.ofInternal(yl);
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

            IQRSolver qr = IQRSolver.fastSolver();

            if (!qr.solve(Yl, Xl)) {
                return false;
            }

            int nx = Xl.getColumnsCount();
            DoubleSequence B = qr.coefficients();
            b = B.toArray();
            w = new double[nx];
            L = qr.R().transpose();

            drcols.begin();
            for (int i = 0; i < Xl.getColumnsCount(); ++i) {
                w[i] = drcols.next().dot(Yl);
            }
            LowerTriangularMatrix.rsolve(L, DataBlock.ofInternal(w));

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
        DataBlock O = DataBlock.ofInternal(o);
        getOutlierFactory(idx).fill(lbound + len, O);
        double[] od = new double[o.length - d];
        DataBlock OD = DataBlock.ofInternal(od);
        df.apply(O, OD);
        DataBlock Yl = DataBlock.ofInternal(yl);

        for (int i = 0; i < len; ++i) {
            O = DataBlock.ofInternal(od, len - i, 2 * len - d - i, 1);
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
                    DataBlock M = DataBlock.ofInternal(l);
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
                        setT(i, idx, (xy - DataBlock.ofInternal(w).dot(M))
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

    protected DoubleSequence filter(DoubleSequence res) {
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
