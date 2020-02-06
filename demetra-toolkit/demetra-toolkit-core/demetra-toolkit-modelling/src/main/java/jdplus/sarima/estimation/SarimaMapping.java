/*
 * Copyright 2017 National Bank of Belgium
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
import jdplus.math.functions.FunctionException;
import jdplus.math.functions.ParamValidation;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;
import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.math.Complex;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaMapping implements IArimaMapping<SarimaModel> {

    static final double MAX = 0.99999;
    public static final double STEP = Math.sqrt(2.220446e-16);

    /**
     *
     */
    private final SarimaOrders spec;
    private final double eps;
    private final boolean all;

    public static boolean checkStability(double d) {
        return Math.abs(d) < 1;
    }

    public static boolean checkStability(double a, double b) {
        double ro = b * b - 4 * a;
        if (ro < 0) {
            return Math.abs(a) < 1;
        } else {
            double sro = Math.sqrt(ro);
            double r = (-b + sro) / (2 * a);
            if (Math.abs(1 / r) >= 1) {
                return false;
            }
            r = (-b - sro) / (2 * a);
            return Math.abs(1 / r) < 1;
        }
    }

    public static boolean checkStability(DoubleSeq c) {
        int nc = c.length();
        while (nc > 0 && c.get(nc - 1) == 0) {
            --nc;
        }
        if (nc == 0) {
            return true;
        }
        if (nc == 1) {
            return checkStability(c.get(0));
        }
        if (nc == 2) {
            return checkStability(c.get(1), c.get(0));
        }

        return jdplus.math.linearfilters.FilterUtility.checkStability(c.extract(0, nc));
    }

    private static boolean stabilize(boolean all, SarimaOrders spec, DataBlock p) {
        boolean rslt = false;
        int start = 0;
        if (spec.getP() > 0) {
            if (stabilize(p.extract(0, spec.getP()))) {
                rslt = true;
            }
            start += spec.getP();
        }
        if (spec.getBp() > 0) {
            if (stabilize(p.extract(start, spec.getBp()))) {
                rslt = true;
            }
            start += spec.getBp();
        }
        if (!all) {
            return rslt;
        }
        if (spec.getQ() > 0) {
            if (stabilize(p.extract(start, spec.getQ()))) {
                rslt = true;
            }
            start += spec.getQ();
        }
        if (spec.getBq() > 0 && stabilize(p.extract(start, spec.getBq()))) {
            rslt = true;
        }
        return rslt;
    }

    private static boolean stabilize(DataBlock c) {
        int nc = c.length();
        if (nc == 0) {
            return false;
        }
        if (checkStability(c)) {
            return false;
        }
        if (nc == 1) {
            double c0 = c.get(0);
            double cabs = Math.abs(c0);

            if (cabs > 1) {
                c.set(0, 1 / c0);
                return true;
            } else {
                return false;
            }
        }

        double[] ctmp = new double[nc + 1];
        ctmp[0] = 1;
        for (int i = 0; i < nc; ++i) {
            ctmp[1 + i] = c.get(i);
        }
        Polynomial p = Polynomial.of(ctmp);
        Polynomial sp = stabilize(p);
        if (p != sp) {
            for (int i = 0; i < nc; ++i) {
                c.set(i, sp.get(1 + i));
            }
            return true;
        }
        return false;
    }

    public static Polynomial stabilize(Polynomial p) {
        if (p == null) {
            return null;
        }

        Complex[] roots = p.roots();
        boolean changed = false;
        for (int i = 0; i < roots.length; ++i) {
            Complex root = roots[i];
            double n = 1 / roots[i].abs();
            if (n > 1) {
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
     *
     * @param m
     * @return
     */
    public static SarimaModel stabilize(SarimaModel m) {
        DataBlock np = DataBlock.of(m.parameters());
        SarimaOrders mspec = m.specification();
        if (stabilize(true, mspec, np)) {
            return SarimaModel.builder(mspec).parameters(np).build();
        } else {
            return m;
        }
    }

    public static SarimaMapping of(SarimaOrders spec) {
        return new SarimaMapping(spec, STEP, true);
    }

    public static SarimaMapping ofStationary(final SarimaOrders spec) {
        SarimaOrders nspec = spec.clone();
        nspec.setD(0);
        nspec.setBd(0);
        return new SarimaMapping(nspec, STEP, true);
    }

    public SarimaMapping(SarimaOrders spec, double eps, boolean all) {
        this.spec = spec;
        this.all = all;
        this.eps = eps;
    }

    /**
     *
     * @param p
     * @return
     */
    @Override
    public boolean checkBoundaries(DoubleSeq p) {
        int start = 0;
        if (spec.getP() > 0) {
            if (!checkStability(p.extract(0, spec.getP()))) {
                return false;
            }
            start += spec.getP();
        }
        if (spec.getBp() > 0) {
            if (!checkStability(p.extract(start, spec.getBp()))) {
                return false;
            }
            start += spec.getBp();
        }
        if (all) {
            if (spec.getQ() > 0) {
                if (!checkStability(p.extract(start, spec.getQ()))) {
                    return false;
                }
                start += spec.getQ();
            }
            if (spec.getBq() > 0 && !checkStability(p.extract(start, spec.getBq()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
            return eps * Math.max(1, -p);
        } else {
            return -eps * Math.max(1, p);
        }
    }

    @Override
    public int getDim() {
        return spec.getParametersCount();
    }

    /**
     *
     * @return
     */
    public boolean isCheckingAll() {
        return all;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double lbound(int idx) {
        if (spec.getP() > 0) {
            if (idx < spec.getP()) {
                if (spec.getP() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getP();
        }
        if (spec.getBp() > 0) {
            if (idx < spec.getBp()) {
                if (spec.getBp() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getBp();
        }
        if (spec.getQ() > 0) {
            if (idx < spec.getQ()) {
                if (spec.getQ() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getQ();
        }
        if (spec.getBq() == 1) {
            return -MAX;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public SarimaModel map(DoubleSeq p) {
        if (p.length() != spec.getParametersCount()) {
            throw new FunctionException(FunctionException.DIM_ERR);
        }
        return SarimaModel.builder(spec).parameters(p).build();
    }

    @Override
    public double ubound(int idx) {
        if (spec.getP() > 0) {
            if (idx < spec.getP()) {
                if (spec.getP() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= spec.getP();
        }
        if (spec.getBp() > 0) {
            if (idx < spec.getBp()) {
                if (spec.getBp() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= spec.getBp();
        }
        if (spec.getQ() > 0) {
            if (idx < spec.getQ()) {
                if (spec.getQ() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= spec.getQ();
        }
        if (spec.getBq() == 1) {
            return MAX;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     *
     * @param value
     * @return
     */
    @Override
    public ParamValidation validate(DataBlock value) {
        if (value.length() != spec.getParametersCount()) {
            return ParamValidation.Invalid;
        }
        if (stabilize(true, spec, value)) {
//        if (stabilize(m_all, false, spec, value, rmax_)) {
//           needUrCancelling(value);
            return ParamValidation.Changed;
        } else {
//            if (needUrCancelling(value)) {
//                return ParamValidation.Changed;
//            } else {
            return ParamValidation.Valid;
//            }
        }
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] p = new double[spec.getParametersCount()];
        int nar = spec.getP() + spec.getBp();
        for (int i = 0; i < nar; ++i) {
            p[i] = -.1;
        }
        for (int i = nar; i < p.length; ++i) {
            p[i] = -.2;
        }
        return DoubleSeq.of(p);
    }

    @Override
    public DoubleSeq parametersOf(SarimaModel m) {
        return m.parameters();
    }

    @Override
    public String getDescription(final int idx) {
        return getDescription(spec, idx);
    }

    static String getDescription(final SarimaOrders xspec, final int idx) {
        int i = idx;
        if (i < xspec.getP()) {
            return desc(PHI, i);
        } else {
            i -= xspec.getP();
        }
        if (i < xspec.getBp()) {
            return desc(BPHI, i);
        } else {
            i -= xspec.getBp();
        }
        if (i < xspec.getQ()) {
            return desc(TH, i);
        } else {
            i -= xspec.getQ();
        }
        if (i < xspec.getBq()) {
            return desc(BTH, i);
        } else {
            return EMPTY;
        }
    }

    static String desc(String prefix, int idx) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append('(').append(idx + 1).append(')');
        return builder.toString();
    }

    public static final String PHI = "phi", BPHI = "bphi", TH = "th", BTH = "bth";

    /**
     * @return the spec
     */
    public SarimaOrders getSpec() {
        return spec;
    }

    @Override
    public IArimaMapping<SarimaModel> stationaryMapping() {
        if (spec.isStationary())
            return this;
        return ofStationary(spec);
    }

}
