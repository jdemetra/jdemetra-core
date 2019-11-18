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
package jdplus.sarima.estimation;

import jdplus.arima.estimation.IArimaMapping;
import jdplus.data.DataBlock;
import demetra.design.Development;
import demetra.maths.Complex;
import jdplus.math.functions.FunctionException;
import jdplus.math.functions.ParamValidation;
import jdplus.math.matrices.Matrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SarimaFixedMapping implements IArimaMapping<SarimaModel> {

    static final double REPS = 0.01;
    final SarimaMapping mapper;
    final boolean[] fixedItems;
    final double[] parameters;
    private static final double RMAX = .99;

    public boolean[] getFixedItems() {
        return fixedItems.clone();
    }

    /**
     *
     * @param spec The specification of the ARIMA model
     * @param p All the parameters of a model, including the free ones (which are unused)
     * @param fixed Indicates the fixed parameters. The array must have the same 
     * length as the parameters (given by the specification) 
     */
    public SarimaFixedMapping(SarimaSpecification spec, DoubleSeq p,
            boolean[] fixed) {
        mapper = SarimaMapping.of(spec);
        fixedItems = fixed.clone();
        parameters = p.toArray();
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        return mapper.checkBoundaries(fullParameters(inparams));
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        return mapper.epsilon(fullParameters(inparams), fullIndex(idx));
    }

    private int fixed() {
        int n = 0;
        for (int i = 0; i < fixedItems.length; ++i) {
            if (fixedItems[i]) {
                ++n;
            }
        }
        return n;
    }

    /**
     *
     * @param allParams
     * @return
     */
    public DataBlock freeParameters(DoubleSeq allParams) {
        DataBlock free = DataBlock.make(getDim());
        save(allParams, free);
        return free;
    }

    public int fullIndex(int freeIndex) {
        int cur = -1;
        for (int i = 0; i < fixedItems.length; ++i) {

            if (!fixedItems[i]) {
                ++cur;
            }
            if (cur == freeIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an array of free parameters (dim = spec.dim) from an array of
     * fixed parameters (dim)
     *
     * @param freeParams
     * @return
     */
    public DataBlock fullParameters(DoubleSeq freeParams) {
        double[] buffer = parameters.clone();
        for (int i = 0, j = 0; i < freeParams.length(); ++i, ++j) {
            while (fixedItems[j]) {
                ++j;
            }
            buffer[j] = freeParams.get(i);
        }
        return DataBlock.of(buffer);
    }

    @Override
    public int getDim() {
        return mapper.getSpec().getParametersCount() - fixed();
    }

    @Override
    public double lbound(int idx) {
        return mapper.lbound(fullIndex(idx));
    }

    @Override
    public SarimaModel map(DoubleSeq p) {
        return mapper.map(fullParameters(p));
    }

    @Override
    public DoubleSeq parametersOf(SarimaModel t) {
        return freeParameters(mapper.parametersOf(t));
    }

    private void save(DoubleSeq all, DataBlock free) {
        for (int i = 0, j = 0; i < fixedItems.length; ++i) {
            if (!fixedItems[i]) {
                free.set(j++, all.get(i));
            }
        }
    }

    @Override
    public double ubound(int idx) {
        return mapper.ubound(fullIndex(idx));
    }
    private static final int MAX_ITER = 20;

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        DataBlock tmp = fullParameters(ioparams);
        if (mapper.checkBoundaries(tmp)) {
            save(tmp, ioparams);
            return ParamValidation.Valid;
        }
        // we validate block by block
        int beg = 0, end = mapper.getSpec().getP();
        boolean changed = false;
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, RMAX)) {
                    changed = true;
                }
            } else if (fstabilize(tmp, beg, end - beg)) {
                changed = true;
            }
        }
        beg = end;
        end += mapper.getSpec().getBp();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, RMAX)) {
                    changed = true;
                }
            } else if (fstabilize(tmp, beg, end - beg)) {
                changed = true;
            }
        }
        beg = end;
        end += mapper.getSpec().getQ();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, 1)) {
                    changed = true;
                }
            } else if (fstabilize(tmp, beg, end - beg)) {
                changed = true;
            }
        }
        beg = end;
        end += mapper.getSpec().getBq();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, 1)) {
                    changed = true;
                }
            } else if (fstabilize(tmp, beg, end - beg)) {
                changed = true;
            }
        }
        if (changed) {
            save(tmp, ioparams);
            return ParamValidation.Changed;
        } else {
            return ParamValidation.Valid;
        }
        // number of invalid coefficients
    }

    public Matrix expandCovariance(Matrix cov) {
        int dim = getDim();
        if (cov.getColumnsCount() != dim) {
            return null;
        }
        int[] idx = new int[dim];
        for (int i = 0, j = 0; i < fixedItems.length; ++i) {
            if (!fixedItems[i]) {
                idx[j++] = i;
            }
        }
        Matrix ecov = Matrix.make(fixedItems.length, fixedItems.length);
        for (int i = 0; i < dim; ++i) {
            for (int j = 0; j <= i; ++j) {
                double s = cov.get(i, j);
                ecov.set(idx[i], idx[j], s);
                if (i != j) {
                    ecov.set(idx[j], idx[i], s);
                }
            }
        }
        return ecov;
    }

    private boolean isFree(int beg, int end) {
        for (int i = beg; i < end; ++i) {
            if (this.fixedItems[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean stabilize(DataBlock c, int start,
            int nc, double rmax) {
        if (nc == 0) {
            return false;
        }
        if (SarimaMapping.checkStability(c.extract(start, nc))) {
            return false;
        }
        if (nc == 1) {
            double c0 = c.get(start);
            double cabs = Math.abs(c0);

            if (rmax < 1 && Math.abs(cabs - 1) <= REPS) {
                c.set(start, c0 > 0 ? rmax : -rmax);
                return true;
            } else if (cabs > 1) {
                c.set(start, 1 / c0);
                return true;
            } else {
                return false;
            }
        }

        double[] ctmp = new double[nc + 1];
        ctmp[0] = 1;
        for (int i = 0; i < nc; ++i) {
            ctmp[1 + i] = c.get(start + i);
        }
        Polynomial p = Polynomial.of(ctmp);
        Polynomial sp = stabilize(p, rmax);
        if (p != sp) {
            for (int i = 0; i < nc; ++i) {
                c.set(start + i, sp.get(1 + i));
            }
            return true;
        }
        return false;
    }

    private static Polynomial stabilize(Polynomial p, double rmax) {
        if (p == null) {
            return null;
        }

        Complex[] roots = p.roots();
        boolean changed = false;
        for (int i = 0; i < roots.length; ++i) {
            Complex root = roots[i];
            double n = 1 / roots[i].abs();
            if (rmax < 1 && Math.abs(n - 1) <= REPS) {
                roots[i] = root.times(n / rmax);
                changed = true;
            } else if (n > 1) {
                roots[i] = root.inv();
                changed = true;
            }
        }
        if (!changed) {
            return p;
        }
        Polynomial ptmp = Polynomial.fromComplexRoots(roots);
        ptmp = ptmp.divide(ptmp.get(0));
        return ptmp;
    }

    /**
     * Stabilises a polynomial that contains fixed parameters
     *
     * @param ioparams
     * @param beg
     * @param n
     * @return
     */
    private boolean fstabilize(DataBlock ioparams, int beg, int n) {
        DataBlock extract = ioparams.extract(beg, n);
        if (SarimaMapping.checkStability(extract)) {
            return false;
        }
        DataBlock tmp = DataBlock.make(n);
        int k = 0;
        do {
            for (int i = 0; i < n; ++i) {
                if (!this.fixedItems[beg + i]) {
                    tmp.copy(extract);
                    for (int j = 0; j < n; ++j) {
                        if (!fixedItems[beg + j]) {
                            tmp.set(j, 0);
                        }
                    }
                    tmp.set(i, K);
                    for (int j = 0; j < 10; ++j) {
                        if (SarimaMapping.checkStability(tmp.extract(0, n))) {
                            extract.copy(tmp);
                            return true;
                        } else {
                            tmp.mul(i, MK);
                        }
                    }
                }
            }

        } while (++k <= MAX_ITER);

        throw new FunctionException("Invalid mapping");
    }
    private static final double K = .2, MK = -1.25;

    @Override
    public String getDescription(final int idx) {
        return mapper.getDescription(fullIndex(idx));
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        SarimaSpecification spec = mapper.getSpec();
        double[] p = new double[getDim()];
        int nar = spec.getP() + spec.getBp();
        int j = 0;
        for (int i = 0; i < nar; ++i) {
            if (!fixedItems[i]) {
                p[j++] = -.1;
            }
        }
        for (int i = nar; i < p.length; ++i) {
            if (!fixedItems[i]) {
                p[j++] = -.2;
            }
        }
        return DoubleSeq.of(p);
    }

    @Override
    public IArimaMapping<SarimaModel> stationaryMapping() {
        return new SarimaFixedMapping(SarimaSpecification.stationary(mapper.getSpec()), DoubleSeq.of(parameters), this.fixedItems);
    }
}
