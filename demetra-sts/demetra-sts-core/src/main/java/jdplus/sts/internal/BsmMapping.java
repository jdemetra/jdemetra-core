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
package jdplus.sts.internal;

import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import demetra.data.Parameter;
import demetra.design.Development;
import jdplus.maths.functions.IParametricMapping;
import jdplus.maths.functions.ParamValidation;
import demetra.sts.Component;
import demetra.sts.BsmSpec;
import demetra.data.DoubleSeq;
import jdplus.sts.BasicStructuralModel;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmMapping implements IParametricMapping<BasicStructuralModel> {

    static final double STEP = 1e-6, STEP2 = 1e-4;
    private static double RMIN = 0, RMAX = 0.999, RDEF = 0.5;
    private static double PMIN = .25, PMAX = 2.5, PDEF = 1;

    private static final Component[] CMPS = {Component.Level, Component.Slope, Component.Seasonal, Component.Noise, Component.Cycle};

    /**
     *
     */
    public enum Transformation {

        /**
         *
         */
        None,
        /**
         *
         */
        Exp,
        /**
         *
         */
        Square
    }

    private Component cFixed = Component.Undefined;

    /**
     *
     */
    public final Transformation transformation;

    /**
     *
     */
    public final BsmSpec spec;

    /**
     *
     */
    public final int freq;

    /**
     *
     * @param spec
     * @param freq
     */
    public BsmMapping(BsmSpec spec, int freq) {
        transformation = Transformation.Square;
        this.spec = spec;
        this.freq = freq;
    }

    /**
     *
     * @param spec
     * @param freq
     * @param tr
     */
    public BsmMapping(BsmSpec spec, int freq, Transformation tr) {
        this.transformation = tr;
        this.spec = spec;
        this.freq = freq;
    }

    public int getVarsCount() {
        int n = 0;
        for (int i = 0; i < CMPS.length; ++i) {
            if (hasFreeComponent(CMPS[i])) {
                ++n;
            }
        }
        return n;
    }

    public boolean hasCycleDumpingFactor() {
        return spec.hasCycle() && spec.getCycleDumpingFactor() == 0;
    }

    public boolean hasCycleLength() {
        return spec.hasCycle() && spec.getCycleLength() == 0;
    }

    public boolean hasFreeComponent(Component cmp) {
        return cFixed != cmp && spec.hasFreeComponent(cmp);
    }

    int pCycle() {
        if (!spec.hasCycle()) {
            return 0;
        }
        int n = 0;
        if (spec.getCycleDumpingFactor() == 0) {
            ++n;
        }
        if (spec.getCycleLength() == 0) {
            ++n;
        }
        return n;
    }

    @Override
    public boolean checkBoundaries(DoubleSeq p) {
        int pc = pCycle();
        int nvar = p.length() - pc;
        if (transformation == Transformation.None) {
            for (int i = 0; i < nvar; ++i) {
                if (p.get(i) <= 0) {
                    return false;
                }
            }
        } else if (transformation == Transformation.Square) {
            for (int i = 0; i < nvar; ++i) {
                if (p.get(i) < -.1 || p.get(i) > 10) {
                    return false;
                }
            }
        }
        if (pc > 0) {
            // rho
            if (hasCycleDumpingFactor()) {
                double rho = p.get(nvar++);
                if (rho < RMIN || rho > RMAX) {
                    return false;
                }
            }
            if (hasCycleLength()) {
                double period = p.get(nvar);
                if (period < PMIN || period > PMAX) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSeq p, int idx) {
        int pc = pCycle();
        int nvar = p.length() - pc;
        if (idx < nvar) {
            double x = p.get(idx);
            if (x < .5) {
                return STEP;
            } else {
                return -STEP;
            }
        } else if (idx == nvar && hasCycleDumpingFactor()) {
            double x = p.get(idx);
            if (x < .5) {
                return STEP;
            } else {
                return -STEP;
            }
        } else {
            double x = p.get(idx);
            if (x < 1) {
                return STEP2;
            } else {
                return -STEP2;
            }
        }
    }

    /**
     *
     * @param idx
     * @return
     */
    public Component getComponent(final int idx) {
        int cur = idx;
        for (int i = 0; i < CMPS.length; ++i) {
            if (hasFreeComponent(CMPS[i])) {
                if (cur == 0) {
                    return CMPS[i];
                }
                --cur;
            }
        }
        return Component.Undefined;
    }

    @Override
    public int getDim() {
        return getVarsCount() + pCycle();
    }

    /**
     *
     * @return
     */
    public Component getFixedComponent() {
        return cFixed;
    }

    private double inparam(double d) {
        switch (transformation) {
            case None:
                return d;
            case Square:
                return d * d;
            default:
                return Math.exp(2 * d);
        }
    }

    @Override
    public double lbound(int idx) {
        return transformation == Transformation.None ? 0
                : Double.NEGATIVE_INFINITY;
    }

    public DoubleSeq map(BasicStructuralModel t) {
        double[] p = new double[getDim()];
        int idx = 0;

        for (int i = 0; i < CMPS.length; ++i) {
            if (hasFreeComponent(CMPS[i])) {
                p[idx++] = outparam(t.getVariance(CMPS[i]));
            }
        }
        if (spec.hasCycle()) {
            double pm = spec.getCycleDumpingFactor();
            if (pm == 0) {
                p[idx++] = t.getCyclicalDumpingFactor();
            }
            pm = spec.getCycleLength();
            if (pm == 0) {
                p[idx++] = t.getCyclicalPeriod() / (6 * freq);
            }
        }
        return DoubleSeq.of(p);
    }

    @Override
    public BasicStructuralModel map(DoubleSeq p) {
        BasicStructuralModel t = new BasicStructuralModel(spec, freq);
        int idx = 0;
        DoubleSeqCursor reader = p.cursor();
        for (int i = 0; i < CMPS.length; ++i) {
            if (hasFreeComponent(CMPS[i])) {
                t.setVariance(CMPS[i], inparam(reader.getAndNext()));
            }
        }
        if (spec.hasCycle()) {
            double cdump, clen;
            double pm = spec.getCycleDumpingFactor();
            if (pm == 0) {
                cdump = reader.getAndNext();
            } else {
                cdump = pm;
            }
            pm = spec.getCycleLength();
            if (pm == 0) {
                clen = 6 * freq * reader.getAndNext();
            } else {
                clen = freq * pm;
            }
            t.setCycle(cdump, clen);
        }
        if (cFixed != Component.Undefined) {
            t.setVariance(cFixed, 1);
        }
        return t;
    }

    private double outparam(double d) {
        switch (transformation) {
            case None:
                return d;
            case Square:
                return d <= 0 ? 0 : Math.sqrt(d);
            default:
                return .5 * Math.log(d);
        }
    }

    /**
     *
     * @param value
     */
    public void setFixedComponent(Component value) {
        cFixed = value;
    }

    @Override
    public double ubound(int idx) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        ParamValidation status = ParamValidation.Valid;
        if (ioparams.length() == 0) {
            return ParamValidation.Valid;
        }
        int pc = pCycle();
        int nvar = ioparams.length() - pc;
        if (transformation == Transformation.Square) {
            for (int i = 0; i < nvar; ++i) {
                if (ioparams.get(i) > 10) {
                    ioparams.set(i, 10);
                    status = ParamValidation.Changed;
                } else if (ioparams.get(i) < -0.1) {
                    ioparams.set(i, Math.min(10, -ioparams.get(i)));
                    status = ParamValidation.Changed;
                }
            }
        } else if (transformation == Transformation.None) {
            for (int i = 0; i < nvar; ++i) {
                if (ioparams.get(i) < 1e-9) {
                    ioparams.set(i, 1e-9);
                    status = ParamValidation.Changed;
                }
            }
        }
        if (pc > 0) {
            if (hasCycleDumpingFactor()) {
                double rho = ioparams.get(nvar);
                if (rho < RMIN) {
                    ioparams.set(nvar, 0.1);
                    status = ParamValidation.Changed;
                }
                if (rho > RMAX) {
                    ioparams.set(nvar, .9);
                    status = ParamValidation.Changed;
                }
                ++nvar;
            }
            if (hasCycleLength()) {
                double p = ioparams.get(nvar);
                if (p < PMIN) {
                    ioparams.set(nvar, PMIN);
                    status = ParamValidation.Changed;
                }
                if (p > PMAX) {
                    ioparams.set(nvar, PMAX);
                    status = ParamValidation.Changed;
                }
            }
        }
        return status;
    }

    @Override
    public String getDescription(final int idx) {
        int n = getVarsCount();
        if (idx < n) {
            return getComponent(idx).name() + " var.";
        }
        if (idx == n && this.hasCycleDumpingFactor()) {
            return "Cycle dumping factor";
        } else {
            return "Cycle length";
        }
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] x = new double[getDim()];
        for (int i = 0; i < x.length; ++i) {
            x[i] = outparam(.2);
        }
        return DoubleSeq.of(x);
    }
}
