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

import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Parameter;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.SeasonalModel;
import jdplus.data.DataBlock;
import jdplus.math.functions.IParametricMapping;
import jdplus.math.functions.ParamValidation;
import jdplus.sts.BsmData;
import nbbrd.design.Development;

/**
 * Order of the parameters:
 * lvar, svar, seasvar, nvar, cvar, cdump, clength
 * Unused parameters are set to -1 (they are always fixed)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class BsmMapping implements IParametricMapping<BsmData> {

    static final double STEP = 1e-6, STEP2 = 1e-4;
    private static double RMIN = 0.25, RMAX = 0.999;
    private static double PMIN = .5, PMAX = 3, PRATIO=6;

    private static final int L = 0, S = 1, SEAS = 2, N = 3, C = 4, CDUMP = 5, CLEN = 6, NVARS = 5, NP = 7;

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

    private final double[] p = new double[NP];
    private final boolean[] fp = new boolean[NP];
    private final int period, nvars;
    private final SeasonalModel sm;
    private final double rmin;
    public final Transformation transformation;

    /**
     *
     * @param spec
     * @param period
     * @param fixedVar
     */
    public BsmMapping(BsmSpec spec, int period, Component fixedVar) {
        this(spec, period, fixedVar, Transformation.Square);
    }

    /**
     *
     * @param spec
     * @param period
     * @param fixedVar
     * @param tr
     */
    public BsmMapping(BsmSpec spec, int period, Component fixedVar, Transformation tr) {
        this.transformation = tr;
        this.period = period;
        this.sm = spec.getSeasonalModel();
        init(spec.getLevelVar(), L);
        init(spec.getSlopeVar(), S);
        init(spec.getSeasonalVar(), SEAS);
        init(spec.getNoiseVar(), N);
        init(spec.getCycleVar(), C);
        init(spec.getCycleDumpingFactor(), CDUMP);
        init(spec.getCycleLength(), CLEN);
        int vp = varPos(fixedVar);
        if (vp >= 0) {
            fp[vp] = true;
        }
        int n = 0;
        for (int i = 0; i < NVARS; ++i) {
            if (!fp[i]) {
                ++n;
            }
        }
        nvars=n;
        rmin=Math.pow(RMIN, 1.0/period);
    }

    private static int varPos(Component cmp) {
        if (cmp == null) {
            return -1;
        }
        switch (cmp) {
            case Level:
                return L;
            case Slope:
                return S;
            case Seasonal:
                return SEAS;
            case Noise:
                return N;
            case Cycle:
                return C;
            default:
                return -1;
        }
    }

    private void init(Parameter param, int idx) {
        if (param == null) {
            p[idx] = -1;
            fp[idx] = true;
        } else {
            p[idx] = param.getValue();
            fp[idx] = param.isFixed();
        }
    }

    public int varsCount() {
        return nvars;
    }
    
    @Override
    public boolean checkBoundaries(DoubleSeq seq) {
        if (transformation == Transformation.None) {
            for (int i = 0; i < nvars; ++i) {
                if (seq.get(i) <= 0) {
                    return false;
                }
            }
        } else if (transformation == Transformation.Square) {
            for (int i = 0; i < nvars; ++i) {
                if (seq.get(i) < -.1 || seq.get(i) > 10) {
                    return false;
                }
            }
        }

        // rho
        int n=nvars;
        if (!fp[CDUMP]) {
            double rho = seq.get(n++);
            if (rho < rmin || rho > RMAX) {
                return false;
            }
        }
        if (!fp[CLEN]) {
            double np = seq.get(n);
            if (np < PMIN || np > PMAX) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSeq seq, int idx) {
        if (idx < nvars) {
            double x = seq.get(idx);
            if (x < .5) {
                return STEP;
            } else {
                return -STEP;
            }
        } else if (idx == nvars && fp[CDUMP]) {
            double x = seq.get(idx);
            if (x < .5) {
                return STEP;
            } else {
                return -STEP;
            }
        } else {
            double x = seq.get(idx);
            if (x < 1) {
                return STEP2;
            } else {
                return -STEP2;
            }
        }
    }

    @Override
    public int getDim() {
        int n = 0;
        for (int i = 0; i < NP; ++i) {
            if (!fp[i]) {
                ++n;
            }
        }
        return n;
    }

    private double invar(double d) {
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

    public DoubleSeq map(BsmData t) {
        double[] np = new double[getDim()];
        int idx = 0;
        
        if (! fp[L])
            np[idx++]=outvar(t.getLevelVar());
        if (! fp[S])
            np[idx++]=outvar(t.getSlopeVar());
        if (! fp[SEAS])
            np[idx++]=outvar(t.getSeasonalVar());
        if (! fp[N])
            np[idx++]=outvar(t.getNoiseVar());
        if (! fp[C])
            np[idx++]=outvar(t.getCycleVar());
        if (! fp[CDUMP])
            np[idx++]=t.getCycleDumpingFactor();
        if (! fp[CLEN])
            np[idx]=t.getCycleLength()/PRATIO;
        return DoubleSeq.of(np);
    }

    @Override
    public BsmData map(DoubleSeq seq) {
        DoubleSeqCursor cur = seq.cursor();
        return BsmData.builder()
                .period(period)
                .seasonalModel(sm)
                .levelVar(fp[L] ? p[L] : invar(cur.getAndNext()))
                .slopeVar(fp[S] ? p[S] : invar(cur.getAndNext()))
                .seasonalVar(fp[SEAS] ? p[SEAS] : invar(cur.getAndNext()))
                .noiseVar(fp[N] ? p[N] : invar(cur.getAndNext()))
                .cycleVar(fp[C] ? p[C] : invar(cur.getAndNext()))
                .cycleDumpingFactor(fp[CDUMP] ? p[CDUMP] : cur.getAndNext())
                .cycleLength(fp[CLEN] ? p[CLEN] : PRATIO * cur.getAndNext())
                .build();
    }

    private double outvar(double d) {
        switch (transformation) {
            case None:
                return d;
            case Square:
                return d <= 0 ? 0 : Math.sqrt(d);
            default:
                return .5 * Math.log(d);
        }
    }

    @Override
    public double ubound(int idx) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        ParamValidation status = ParamValidation.Valid;
        if (transformation == Transformation.Square) {
            for (int i = 0; i < nvars; ++i) {
                if (ioparams.get(i) > 10) {
                    ioparams.set(i, 10);
                    status = ParamValidation.Changed;
                } else if (ioparams.get(i) < -0.1) {
                    ioparams.set(i, Math.min(10, -ioparams.get(i)));
                    status = ParamValidation.Changed;
                }
            }
        } else if (transformation == Transformation.None) {
            for (int i = 0; i < nvars; ++i) {
                if (ioparams.get(i) < 1e-9) {
                    ioparams.set(i, 1e-9);
                    status = ParamValidation.Changed;
                }
            }
        }
        int n=nvars;
        if (!fp[CDUMP]) {
            double rho = ioparams.get(n);
            if (rho < rmin) {
                ioparams.set(n, (rmin+RMAX)/2);
                status = ParamValidation.Changed;
            }
            else if (rho > RMAX) {
                ioparams.set(n, (rmin+RMAX)/2);
                status = ParamValidation.Changed;
            }
            ++n;
        }
        if (!fp[CLEN]) {
            double pcur = ioparams.get(n);
            if (pcur < PMIN) {
                ioparams.set(n, PMIN);
                status = ParamValidation.Changed;
            }
            else if (pcur > PMAX) {
                ioparams.set(n, PMAX);
                status = ParamValidation.Changed;
            }
        }
        return status;
    }

    @Override
    public String getDescription(final int idx) {
        int j = 0;
        if (!fp[L]) {
            if (j++ == idx) {
                return "level var.";
            }
        }
        if (!fp[S]) {
            if (j++ == idx) {
                return "slope var.";
            }
        }
        if (!fp[SEAS]) {
            if (j++ == idx) {
                return "seasonal var.";
            }
        }
        if (!fp[N]) {
            if (j++ == idx) {
                return "noise var.";
            }
        }
        if (!fp[C]) {
            if (j++ == idx) {
                return "cycle var.";
            }
        }
        if (!fp[CDUMP]) {
            if (j++ == idx) {
                return "Cycle dumping factor";
            }
        }
        if (!fp[CLEN]) {
            if (j == idx) {
                return "Cycle length";
            }
        }
        return "Unexpected";
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] x = new double[getDim()];
        int j = 0;
        for (int i = 0; i < NVARS; ++i) {
            if (!fp[i]) {
                x[j++] = BsmSpec.DEF_VAR;
            }
        }
        if (!fp[CDUMP]) {
            x[j++] = (rmin+RMAX)/2;
        }
        if (!fp[CLEN]) {
            x[j] = BsmSpec.DEF_CLENGTH/6;
        }
        return DoubleSeq.of(x);
    }

    public Component varPosition(final int idx) {
        int j = 0;
        if (!fp[L]) {
            if (j++ == idx) {
                return Component.Level;
            }
        }
        if (!fp[S]) {
            if (j++ == idx) {
                return Component.Slope;
            }
        }
        if (!fp[SEAS]) {
            if (j++ == idx) {
                return Component.Seasonal;
            }
        }
        if (!fp[N]) {
            if (j++ == idx) {
                return Component.Noise;
            }
        }
        if (!fp[C]) {
            if (j == idx) {
                return Component.Cycle;
            }
        }
        return Component.Undefined;
    }
}
