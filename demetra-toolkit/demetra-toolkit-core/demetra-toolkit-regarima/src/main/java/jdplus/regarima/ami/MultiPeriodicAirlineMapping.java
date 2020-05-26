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
package jdplus.regarima.ami;

import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.math.functions.ParamValidation;
import jdplus.math.linearfilters.BackFilter;
import jdplus.arima.estimation.IArimaMapping;
import demetra.data.DoubleSeq;

class MultiPeriodicAirlineMapping implements IArimaMapping<ArimaModel> {

    private final int[] periods;
    private final boolean stationary;

    public MultiPeriodicAirlineMapping(int[] periods) {
        this(periods, false);
    }

    public MultiPeriodicAirlineMapping(int[] periods, boolean stationary) {
        this.stationary = stationary;
        this.periods = periods.clone();
    }

    @Override
    public ArimaModel map(DoubleSeq p) {
        double th = p.get(0);
        double[] ma = new double[]{1, -th};
        BackFilter fma = BackFilter.ofInternal(ma), fs = BackFilter.ONE,
                fd = stationary ? BackFilter.ONE : BackFilter.D1;
        for (int i = 0; i < periods.length; ++i) {
            double[] dma = new double[periods[i] + 1];
            dma[0] = 1;
            dma[periods[i]] = -p.get(i + 1);
            if (!stationary) {
                double[] d = new double[periods[i] + 1];
                d[0] = 1;
                d[periods[i]] = -1;
                fd = fd.times(BackFilter.ofInternal(d));
            }
            fma = fma.times(BackFilter.ofInternal(dma));
        }
        return new ArimaModel(fs, fd, fma, 1);
    }

    @Override
    public DoubleSeq parametersOf(ArimaModel t) {
        BackFilter ma = t.getMa();
        double[] p = new double[periods.length + 1];
        p[0] = -ma.get(1);
        for (int i = 0; i < periods.length; ++i) {
            p[i + 1] = -ma.get(periods[i]);
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
        return periods.length + 1;
    }

    @Override
    public double lbound(int idx) {
        return -1;
    }

    @Override
    public double ubound(int idx) {
        return 1;
    }

    private final static double UB = .999;

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
        return stationary ? this : new MultiPeriodicAirlineMapping(periods, true);
    }
}
