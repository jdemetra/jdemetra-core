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
package jdplus.ssf.dk;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import jdplus.math.matrices.Matrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ResultsRange;
import jdplus.ssf.State;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DkFilter {

    private final BaseDiffuseFilteringResults frslts;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private final int start, end, enddiffuse;
    private final boolean normalized;

    public boolean isnormalized() {
        return normalized;
    }

    public boolean filter(Matrix x) {
        if (x.getColumnsCount() == 1) {
            return new FastDiffuseFilter1().filter(x.column(0), normalized);
        } else {
            return new FastDiffuseFilterN().filter(x, normalized);
        }
    }

    public boolean filter(DataBlock x) {
        return new FastDiffuseFilter1().filter(x, normalized);
    }

    public DkFilter(ISsf ssf, BaseDiffuseFilteringResults frslts, ResultsRange range, boolean normalized) {
        this.frslts = frslts;
        this.ssf = ssf;
        loading = ssf.loading();
        dynamics = ssf.dynamics();
        start = range.getStart();
        end = range.getEnd();
        enddiffuse = frslts.getEndDiffusePosition();
        this.normalized = normalized;
    }

    public void apply(DoubleSeq in, DataBlock out) {
        new FastDiffuseFilter1().apply(in, out);
    }

    class FastDiffuseFilterN {

        private Matrix states;
        // temporaries
        private DataBlock tmp;
        private DataBlockIterator scols;

        boolean filter(Matrix x, boolean normalized) {
            if (x.getRowsCount() > end - start) {
                return false;
            }
            int dim = ssf.getStateDim();
            states = Matrix.make(dim, x.getColumnsCount());
            prepareTmp();
            DataBlockIterator rows = x.rowsIterator();
            int pos = start;
            while (rows.hasNext()) {
                iterate(pos++, rows.next(), normalized);
            }
            return true;
        }

        private void prepareTmp() {
            int nvars = states.getColumnsCount();
            tmp = DataBlock.make(nvars);
            scols = states.columnsIterator();
        }

        private void iterate(int i, DataBlock row, boolean normalized) {
            boolean missing = !Double.isFinite(frslts.error(i));
            double f = frslts.errorVariance(i);
            double w;
            DataBlock K;
            if (i < enddiffuse) {
                double fi = frslts.diffuseNorm2(i);
                if (fi != 0) {
                    w = fi;
                    K = frslts.Mi(i);
                } else {
                    w = f;
                    K = frslts.M(i);
                }
            } else {
                w = f;
                K = frslts.M(i);
            }

            loading.ZM(i, states, tmp);
            row.sub(tmp);
            if (w > 0) {
                if (!missing) {
                    // update the states
                    scols.reset();
                    DoubleSeqCursor reader = row.cursor();
                    while (scols.hasNext()) {
                        DataBlock scol = scols.next();
                        scol.addAY(reader.getAndNext() / w, K);
                    }
                }
            }
            if (f > 0) {
                if (normalized) {
                    row.mul(1 / Math.sqrt(f));
                }
            } else {
                row.apply(q -> Math.abs(q) > State.ZERO ? Double.NaN : 0);
            }

            scols.reset();
            while (scols.hasNext()) {
                dynamics.TX(i, scols.next());
            }
//            row.set(Double.NaN);
        }
    }

    class FastDiffuseFilter1 {

        private DataBlock state;

        boolean filter(DataBlock x, boolean normalized) {
            if (x.length() > end - start) {
                return false;
            }
            int dim = ssf.getStateDim(), n = x.length();
            state = DataBlock.make(dim);
            int pos = start, xpos = 0;
            do {
                x.set(xpos, iterate(pos, x.get(xpos), normalized));
                pos++;
                xpos++;
            } while (xpos < n);
            return true;
        }

        private double iterate(int i, double y, boolean normalized) {
            boolean missing = !Double.isFinite(frslts.error(i));
            double f = frslts.errorVariance(i);
            double w;
            DataBlock K;
            if (i < enddiffuse) {
                double fi = frslts.diffuseNorm2(i);
                if (fi != 0) {
                    w = fi;
                    K = frslts.Mi(i);
                } else {
                    w = f;
                    K = frslts.M(i);
                }
            } else {
                w = f;
                K = frslts.M(i);
            }
            double e = y - loading.ZX(i, state);
            // update the states
            if (w > 0) {
                if (!missing) {
                    state.addAY(e / w, K);
                }
            }
            if (f > 0) { // can we have fi > 0 && f == 0 ?
                if (normalized) {
                    e /= Math.sqrt(f);
                }
            } else if (Math.abs(e) > State.ZERO) {
                e = Double.NaN;
            } else {
                e = 0;
            }
            dynamics.TX(i, state);
            return e;
        }

        boolean apply(DoubleSeq in, DataBlock out) {
            if (in.length() > end - start) {
                return false;
            }
            int dim = ssf.getStateDim(), n = in.length();
            state = DataBlock.make(dim);
            int pos = start, ipos = 0, opos = 0;
            do {
                boolean missing = !Double.isFinite(frslts.error(pos));
                if (!missing) {
                    double f = frslts.errorVariance(pos);
                    double w;
                    DataBlock K;
                    boolean diffuse = false;
                    if (pos < enddiffuse) {
                        double fi = frslts.diffuseNorm2(pos);
                        if (fi != 0) {
                            w = fi;
                            K = frslts.Mi(pos);
                            diffuse = true;
                        } else {
                            w = f;
                            K = frslts.M(pos);
                        }
                    } else {
                        w = f;
                        K = frslts.M(pos);
                    }

                    double e = in.get(ipos) - loading.ZX(pos, state);
                    // update the states
                    state.addAY(e / w, K);
                    if (!diffuse && f != 0) {
                        out.set(opos++, e / Math.sqrt(f));
                    }
                }
                dynamics.TX(pos++, state);
            } while (++ipos < n);
            return true;
        }

    }
}
