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
import demetra.regarima.IArimaMapping;

public class PeriodicAirlineMapping implements IArimaMapping<ArimaModel> {

    private final double f0, f1;
    private final int p0;
    private final boolean adjust;
    private final boolean stationary;

    private PeriodicAirlineMapping(double f0, double f1, int p0, boolean adjust, boolean stationary) {
        this.f0 = f0;
        this.f1 = f1;
        this.p0 = p0;
        this.adjust = adjust;
        this.stationary = stationary;
    }

    public PeriodicAirlineMapping(double period) {
        this(period, true, false);
    }

    public PeriodicAirlineMapping(double period, boolean adjust, boolean stationary) {
        this.adjust = adjust;
        this.stationary = stationary;
        if (adjust) {
            p0 = (int) period;
            f1 = period - p0;
            f0 = 1 - f1;
        } else {
            p0 = (int) (period + .5);
            f1 = f0 = 0;
        }
    }

    @Override
    public ArimaModel map(DoubleSequence p) {
        double th = p.get(0), bth = p.get(1);
        double[] ma = new double[]{1, -th};
        double[] dma = new double[adjust ? p0 + 2 : p0 + 1];
        dma[0] = 1;
        if (adjust) {
            double[] d = new double[p0 + 2];
            d[0] = 1;
            d[p0] = -f0;
            d[p0 + 1] = -f1;
            dma[p0] = -f0 * bth;
            dma[p0 + 1] = -f1 * bth;
            BackFilter fma = BackFilter.ofInternal(ma).times(BackFilter.ofInternal(dma));
            double[] s = new double[p0 + 1];
            for (int i = 0; i < p0; ++i) {
                s[i] = 1;
            }
            s[p0] = f1;
            if (stationary) {
                return new ArimaModel(BackFilter.ofInternal(s), BackFilter.ONE, fma, 1);
            } else {
                return new ArimaModel(BackFilter.ofInternal(s), BackFilter.ofInternal(1, -2, 1), fma, 1);
            }

        } else {
            double[] d = new double[p0 + 1];
            d[0] = 1;
            d[p0] = -1;
            dma[p0] = -bth;
            BackFilter fma = BackFilter.ofInternal(ma).times(BackFilter.ofInternal(dma));
            if (stationary) {
                return new ArimaModel(BackFilter.ONE, BackFilter.ONE, fma, 1);
            } else {
                return new ArimaModel(BackFilter.ONE, BackFilter.D1.times(BackFilter.ofInternal(d)), fma, 1);
            }
        }
    }

    @Override
    public DoubleSequence map(ArimaModel t) {
        BackFilter ma = t.getMA();
        double[] p = new double[2];
        p[0] = -ma.get(1);
        if (adjust) {
            p[1] = -ma.get(p0) / f0;
        } else {
            p[1] = -ma.get(p0);
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
        return 2;
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
        double p = ioparams.get(0);
        if (Math.abs(p) >= .999) {
            ioparams.set(0, 1 / p);
            changed = true;
        }
        p = ioparams.get(1);
        if (Math.abs(p) >= .999) {
            ioparams.set(1, 1 / p);
            changed = true;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "p" + idx;
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        return DoubleSequence.of(.9, .9);
    }

    @Override
    public IArimaMapping<ArimaModel> stationaryMapping() {
        return stationary ? this : new PeriodicAirlineMapping(f0, f1, p0, adjust, true);
    }
}
