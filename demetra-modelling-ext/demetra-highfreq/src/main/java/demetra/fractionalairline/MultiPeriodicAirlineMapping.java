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
package demetra.fractionalairline;

import demetra.arima.ArimaModel;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.maths.linearfilters.BackFilter;

public class MultiPeriodicAirlineMapping implements IParametricMapping<ArimaModel> {

    private final double[] f0, f1;
    private final int[] p0;
    private final boolean adjust;
    private final boolean stationary;

    public MultiPeriodicAirlineMapping(double[] periods, boolean adjust, boolean stationary) {
        this.adjust = adjust;
        this.stationary = stationary;
        p0 = new int[periods.length];
        f0 = new double[periods.length];
        f1 = new double[periods.length];
        if (adjust) {
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
    }

    @Override
    public ArimaModel map(DoubleSequence p) {
        double th = p.get(0);
        double[] ma = new double[]{1, -th};
        BackFilter fma = BackFilter.ofInternal(ma), fs = BackFilter.ONE,
                fd = stationary ? BackFilter.ONE : BackFilter.D1;
        for (int i = 0; i < p0.length; ++i) {
            boolean frac=adjust && f1[i] != 0;
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
                if (!stationary) {
                    fd = fd.times(BackFilter.D1);
                }
            } else {
                dma[p0[i]] = -p.get(i + 1);
                if (!stationary) {
                    double[] d = new double[p0[i] + 1];
                    d[0] = 1;
                    d[p0[i]] = -1;
                    fd = fd.times(BackFilter.ofInternal(d));
                }
            }
            fma = fma.times(BackFilter.ofInternal(dma));
        }
        return new ArimaModel(fs, fd, fma, 1);
    }

    @Override
    public DoubleSequence map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[p0.length + 1];
        p[0] = -ma.get(1);
        for (int i = 0; i < p0.length; ++i) {
            if (adjust) {
                p[i + 1] = -ma.get(p0[i]) / f0[i];
            } else {
                p[i + 1] = -ma.get(p0[i]);
            }
        }
        return DoubleSequence.of(p);
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        return inparams.allMatch(x -> Math.abs(x) < .999);
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        return 1e-6;
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

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        boolean changed = false;
        for (int i = 0; i < ioparams.length(); ++i) {
            double p = ioparams.get(i);
            if (Math.abs(p) >= .999) {
                ioparams.set(i, 1 / p);
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
    public DoubleSequence getDefaultParameters() {
        double[] p = new double[getDim()];
        for (int i = 0; i < p.length; ++i) {
            p[i] = .2;
        }
        return DoubleSequence.ofInternal(p);
    }
}
