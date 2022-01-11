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
package jdplus.fractionalairline;

import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.math.functions.ParamValidation;
import jdplus.math.linearfilters.BackFilter;
import jdplus.arima.estimation.IArimaMapping;
import demetra.data.DoubleSeq;

public class MultiPeriodicAirlineMapping implements IArimaMapping<ArimaModel> {

    private final double[] f0, f1;
    private final int[] p0;
    private final boolean round;
    private final boolean stationary;
    private final int nur1;
    private final boolean ar;

    // internal stationary mapping
    private MultiPeriodicAirlineMapping(double[] f0, double[] f1, int[] p0, boolean round, boolean ar) {
        this.f0 = f0;
        this.f1 = f1;
        this.p0 = p0;
        this.round = round;
        this.stationary = true;
        this.nur1 = 0;
        this.ar = ar;
    }

    public MultiPeriodicAirlineMapping(double[] periods) {
        this(periods, false, 0, false);
    }

    /**
     *
     * @param periods
     * @param round
     * @param nur1
     * @param ar 
     */
    public MultiPeriodicAirlineMapping(double[] periods, boolean round, int nur1, boolean ar) {
        this.round = round;
        this.stationary = false;
        this.nur1 = nur1;
        p0 = new int[periods.length];
        f0 = new double[periods.length];
        f1 = new double[periods.length];
        if (!round) {
            for (int i = 0; i < periods.length; ++i) {
                p0[i] = (int) periods[i];
                f1[i] = periods[i] - p0[i];
                f0[i] = 1 - f1[i];
            }
        } else {
            for (int i = 0; i < periods.length; ++i) {
                p0[i] = (int) (periods[i] + .5);
                f1[i] = f0[i] = 0;
            }
        }
        this.ar = ar;
    }

    private BackFilter ur1Filter() {
        if (nur1 == 0) {
            return BackFilter.ONE;
        }
        int nd;
        if (nur1 < 0) {
            nd = ar ? p0.length : p0.length + 1;
        } else {
            nd = nur1;
        }
        BackFilter fd = BackFilter.D1;
        for (int i = 1; i < nd; ++i) {
            fd = fd.times(BackFilter.D1);
        }
        return fd;
    }

    @Override
    public ArimaModel map(DoubleSeq p) {
        double[] phi = null, theta = null;
        if (ar) {
            phi = new double[]{1, -p.get(0)};
        } else {
            theta = new double[]{1, -p.get(0)};
        }

        BackFilter fma = theta == null ? BackFilter.ONE : BackFilter.ofInternal(theta),
                far = phi == null ? BackFilter.ONE : BackFilter.ofInternal(phi),
                fs = BackFilter.ONE, fd = BackFilter.ONE;

        for (int i = 0; i < p0.length; ++i) {
            boolean frac = f1[i] != 0;
            double[] dma = new double[frac ? p0[i] + 2 : p0[i] + 1];
            dma[0] = 1;
            if (frac) {
                dma[p0[i]] = -f0[i] * p.get(i + 1);
                dma[p0[i] + 1] = -f1[i] * p.get(i + 1);
                double[] s = new double[p0[i] + 1];
                for (int j = 0; j < p0[i]; ++j) {
                    s[j] = 1;
                }
                s[p0[i]] = f1[i];
                fs = fs.times(BackFilter.ofInternal(s));
            } else {
                dma[p0[i]] = -p.get(i + 1);
                if (!stationary) {
                    double[] d = new double[p0[i]];
                    for (int j = 0; j < d.length; ++j) {
                        d[j] = 1;
                    }
                    fd = fd.times(BackFilter.ofInternal(d));
                }
            }
            fma = fma.times(BackFilter.ofInternal(dma));
        }
        if (!stationary) {
            fd = fd.times(ur1Filter());
        }
        return new ArimaModel(fs.times(far), fd, fma, 1);
    }

    @Override
    public DoubleSeq parametersOf(ArimaModel t) {
        BackFilter theta = t.getMa();
        BackFilter phi = t.getAr();
        double[] p = new double[p0.length + 1];
        if (ar) {
            double ph = -phi.get(1);
            p[0] = ph;
            for (int i = 0; i < p0.length; ++i) {
                if (!round) {
                    p[i + 1] = -theta.get(p0[i]-1) / f0[i];
                } else {
                    p[i + 1] = -theta.get(p0[i]-1);
                }

            }
        } else {
            double th = -theta.get(1);
            p[0] = th;
            for (int i = 0; i < p0.length; ++i) {
                if (!round) {
                    p[i + 1] = -theta.get(p0[i]) / f0[i];
                } else {
                    p[i + 1] = -theta.get(p0[i]);
                }
            }
        }
        return DoubleSeq.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        return inparams.allMatch(x -> Math.abs(x) <= .999);
    }

    @Override
    public double epsilon(DoubleSeq inparams, int idx) {
        return inparams.get(idx) > 0 ? -1e-6 : 1e-6;
    }

    @Override
    public int getDim() {
        return f0.length + 1;
    }

    @Override
    public double lbound(int idx) {
        return -1;
    }

    @Override
    public double ubound(int idx) {
        return 1;
    }

    private final static double UB = .99;

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        boolean changed = false;
        for (int i = 0; i < ioparams.length(); ++i) {
            double p = ioparams.get(i);
            if (Math.abs(p) > 1 / UB) {
                ioparams.set(i, 1 / p);
                changed = true;
            } else if (Math.abs(p) > UB) {
                ioparams.set(i, p < 0 ? -UB : UB);
                changed = true;
            }

        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "p" + idx;
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] p = new double[getDim()];
        for (int i = 0; i < p.length; ++i) {
            p[i] = .2;
        }
        return DoubleSeq.of(p);
    }

    @Override
    public IArimaMapping<ArimaModel> stationaryMapping() {
        return stationary ? this : new MultiPeriodicAirlineMapping(f0, f1, p0, round, ar);
    }
}
