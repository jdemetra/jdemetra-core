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
package ec.tstoolkit.sarima.estimation;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.FunctionException;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import static ec.tstoolkit.sarima.estimation.DefaultSarimaMapping.desc;
import static ec.tstoolkit.sarima.estimation.SarimaMapping.REPS;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SarimaFixedMapping implements IParametricMapping<SarimaModel> {

    final SarimaMapping mapper;
    final boolean[] fixedItems;
    final double[] parameters;
    private static final double RMAX = .99;

    public boolean[] getFixedItems() {
        return fixedItems.clone();
    }

    /**
     *
     * @param start
     * @param p
     * @param fixed
     */
    public SarimaFixedMapping(SarimaSpecification start, IReadDataBlock p,
            boolean[] fixed) {
        mapper = new SarimaMapping(start, false);
        fixedItems = fixed.clone();
        parameters = new double[p.getLength()];
        p.copyTo(parameters, 0);
    }

    public SarimaFixedMapping(final SarimaSpecification start, final double[] p,
            final boolean[] fixed) {
        mapper = new SarimaMapping(start, false);
        fixedItems = fixed.clone();
        parameters = p;
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        return mapper.checkBoundaries(fullParameters(inparams));
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
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
    public IReadDataBlock freeParameters(IReadDataBlock allParams) {
        DataBlock free = new DataBlock(getDim());
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
    public DataBlock fullParameters(IReadDataBlock freeParams) {
        double[] buffer = parameters.clone();
        for (int i = 0, j = 0; i < freeParams.getLength(); ++i, ++j) {
            while (fixedItems[j]) {
                ++j;
            }
            buffer[j] = freeParams.get(i);
        }
        return new DataBlock(buffer);
    }

    @Override
    public int getDim() {
        return mapper.spec.getParametersCount() - fixed();
    }

    @Override
    public double lbound(int idx) {
        return mapper.lbound(fullIndex(idx));
    }

    @Override
    public SarimaModel map(IReadDataBlock p) {
        return mapper.map(fullParameters(p));
    }

    @Override
    public IReadDataBlock map(SarimaModel t) {
        return freeParameters(mapper.map(t));
    }

    private void save(IReadDataBlock all, IDataBlock free) {
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
    public ParamValidation validate(IDataBlock ioparams) {
        DataBlock tmp = fullParameters(ioparams);
        if (mapper.checkBoundaries(tmp)) {
            save(tmp, ioparams);
            return ParamValidation.Valid;
        }
        // we validate block by block
        int beg = 0, end = mapper.spec.getP();
        boolean changed = false;
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, RMAX)) {
                    changed = true;
                }
            } else {
                if (fstabilize(tmp, beg, end - beg)) {
                    changed = true;
                }
            }
        }
        beg = end;
        end += mapper.spec.getBP();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, RMAX)) {
                    changed = true;
                }
            } else {
                if (fstabilize(tmp, beg, end - beg)) {
                    changed = true;
                }
            }
        }
        beg = end;
        end += mapper.spec.getQ();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, 1)) {
                    changed = true;
                }
            } else {
                if (fstabilize(tmp, beg, end - beg)) {
                    changed = true;
                }
            }
        }
        beg = end;
        end += mapper.spec.getBQ();
        if (beg != end) {
            if (isFree(beg, end)) {
                if (stabilize(tmp, beg, end - beg, 1)) {
                    changed = true;
                }
            } else {
                if (fstabilize(tmp, beg, end - beg)) {
                    changed = true;
                }
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
        Matrix ecov = new Matrix(fixedItems.length, fixedItems.length);
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

    private static boolean stabilize(IDataBlock c, int start,
            int nc, double rmax) {
        if (nc == 0) {
            return false;
        }
        if (SarimaMapping.checkStability(c, start, nc)) {
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
        if (SarimaMapping.checkStability(extract, 0, n)) {
            return false;
        }
        DataBlock tmp = new DataBlock(n);
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
                        if (SarimaMapping.checkStability(tmp, 0, n)) {
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
}
