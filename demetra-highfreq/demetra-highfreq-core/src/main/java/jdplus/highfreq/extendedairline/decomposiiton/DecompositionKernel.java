/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.highfreq.extendedairline.decomposiiton;

import jdplus.highfreq.extendedairline.ExtendedAirlineKernel;
import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.highfreq.DecompositionSpec;
import demetra.highfreq.ExtendedAirlineDictionaries;
import demetra.highfreq.SeriesComponent;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import java.util.Arrays;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.data.DataBlockStorage;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.ExtendedSsfData;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import jdplus.ssf.arima.SsfUcarima;

/**
 *
 * @author PALATEJ
 */
public class DecompositionKernel {

    public static final String CD = "canonical decomposition";

    private final DecompositionSpec spec;

    public DecompositionKernel(DecompositionSpec spec) {
        this.spec = spec;
    }

    public ExtendedAirlineDecomposition process(DoubleSeq lin, boolean mul, ProcessingLog log) {
        try {
            log.push(CD);
            double[] periodicities = spec.getPeriodicities().clone();
            if (!spec.isIterative() && periodicities.length > 1) {
                throw new java.lang.UnsupportedOperationException("Not implemented yet");
            }
            if (spec.isAdjustToInt()){
                for (int i=0; i<periodicities.length; ++i)
                    periodicities[i]=Math.round(periodicities[i]);
            }

            int nb = spec.getBackcastsCount(), nf = spec.getForecastsCount();
            ExtendedAirlineDecomposition.Builder builder = ExtendedAirlineDecomposition.builder()
                    .multiplicative(mul)
                    .backcastsCount(nb)
                    .forecastsCount(nf);
            Arrays.sort(periodicities);
            DoubleSeq cur = lin;
            ExtendedAirlineDecomposition.Step[] steps = new ExtendedAirlineDecomposition.Step[periodicities.length];
            for (int i = 0; i < periodicities.length; ++i) {
                ExtendedAirlineDecomposition.Step.Builder sbuilder = ExtendedAirlineDecomposition.Step.builder();
                // we re-estimate the model
                double curp = periodicities[i];
                ArimaModel arima = ExtendedAirlineKernel.estimate(cur, curp);
                UcarimaModel ucm = ucm(arima, false);
                if (ucm.getComponentsCount() == 2) {
                    log.warning("non decomposable model", curp);
                }
                sbuilder.period(curp)
                        .data(cur)
                        .model(arima)
                        .ucarimaModel(ucm);
                CompositeSsf ssf = SsfUcarima.of(ucm);
                ISsfData data = new ExtendedSsfData(new SsfData(cur), nb, nf);
                int[] pos = ssf.componentsPosition();

                boolean done = false;
                if (spec.isStdev()) {
                    try {
                        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, data, true, true);
                        for (int j = 0; j < pos.length; ++j) {
                            sbuilder.component(new SeriesComponent("cmp" + (j + 1),
                                    sr.getComponent(pos[j]).commit(),
                                    sr.getComponentVariance(j).fn(a -> a <= 0 ? 0 : Math.sqrt(a)), 
                                    ComponentType.Undefined));
                        }
                        done = true;
                    } catch (OutOfMemoryError err) {
                        sbuilder.clearComponents();
                    }
                }
                if (!done) {
                    DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
                    for (int j = 0; j < pos.length; ++j) {
                        sbuilder.component(new SeriesComponent("cmp" + (j + 1),
                                ds.item(pos[j]).commit(), DoubleSeq.empty(), ComponentType.Undefined));
                    }
                }
                ExtendedAirlineDecomposition.Step step = sbuilder.build();
                builder.step(step);
                steps[i] = step;
                // take the series for the next step. That's the "seasonally adjusted series, which corresponds to y-s
                cur = DoublesMath.subtract(cur, step.getComponent(1).getData().drop(nb, nf));
            }
            // final decomposition, with simple bias correction (in case of log-transformation)

            DoubleSeq t = steps[steps.length - 1].getComponent(0).getData();
            if (mul) {
                t = t.exp();
            }

            DoubleSeq s = null;
            double bias = 1;

            int n = lin.length();
            for (int i = 0; i < steps.length; ++i) {
                DoubleSeq curs = steps[i].getComponent(1).getData();
                if (mul) {
                    curs = curs.exp();
                    if (spec.isBiasCorrection()) {
                        double f = steps[i].getPeriod();
                        int np = (int) (f * (int) (n / f));
                        double sbias = curs.range(nb, nb + np).average();
                        curs = curs.fastOp(z -> z / sbias);
                        bias *= sbias;
                    }
                    s = DoublesMath.multiply(s, curs);
                } else {
                    s = DoublesMath.add(s, curs);
                }
                if (steps[i].getPeriod() == 7) {
                    builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.SW_CMP, curs, ComponentType.Seasonal));
                } else {
                    builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.SY_CMP, curs, ComponentType.Seasonal));
                }
            }
            builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.S_CMP, s, ComponentType.Seasonal));

            SeriesComponent cmp = steps[steps.length - 1].getComponent(2);
            DoubleSeq irr = cmp == null ? null : cmp.getData();
            if (irr != null) {
                if (mul) {
                    irr = irr.exp();
                    if (spec.isBiasCorrection()) {
                        double ibias = irr.range(nb, nb + n).average();
                        irr = irr.fastOp(z -> z / ibias);
                        bias *= ibias;
                    }
                }
                builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.I_CMP, irr, ComponentType.Irregular));
            }
            if (bias != 1) {
                double tbias = bias;
                t = t.fn(z -> z * tbias);
            }
            builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.T_CMP, t, ComponentType.Trend));

            DoubleSeq sa = mul ? DoublesMath.multiply(t, irr) : DoublesMath.add(t, irr);
            DoubleSeq y = mul ? DoublesMath.multiply(sa, s) : DoublesMath.add(sa, s);

            builder.finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.Y_CMP, y, ComponentType.Series))
                    .finalComponent(new SeriesComponent(ExtendedAirlineDictionaries.SA_CMP, sa, ComponentType.SeasonallyAdjusted));

            return builder.build();
        } finally {
            log.pop();
        }
    }

    public static UcarimaModel ucm(IArimaModel arima, boolean sn) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        if (sn) {
            ucm = ucm.setVarianceMax(0, true);
        } else {
            ucm = ucm.setVarianceMax(-1, true);
        }
        return ucm.simplify();
    }

}
