/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.ssf.implementations;

import demetra.data.Cell;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfMeasurement;
import java.util.Collection;
import java.util.Iterator;
import demetra.data.DataBlockIterator;
import demetra.maths.matrices.SymmetricMatrix;

/**
 *
 * @author Jean Palate
 */
public class CompositeMeasurements implements ISsfMeasurements {

    public static ISsfMeasurements of(ISsf... ssf) {
        ISsfMeasurement[] l = new ISsfMeasurement[ssf.length];
        int[] cdim = new int[ssf.length + 1];
        for (int i = 0; i < ssf.length; ++i) {
            cdim[i + 1] = cdim[i] + ssf[i].getStateDim();
            l[i] = ssf[i].getMeasurement();
        }
        return new CompositeMeasurements(l, cdim, null, null);
    }

    public static ISsfMeasurements of(Matrix corr, ISsf... ssf) {
        if (!corr.isSquare()) {
            return null;
        }
        if (corr.getRowsCount() != ssf.length) {
            return null;
        }
        if (!corr.diagonal().isConstant(1)) {
            return null;
        }
        if (corr.isDiagonal()) {
            return of(ssf);
        }
        Matrix lcorr = corr.deepClone();
        try {
            SymmetricMatrix.lcholesky(lcorr, 1e-9);
        } catch (MatrixException err) {
            return null;
        }

        ISsfMeasurement[] l = new ISsfMeasurement[ssf.length];
        int[] cdim = new int[ssf.length + 1];
        for (int i = 0; i < ssf.length; ++i) {
            cdim[i + 1] = cdim[i] + ssf[i].getStateDim();
            l[i] = ssf[i].getMeasurement();
        }
        return new CompositeMeasurements(l, cdim, corr, lcorr);
    }

    public static ISsfMeasurements of(Collection<ISsf> ssf) {
        ISsfMeasurement[] l = new ISsfMeasurement[ssf.size()];
        int[] cdim = new int[l.length];
        int i = 0;
        for (ISsf s : ssf) {
            l[i++] = s.getMeasurement();
            cdim[i] = s.getStateDim() + cdim[i - 1];
        }
        return new CompositeMeasurements(l, cdim, null, null);
    }

    private final ISsfMeasurement[] measurements;
    private final int[] cdim;
    private final DataBlock tmp;
    private final Matrix corr, lcorr;

    /**
     * H = D C D, where D is the correlation matrix, D is the diagonal matrix
     * with the individual standard deviations. H = R R' = D L L' D, so that R =
     * D * L (each column of L is multiplied by the individual standard
     * deviation
     *
     * @param ms
     * @param cdim
     * @param corr
     * @param lcorr
     */
    private CompositeMeasurements(final ISsfMeasurement[] ms, final int[] cdim, Matrix corr, Matrix lcorr) {
        this.measurements = ms;
        this.corr = corr;
        this.lcorr = lcorr;
        int n = ms.length;
        this.cdim = cdim;
        tmp = DataBlock.make(cdim[n]);
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < measurements.length; ++i) {
            if (!measurements[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void Z(int pos, int var, DataBlock z) {
        DataBlock cur = z.range(cdim[var], cdim[var + 1]);
        measurements[var].Z(pos, cur);
    }

    @Override
    public boolean hasErrors() {
        for (int i = 0; i < measurements.length; ++i) {
            if (measurements[i].hasErrors()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasError(int pos) {
        for (int i = 0; i < measurements.length; ++i) {
            if (measurements[i].hasError(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasIndependentErrors() {
        return corr == null;
    }

    @Override
    public int getCount(int pos) {
        return measurements.length;
    }

    @Override
    public int getMaxCount() {
        return measurements.length;
    }

    @Override
    public boolean isHomogeneous() {
        return true;
    }

    @Override
    public void addH(int pos, Matrix V) {
        DataBlock diagonal = V.diagonal();
        for (int i = 0; i < measurements.length; ++i) {
            if (measurements[i].hasError(pos)) {
                diagonal.add(i, measurements[i].errorVariance(pos));
            }
        }
        if (corr != null) {
            for (int i = 0; i < measurements.length; ++i) {
                double vi = measurements[i].errorVariance(pos);
                if (vi != 0) {
                    double svi = Math.sqrt(vi);
                    for (int j = 0; j < i; ++j) {
                        double vj = measurements[j].errorVariance(pos);
                        if (vj != 0) {
                            double svj = Math.sqrt(vj);
                            double z = corr.get(i, j) * svi * svj;
                            V.add(i, j, z);
                            V.add(j, i, z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void H(int pos, Matrix h) {
        DataBlock diagonal = h.diagonal();
        for (int i = 0; i < measurements.length; ++i) {
            if (measurements[i].hasError(pos)) {
                diagonal.set(i, measurements[i].errorVariance(pos));
            }
        }
        if (corr != null) {
            for (int i = 0; i < measurements.length; ++i) {
                double vi = measurements[i].errorVariance(pos);
                if (vi != 0) {
                    double svi = Math.sqrt(vi);
                    for (int j = 0; j < i; ++j) {
                        double vj = measurements[j].errorVariance(pos);
                        if (vj != 0) {
                            double svj = Math.sqrt(vj);
                            double z = corr.get(i, j) * svi * svj;
                            h.set(i, j, z);
                            h.set(j, i, z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void R(int pos, Matrix r) {
        DataBlock diagonal = r.diagonal();
        for (int i = 0; i < measurements.length; ++i) {
            if (measurements[i].hasError(pos)) {
                double v = measurements[i].errorVariance(pos);
                diagonal.set(i, Math.sqrt(v));
            }
        }
        if (lcorr != null) {
            for (int i = 0; i < measurements.length; ++i) {
                double vi = measurements[i].errorVariance(pos);
                if (vi != 0) {
                    double svi = Math.sqrt(vi);
                    for (int j = 0; j < i; ++j) {
                        if (measurements[j].hasError(pos));
                        r.set(j, i, lcorr.get(j, i) * svi);
                    }
                }

            }
        }
    }

    @Override
    public double ZX(int pos, int var, DataBlock m) {
        DataBlock cur = m.range(cdim[var], cdim[var + 1]);
        return measurements[var].ZX(pos, cur);
    }

    @Override
    public double ZVZ(int pos, int v, int w, Matrix V) {
        Matrix Vvw = V.extract(cdim[v], cdim[v + 1]-cdim[v], cdim[w], cdim[w + 1]-cdim[w]);
        if (v == w) {
            return measurements[v].ZVZ(pos, Vvw);
        } else {
            DataBlock zm = tmp.range(cdim[w], cdim[w + 1]);
            zm.set(0);
            measurements[v].ZM(pos, Vvw, zm);
            return measurements[w].ZX(pos, zm);
        }
    }

    @Override
    public void VpZdZ(int pos, int v, int w, Matrix V, double d) {
        Matrix Vvw = V.extract(cdim[v], cdim[v + 1]-cdim[v], cdim[w], cdim[w + 1]-cdim[w]);
        if (v == w) {
            measurements[v].VpZdZ(pos, Vvw, d);
        } else {
            DataBlock zw = tmp.range(cdim[w], cdim[w + 1]);
            zw.set(0);
            measurements[w].Z(pos, zw);
            DataBlockIterator columns = V.columnsIterator();
            Cell cell = zw.cells();
            while (columns.hasNext()) {
                measurements[v].XpZd(pos, columns.next(), cell.next() * d);

            }
        }
    }

    @Override
    public void XpZd(int pos, int var, DataBlock x, double d) {
        DataBlock cur = x.range(cdim[var], cdim[var + 1]);
        measurements[var].XpZd(pos, cur, d);
    }

}
