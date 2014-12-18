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
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockIterator;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.DefaultLikelihoodEvaluation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Householder;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.matrices.SymmetricMatrix;
import ec.tstoolkit.maths.matrices.UpperTriangularMatrix;

/**
 *
 * @param <F>
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AkfAlgorithm<F extends ISsf> implements ISsfAlgorithm<F> {

    private boolean m_ssq = false, m_ml = true, m_diffuse = false;

    /**
     *
     */
    public AkfAlgorithm() {
    }

    /**
     *
     * @param instance
     * @return
     */
    @Override
    public DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> evaluate(
            SsfModel<F> instance) {
        SsfModel<F> model = instance;
        FilteringResults frslts = new FilteringResults(true);
        Filter<ISsf> filter = new Filter<>();
        F ssf = model.ssf;
        filter.setSsf(ssf);

        int statedim = ssf.getStateDim();

        State state = new State(statedim, true);
        ssf.Pf0(state.P.subMatrix());
        SsfInitializer initializer = new SsfInitializer(0, state);
        filter.setInitializer(initializer);
        if (!filter.process(model.getData(), frslts)) {
            return null;
        }

        double[] yl = frslts.getFilteredData().data(true, true);
        int ndiffuse = ssf.getNonStationaryDim(), nx = model.getX() == null ? 0
                : model.getX().getColumnsCount();

        DiffuseConcentratedLikelihood ll = new DiffuseConcentratedLikelihood();
        if (ndiffuse > 0 || nx > 0) {
            Matrix xl = new Matrix(yl.length, ndiffuse + nx);
            double[] buffer = new double[model.getData().getCount()];
            DataBlockIterator lcols = xl.columns();
            if (ndiffuse > 0) {
                Matrix dconst = new Matrix(statedim, ndiffuse);
                ssf.diffuseConstraints(dconst.subMatrix());
                DataBlockIterator dcols = dconst.columns();
                DataBlock dcol = dcols.getData();
                DataBlock lcol = lcols.getData();
                double[] start = new double[statedim];
                do {
                    dcol.copyTo(start, 0);
                    frslts.getVarianceFilter().process(
                            frslts.getFilteredData(), 0, buffer, start);
                    lcol.copy(new DataBlock(frslts.getFilteredData().data(true,
                            true)));
                } while (lcols.next() && dcols.next());
            }
            if (nx > 0) {
                DataBlockIterator cols = model.getX().columns();
                DataBlock col = cols.getData();
                DataBlock lcol = lcols.getData();
                do {
                    col.copyTo(buffer, 0);
                    frslts.getVarianceFilter().process(
                            frslts.getFilteredData(), 0, buffer, null);
                    lcol.copy(new DataBlock(frslts.getFilteredData().data(true,
                            true)));
                } while (lcols.next() && cols.next());
            }

            Householder qr = new Householder(false);
            qr.decompose(xl);
            DataBlock res = new DataBlock(xl.getRowsCount()
                    - xl.getColumnsCount());
            double[] b = new double[xl.getColumnsCount()];
            qr.leastSquares(new DataBlock(yl), new DataBlock(b), res);
            double ssqerr = res.ssq();
            Matrix u = UpperTriangularMatrix.inverse(qr.getR());

            // initializing the results...
            int n = model.getData().getObsCount();
            int d = 0;
            if (m_diffuse) {
                d = ndiffuse;
            }
            if (model.getDiffuseX() != null) {
                d += model.getDiffuseX().length;
            }

            double sig = ssqerr / (n - d);
            Matrix bvar = SymmetricMatrix.XXt(u);
            bvar.mul(sig);
            double lddet = 0;
            if (d > 0) {
                DataBlock rdiag = qr.getRDiagonal();
                int j=0;
                if (ndiffuse > 0 && m_diffuse) {
                    for (int i = 0; i < ndiffuse; ++i) {
                        lddet += Math.log(Math.abs(rdiag.get(i)));
                    }
                    j=ndiffuse;
                }
                if (model.getDiffuseX() != null) {
                    for (int i = 0; i < model.getDiffuseX().length; ++i) {
                        lddet += Math.log(Math.abs(rdiag.get(j+model
                                .getDiffuseX()[i])));
                    }
                }
                lddet *= 2;
            }
            ll.set(ssqerr, frslts.getLogDeterminant(), lddet, n, d);
            ll.setRes(res.getData());
            ll.setB(b, bvar, qr.getRank());
        } else {
            LikelihoodEvaluation.evaluate(frslts, ll);
        }
        DefaultLikelihoodEvaluation<DiffuseConcentratedLikelihood> rslt = new DefaultLikelihoodEvaluation<>(
                ll);
        rslt.useLogLikelihood(!m_ssq);
        rslt.useML(m_ml);
        return rslt;
    }

    /**
     *
     * @return
     */
    public boolean isUsingDiffuseInitialization() {
        return m_diffuse;
    }

    /**
     *
     * @return
     */
    public boolean isUsingML() {
        return m_ml;
    }

    /**
     *
     * @return
     */
    public boolean isUsingSsq() {
        return m_ssq;
    }

    /**
     *
     * @param value
     */
    public void useDiffuseInitialization(boolean value) {
        m_diffuse = value;
    }

    /**
     *
     * @param value
     */
    public void useML(boolean value) {
        m_ml = value;
    }

    /**
     *
     * @param value
     */
    public void useSsq(boolean value) {
        m_ssq = value;
    }
}
