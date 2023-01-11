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
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import demetra.data.DoubleSeq;
import jdplus.ssf.univariate.DefaultFilteringResults;

/**
 *
 * @author Jean Palate
 */
public class FastDkFilter {

    public static interface VarianceFilterProvider {

        int size();

        int endDiffusePosition();

        boolean isMissing(int pos);

        double errorVariance(int pos);

        double diffuseNorm2(int pos);

        DataBlock M(int pos);

        DataBlock Mi(int pos);

        public static VarianceFilterProvider of(final BaseDiffuseFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }

                @Override
                public int endDiffusePosition() {
                    return fr.getEndDiffusePosition();
                }

                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }

                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }

                @Override
                public double diffuseNorm2(int pos) {
                    return fr.diffuseNorm2(pos);
                }

                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }

                @Override
                public DataBlock Mi(int pos) {
                    return fr.Mi(pos);
                }

            };
        }

        public static VarianceFilterProvider of(final DefaultFilteringResults fr) {
            return new VarianceFilterProvider() {
                @Override
                public int size() {
                    return fr.size();
                }

                @Override
                public int endDiffusePosition() {
                    return 0;
                }

                @Override
                public boolean isMissing(int pos) {
                    return fr.isMissing(pos);
                }

                @Override
                public double errorVariance(int pos) {
                    return fr.errorVariance(pos);
                }

                @Override
                public double diffuseNorm2(int pos) {
                    return 0;
                }

                @Override
                public DataBlock M(int pos) {
                    return fr.M(pos);
                }

                @Override
                public DataBlock Mi(int pos) {
                    return DataBlock.EMPTY;
                }

            };
        }
    }

    private final VarianceFilterProvider vf;
    private final ISsf ssf;
    private final ISsfLoading loading;
    private final ISsfDynamics dynamics;
    private final int enddiffuse;
    private final boolean normalized;

    public boolean isnormalized() {
        return normalized;
    }

    public boolean filter(FastMatrix x) {
        if (x.getColumnsCount() == 1) {
            return new FastDiffuseFilter1().filter(x.column(0), normalized);
        } else {
            return new FastDiffuseFilterN().filter(x, normalized);
        }
    }

    public boolean filter(DataBlock x) {
        return new FastDiffuseFilter1().filter(x, normalized);
    }

    public FastDkFilter(ISsf ssf, BaseDiffuseFilteringResults frslts, boolean normalized) {
        this.vf = VarianceFilterProvider.of(frslts);
        this.ssf = ssf;
        loading = ssf.loading();
        dynamics = ssf.dynamics();
        enddiffuse = frslts.getEndDiffusePosition();
        this.normalized = normalized;
    }

    public void apply(DoubleSeq in, DataBlock out) {
        new FastDiffuseFilter1().apply(in, out);
    }

    class FastDiffuseFilterN {

        private FastMatrix states;
        // temporaries
        private DataBlock tmp;
        private DataBlockIterator scols;

        boolean filter(FastMatrix x, boolean normalized) {
            int n = vf.size();
            if (x.getRowsCount() > n) {
                return false;
            }
            int dim = ssf.getStateDim();
            states = FastMatrix.make(dim, x.getColumnsCount());
            prepareTmp();
            DataBlockIterator rows = x.rowsIterator();
            int pos = 0;
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
            boolean missing = vf.isMissing(i);
            double f = vf.errorVariance(i);
            double w;
            DataBlock K;
            if (i < enddiffuse) {
                double fi = vf.diffuseNorm2(i);
                if (fi != 0) {
                    w = fi;
                    K = vf.Mi(i);
                } else {
                    w = f;
                    K = vf.M(i);
                }
            } else {
                w = f;
                K = vf.M(i);
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
            int len = vf.size();
            if (x.length() > len) {
                return false;
            }
            int dim = ssf.getStateDim(), n = x.length();
            state = DataBlock.make(dim);
            int pos = 0;
            do {
                x.set(pos, iterate(pos, x.get(pos), normalized));
            } while (++pos < n);
            return true;
        }

        private double iterate(int i, double y, boolean normalized) {
            boolean missing = vf.isMissing(i);
            double f = vf.errorVariance(i);
            double w;
            DataBlock K;
            if (i < enddiffuse) {
                double fi = vf.diffuseNorm2(i);
                if (fi != 0) {
                    w = fi;
                    K = vf.Mi(i);
                } else {
                    w = f;
                    K = vf.M(i);
                }
            } else {
                w = f;
                K = vf.M(i);
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
            int len = vf.size();
            if (in.length() > len) {
                return false;
            }
            int dim = ssf.getStateDim(), n = in.length();
            state = DataBlock.make(dim);
            int pos = 0, opos = 0;
            do {
                boolean missing = vf.isMissing(pos);
                if (!missing) {
                    double f = vf.errorVariance(pos);
                    double w;
                    DataBlock K;
                    boolean diffuse = false;
                    if (pos < enddiffuse) {
                        double fi = vf.diffuseNorm2(pos);
                        if (fi != 0) {
                            w = fi;
                            K = vf.Mi(pos);
                            diffuse = true;
                        } else {
                            w = f;
                            K = vf.M(pos);
                        }
                    } else {
                        w = f;
                        K = vf.M(pos);
                    }

                    double e = in.get(pos) - loading.ZX(pos, state);
                    // update the states
                    state.addAY(e / w, K);
                    if (!diffuse && f != 0) {
                        out.set(opos++, e / Math.sqrt(f));
                    }
                }
                dynamics.TX(pos++, state);
            } while (pos < n);
            return true;
        }

    }
}
