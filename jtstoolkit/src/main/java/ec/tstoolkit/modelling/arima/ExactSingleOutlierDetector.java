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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.arima.estimation.IArmaFilter;
import ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.LowerTriangularMatrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.IRobustStandardDeviationComputer;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ExactSingleOutlierDetector<T extends IArimaModel> extends AbstractSingleOutlierDetector<T> {

    private IArmaFilter m_filter;
    private Matrix m_L, m_X;
    private double[] m_yl, m_b, m_w;
    private int m_n;

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
            m_filter = new AnsleyFilter();
        } else {
            m_filter = filter;
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
        RegModel dmodel = getModel().getDModel();
        m_n = m_filter.initialize(getModel().getArma(), dmodel.getObsCount());
        if (!initialize(dmodel)) {
            return false;
        }
        for (int i = 0; i < getOutlierFactoriesCount(); ++i) {
            processOutlier(i);
        }
        return true;
    }

    /**
     * 
     * @param model
     * @return
     */
    protected boolean initialize(RegModel model) {
        try {
            m_yl = new double[m_n];
            DataBlock YL = new DataBlock(m_yl);
            m_filter.filter(model.getY(), YL);

            Matrix regs = model.variables();
            if (regs == null) {
                getStandardDeviationComputer().compute(filter(model.getY()));
                return true;
            }

            m_X = new Matrix(m_n, regs.getColumnsCount());
            DataBlockIterator rcols = regs.columns(), drcols = m_X.columns();
            DataBlock rcol = rcols.getData(), drcol = drcols.getData();
            do {
                m_filter.filter(rcol, drcol);
            } while (rcols.next() && drcols.next());

            Householder qr = new Householder(true);
            qr.decompose(m_X);
            int nx = m_X.getColumnsCount();
            m_b = qr.solve(m_yl);
            m_w = new double[nx];
            m_L = qr.getR().transpose();

            DataBlock e = model.calcRes(new DataBlock(m_b));
            getStandardDeviationComputer().compute((filter(e)));
//	    DataBlock E = YL.deepClone();
            drcols.begin();
            do {
//		E.addAY(-m_b[drcols.getPosition()], drcol);
                m_w[drcols.getPosition()] = drcol.dot(YL);
            } while (drcols.next());

//	    calcMAD(E);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 
     * @param idx
     */
    protected void processOutlier(int idx) {
        int n = getModel().getY().getLength();
        int d = getModel().getDifferencingFilter().getDegree();
        double[] o = new double[2 * n];
        DataBlock O = new DataBlock(o);
        TsPeriod start = getDomain().getStart();
        IOutlierVariable outlier = getOutlierFactory(idx).create(start.firstday());
        outlier.data(start.minus(n), O);
        double[] od = new double[o.length - d];
        DataBlock OD = new DataBlock(od);
        getModel().getDifferencingFilter().filter(O, OD);

        DataBlock OL = new DataBlock(od, n, 2 * n - d, 1);
        for (int i = 0; i < n; ++i) {
            if (isDefined(i, idx)) {
//                double[] ol = new double[n - d];
//                DataBlock OL = new DataBlock(ol);
//                System.arraycopy(od, n - i , ol, 0, ol.length);
                double[] u = new double[m_n];
                DataBlock U = new DataBlock(u);
                m_filter.filter(OL, U);
                double xx = 0, xy = 0;
                for (int j = 0; j < u.length; ++j) {
                    xx += u[j] * u[j];
                    xy += u[j] * m_yl[j];
                }

                if (m_L != null) {
                    double[] l = new double[m_b.length];
                    DataBlockIterator xcols = m_X.columns();
                    DataBlock xcol = xcols.getData();
                    do {
                        l[xcols.getPosition()] = xcol.dot(U);
                    } while (xcols.next());
                    DataBlock L = new DataBlock(l);
                    // K=A^-1*L
                    // lA * lA' * K = L
                    // l'AA^-1l = |l' * lA'^-1|
                    LowerTriangularMatrix.rsolve(m_L, l);
                    // q = l'A^-1l
                    double q = L.dot(L);
                    //
                    double c = xx - q;
                    if (c <= 0) {
                        exclude(i, idx);
                    } else {
                        LowerTriangularMatrix.lsolve(m_L, l);
                        setT(i, idx, (xy - new DataBlock(m_w).dot(L))
                                / (Math.sqrt(c)) / getMAD());
                    }
                } else if (xx <= 0) {
                    exclude(i, idx);
                } else {
                    setT(i, idx, (xy / (Math.sqrt(xx)) / getMAD()));
                }
            }
            OL.move(-1);
        }
    }

    protected DataBlock filter(DataBlock res) {
        ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
        IArimaModel arma = getModel().getArma();
        int nf = f.initialize(arma, res.getLength());
        DataBlock fres = new DataBlock(nf);
        f.filter(res, fres);
        return fres.drop(nf - getModel().getDModel().getObsCount(), 0);
    }

    @Override
    protected void clear(boolean all) {
        super.clear(all);
        m_L = null;
        m_X = null;
        m_b = null;
        m_w = null;
    }
}
