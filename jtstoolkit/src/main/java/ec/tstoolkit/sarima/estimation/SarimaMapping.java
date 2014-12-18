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
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.Complex;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaMapping implements IParametricMapping<SarimaModel> {

    static final double RTOL = 1.1;
    static final double REPS = 0.01;
    static final double MAX = 0.99999;
    static final double RMAX = 0.985;
    public static final double STEP1 = Math.sqrt(1.19e-7);
    public static final double STEP2 = 1e-6;
    public static final double STEP3 = Math.sqrt(2.220446e-16);
    private double rmax_ = 1;

    private static boolean checkStability(double d) {
        return Math.abs(d) < 1;
    }

//    private static boolean checkQuasiStability(double d, double rmax) {
//        return Math.abs(d) < rmax;
//    }
//
//    private static boolean checkQuasiStability(double a, double b, double rmax) {
//        double ro = b * b - 4 * a;
//        if (ro < 0) {
//            return Math.abs(a) < rmax * rmax;
//        } else {
//            double sro = Math.sqrt(ro);
//            double r = (-b + sro) / (2 * a);
//            if (Math.abs(1 / r) >= rmax) {
//                return false;
//            }
//            r = (-b - sro) / (2 * a);
//            return Math.abs(1 / r) < rmax;
//        }
//    }
//    // a*x*x + b*x + 1
    private static boolean checkStability(double a, double b) {
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

    static boolean checkStability(IReadDataBlock c, int start, int nc) {
        while (nc > 0 && c.get(start + nc - 1) == 0) {
            --nc;
        }
        if (nc == 0) {
            return true;
        }
        if (nc == 1) {
            return checkStability(c.get(start));
        }
        if (nc == 2) {
            return checkStability(c.get(start + 1), c.get(start));
        }

        return ec.tstoolkit.maths.linearfilters.Utilities.checkStability(c.rextract(start, nc));
    }

//    private static boolean checkQuasiStability(IReadDataBlock c, int start, int nc, double rtol) {
//        if (nc == 0) {
//            return true;
//        }
//        if (nc == 1) {
//            return checkQuasiStability(c.get(start), rtol);
//        }
//        if (nc == 2) {
//            return checkQuasiStability(c.get(start + 1), c.get(start), rtol);
//        }
//
//        return ec.tstoolkit.maths.linearfilters.Utilities.checkQuasiStability(c.rextract(start, nc), rtol);
//    }
    private static boolean stabilize(boolean all, boolean model,
            SarimaSpecification spec, IDataBlock p, double rmax) {
        boolean rslt = false;
        if (spec.getP() > 0 && stabilize(model, p, 0, spec.getP(), rmax)) {
            rslt = true;
        }
        if (spec.getBP() > 0 && stabilize(model, p, spec.getP(), spec.getBP(), rmax)) {
            rslt = true;
        }
        if (all && spec.getQ() > 0
                && stabilize(model, p, spec.getP() + spec.getBP(), spec.getQ(), rmax)) {
            rslt = true;
        }
        if (all
                && spec.getBQ() > 0
                && stabilize(model, p,
                        spec.getP() + spec.getBP() + spec.getQ(), spec.getBQ(), rmax)) {
            rslt = true;
        }
        return rslt;
    }

    private static boolean stabilize(boolean model, IDataBlock c, int start,
            int nc, double rmax) {
        if (nc == 0) {
            return false;
        }
        if (checkStability(c, start, nc)) {
            return false;
        }
        if (nc == 1) {
            double c0 = c.get(start);
            double cabs = Math.abs(c0);

            if (!model && rmax < 1 && Math.abs(cabs - 1) <= REPS) {
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
        Polynomial sp = stabilize(model, p, rmax);
        if (p != sp) {
            for (int i = 0; i < nc; ++i) {
                c.set(start + i, sp.get(1 + i));
            }
            return true;
        }
        return false;
    }

    private static Polynomial stabilize(boolean model, Polynomial p, double rmax) {
        if (p == null) {
            return null;
        }

        Complex[] roots = p.roots();
        boolean changed = false;
        for (int i = 0; i < roots.length; ++i) {
            Complex root = roots[i];
            double n = 1 / roots[i].abs();
            if (!model && rmax < 1 && Math.abs(n - 1) <= REPS) {
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
     *
     * @param m
     * @return
     */
    public static boolean stabilize(SarimaModel m) {
        DataBlock np = new DataBlock(m.getParameters());
        if (stabilize(true, true, m.getSpecification(), np, RMAX)) {
            m.setParameters(np);
            return true;
        } else {
            return false;
        }
    }
    private boolean m_all;
    /**
     *
     */
    public final SarimaSpecification spec;
    private final double eps_;

    public double getRmax() {
        return rmax_;
    }

    public void setRmax(double value) {
        rmax_ = value;
    }

    /**
     *
     * @param spec
     * @param all
     */
    public SarimaMapping(SarimaSpecification spec, boolean all) {
        this.spec = spec;
        m_all = all;
        eps_ = STEP3; // step(spec);
    }

    public SarimaMapping(SarimaSpecification spec, double eps, boolean all) {
        this.spec = spec;
        m_all = all;
        eps_ = eps;
    }

    /**
     *
     * @param value
     */
    public void checkAll(boolean value) {
        m_all = value;
    }

    /**
     *
     * @param p
     * @return
     */
    @Override
    public boolean checkBoundaries(IReadDataBlock p) {
        if (spec.getP() > 0 && !checkStability(p, 0, spec.getP())) {
            return false;
        }
        if (spec.getBP() > 0 && !checkStability(p, spec.getP(), spec.getBP())) {
            return false;
        }
        if (spec.getQ() > 0) {
            if (m_all && !checkStability(p, spec.getP() + spec.getBP(), spec.getQ())) {
                return false;
            }
//            if (! m_all && !checkQuasiStability(p, spec.getP() + spec.getBP() + spec.getQ(),
//                    spec.getBQ(), RTOL)) {
//                return false;
//            }
        }
        if (spec.getBQ() > 0) {
            if (m_all && !checkStability(p, spec.getP() + spec.getBP() + spec.getQ(),
                    spec.getBQ())) {
                return false;
            }
//            if (! m_all && !checkQuasiStability(p, spec.getP() + spec.getBP() + spec.getQ(),
//                    spec.getBQ(), RTOL)) {
//                return false;
//            }
        }
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
//            return STEP;
//           return -STEP * Math.min(-0.1, p);
            return eps_ * Math.max(1, -p);
        } else {
//            return -STEP;
//           return -STEP * Math.max(0.1, p);
            return -eps_ * Math.max(1, p);
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
        return m_all;
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
        if (spec.getBP() > 0) {
            if (idx < spec.getBP()) {
                if (spec.getBP() == 1) {
                    return -MAX;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getBP();
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
        if (spec.getBQ() == 1) {
            return -MAX;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public SarimaModel map(IReadDataBlock p) {
        if (p.getLength() != spec.getParametersCount()) {
            return null;
        }
        SarimaModel m = new SarimaModel(spec);
        m.setParameters(p);
        return m;
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public IReadDataBlock map(SarimaModel t) {
        SarimaSpecification curspec = t.getSpecification();
        if (curspec.getP() != spec.getP() || curspec.getQ() != spec.getQ()
                || curspec.getBP() != spec.getBP()
                || curspec.getBQ() != spec.getBQ()) {
            return null;
        }
        return t.getParameters();
    }

    /**
     *
     * @param p
     * @return
     */
    public boolean stabilize(IDataBlock p) {
        if (stabilize(m_all, false, spec, p, rmax_)) {
            return true;
        } else {
            return false;
        }
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
        if (spec.getBP() > 0) {
            if (idx < spec.getBP()) {
                if (spec.getBP() == 1) {
                    return MAX;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            idx -= spec.getBP();
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
        if (spec.getBQ() == 1) {
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
    public ParamValidation validate(IDataBlock value) {
        if (value.getLength() != spec.getParametersCount()) {
            return ParamValidation.Invalid;
        }
        if (stabilize(true, true, spec, value, rmax_)) {
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
    public String getDescription(final int idx) {
        return DefaultSarimaMapping.getDescription(spec, idx);
    }

}
