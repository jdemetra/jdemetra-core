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
import ec.tstoolkit.maths.linearfilters.Utilities;
import ec.tstoolkit.maths.polynomials.Polynomial;
import static ec.tstoolkit.maths.realfunctions.IParametersDomain.EMPTY;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import static ec.tstoolkit.sarima.estimation.DefaultSarimaMapping.BPHI;
import static ec.tstoolkit.sarima.estimation.DefaultSarimaMapping.BTH;
import static ec.tstoolkit.sarima.estimation.DefaultSarimaMapping.PHI;
import static ec.tstoolkit.sarima.estimation.DefaultSarimaMapping.TH;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SarimaMapping2 implements IParametricMapping<SarimaModel> {

    static final double REPS = 0.01;
    static final double MAX = 0.99999;
    public static final double ARMAX = 0.96;
    public static final double MAMAX = 0.99;
//    static final double STEP = Math.sqrt(1.19e-7);
//    static final double STEP = Math.sqrt(2.220446e-16);
    static final double STEP = 1e-6;
    private double armax_ = 1;
    private double mamax_ = 1;
    private boolean all_;
    private final SarimaSpecification spec;

    public static boolean stabilize(SarimaModel model, double armax, double mamax) {
        SarimaMapping2 mapping = new SarimaMapping2(model.getSpecification(), true);
        mapping.setArMax(armax);
        mapping.setMaMax(mamax);
        DataBlock p = new DataBlock(model.getParameters());
        if (mapping.stabilize(p)) {
            model.setParameters(p);
            return true;
        } else {
            return false;
        }
    }

    public boolean stabilize(IDataBlock p) {
        boolean rslt = false;
        if (spec.getP() > 0 && stabilize(p, 0, spec.getP(), armax_)) {
            rslt = true;
        }
        if (spec.getBP() > 0 && stabilize(p, spec.getP(), spec.getBP(), armax_)) {
            rslt = true;
        }
        if (all_ && spec.getQ() > 0
                && stabilize(p, spec.getP() + spec.getBP(), spec.getQ(), mamax_)) {
            rslt = true;
        }
        if (all_
                && spec.getBQ() > 0
                && stabilize(p, spec.getP() + spec.getBP() + spec.getQ(), spec.getBQ(), mamax_)) {
            rslt = true;
        }
        return rslt;
    }

    private boolean stabilize(IDataBlock c, int start,
            int nc, double rmax) {
        if (nc == 0) {
            return false;
        }
        if (nc == 1) {
            double c0 = c.get(start);
            double cabs = Math.abs(c0);
            if (cabs <= rmax) {
                return false;
            }

            if (rmax < 1) {
                c.set(start, c0 > 0 ? rmax : -rmax);
            } else {
                c.set(start, 1 / c0);
            }
            return true;
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
            if (n > rmax) {
                if (rmax < 1) {
                    roots[i] = root.times(n / rmax);
                } else if (n > 1) {
                    roots[i] = root.inv();

                }
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

    public double getArMax() {
        return armax_;
    }

    public void setArMax(double value) {
        armax_ = value;
    }

    public double getMaMax() {
        return mamax_;
    }

    public void setMaMax(double value) {
        mamax_ = value;
    }

    /**
     *
     * @param spec
     * @param all
     */
    public SarimaMapping2(SarimaSpecification spec, boolean all) {
        this.spec = spec;
        all_ = all;
    }

    /**
     *
     * @param value
     */
    public void checkAll(boolean value) {
        all_ = value;
    }

    private boolean check(IReadDataBlock p, int start, int length, double rmax) {
        IReadDataBlock ex = p.rextract(start, length);
        if (rmax == 1) {
            return Utilities.checkStability(ex);
        } else {
            return Utilities.checkRoots(ex, 1 / rmax);
        }
    }

    /**
     *
     * @param p
     * @return
     */
    @Override
    public boolean checkBoundaries(IReadDataBlock p) {
        if (spec.getP() > 0 && !check(p, 0, spec.getP(), armax_)) {
            return false;
        }
        if (spec.getBP() > 0 && !check(p, spec.getP(), spec.getBP(), armax_)) {
            return false;
        }
        if (all_ && spec.getQ() > 0
                && !check(p, spec.getP() + spec.getBP(), spec.getQ(), mamax_)) {
            return false;
        }
        if (all_
                && spec.getBQ() > 0
                && !check(p, spec.getP() + spec.getBP() + spec.getQ(),
                        spec.getBQ(), mamax_)) {
            return false;
        }
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
            //           return STEP;
            return -STEP * Math.min(-0.1, p);
        } else {
            //           return -STEP;
            return -STEP * Math.max(0.1, p);
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
        return all_;
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
                    return -armax_;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getP();
        }
        if (spec.getBP() > 0) {
            if (idx < spec.getBP()) {
                if (spec.getBP() == 1) {
                    return -armax_;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getBP();
        }
        if (spec.getQ() > 0) {
            if (idx < spec.getQ()) {
                if (spec.getQ() == 1) {
                    return -mamax_;
                } else {
                    return Double.NEGATIVE_INFINITY;
                }
            }
            idx -= spec.getQ();
        }
        if (spec.getBQ() == 1) {
            return -mamax_;
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
        if (stabilize(value)) {
            return ParamValidation.Changed;
        } else {
            return ParamValidation.Valid;
        }
    }

    @Override
    public String getDescription(final int idx) {
        return DefaultSarimaMapping.getDescription(spec, idx);
    }

}
