/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.x13;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.processing.ProcessingLog;
import demetra.regarima.BasicSpec;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.IUserTsVariable;
import demetra.timeseries.regression.modelling.ModellingContext;
import jdplus.x11.X11Kernel;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.X13Finals;
import demetra.x13.X13Preadjustment;
import demetra.x13.X13Spec;
import jdplus.x13.regarima.FastArimaForecasts;
import jdplus.x13.regarima.RegArimaKernel;
import java.util.Arrays;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sa.RegArimaDecomposer;
import jdplus.sa.SaVariablesMapping;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author palatej
 */
@lombok.Value
public class X13Kernel {

    private static PreliminaryChecks of(X13Spec spec) {
        BasicSpec basic = spec.getRegArima().getBasic();
        return (s, logs) -> {
            TsData sc = s.select(basic.getSpan());
            if (basic.isPreliminaryCheck()) {
                jdplus.sa.PreliminaryChecks.testSeries(sc);
            }
            return sc;
        };
    }

    private PreliminaryChecks preliminary;
    private RegArimaKernel regarima;
    private SaVariablesMapping samapping;
    private X11Spec spec;

    public static X13Kernel of(X13Spec spec, ModellingContext context) {
        PreliminaryChecks check = of(spec);
        RegArimaKernel regarima = RegArimaKernel.of(spec.getRegArima(), context);
        SaVariablesMapping mapping = new SaVariablesMapping();
        // TO DO: fill maping with existing information in TramoSpec (section Regression)
        return new X13Kernel(check, regarima, mapping, spec.getX11());
    }

    public X13Results process(TsData s, ProcessingLog log) {
        // Step 0. Preliminary checks
        TsData sc = preliminary.check(s, log);
        // Step 1. Preprocessing
        ModelEstimation preprocessing = regarima.process(sc, log);
        // Step 2. Link between regarima and x11
        SaVariablesMapping nmapping = new SaVariablesMapping();
        nmapping.addDefault(Arrays
                .stream(preprocessing.getVariables())
                .map(var -> var.getVariable())
                .toArray(q -> new ITsVariable[q]));
        nmapping.put(samapping);
        RegArimaDecomposer decomposer = RegArimaDecomposer.of(preprocessing, nmapping);
        int nb = spec.getBackcastHorizon();
        if (nb < 0) {
            nb = -nb * s.getAnnualFrequency();
        }
        int nf = spec.getForecastHorizon();
        if (nf < 0) {
            nf = -nf * s.getAnnualFrequency();
        }
        X13Preadjustment.Builder builder = X13Preadjustment.builder();
        TsData alin = initialStep(decomposer, nb, nf, builder);
        X13Preadjustment preadjustment = builder.build();
        // Step 3. X11
        X11Kernel x11 = new X11Kernel();
        X11Spec nspec = updateSpec(spec, preprocessing);
        X11Results xr = x11.process(alin, nspec);
        X13Finals finals = finals(nspec.getMode(), preadjustment, xr);
        return new X13Results(preprocessing, preadjustment, xr, finals);
    }

    private TsData initialStep(RegArimaDecomposer decomposer, int nb, int nf, X13Preadjustment.Builder astep) {
        ModelEstimation model = decomposer.getModel();
        boolean mul = model.isLogTransformation();
        TsData series = model.interpolatedSeries(false);
        int n = series.length();
        TsDomain sdomain = series.getDomain();
        TsDomain domain = sdomain.extend(nb, nf);
        TsPeriod bstart = domain.getStartPeriod(), start = sdomain.getStartPeriod(), fstart = sdomain.getEndPeriod();

        // Gets all regression effects
        TsData mh = model.getMovingHolidayEffect(domain);
        TsData td = model.getTradingDaysEffect(domain);

        TsData pt = decomposer.deterministicEffect(domain, ComponentType.Trend, false, v -> v instanceof IOutlier);
        TsData ps = decomposer.deterministicEffect(domain, ComponentType.Seasonal, false, v -> v instanceof IOutlier);
        TsData pi = decomposer.deterministicEffect(domain, ComponentType.Irregular, false, v -> v instanceof IOutlier);
        TsData ut = decomposer.deterministicEffect(domain, ComponentType.Trend, false, v -> v instanceof IUserTsVariable);
        TsData us = decomposer.deterministicEffect(domain, ComponentType.Seasonal, false, v -> v instanceof IUserTsVariable);
        TsData ui = decomposer.deterministicEffect(domain, ComponentType.Irregular, false, v -> v instanceof IUserTsVariable);
        TsData usa = decomposer.deterministicEffect(domain, ComponentType.SeasonallyAdjusted, false, v -> v instanceof IUserTsVariable);
        TsData user = decomposer.deterministicEffect(domain, ComponentType.Series, false, v -> v instanceof IUserTsVariable);
        TsData uu = decomposer.deterministicEffect(domain, ComponentType.Undefined, false, v -> v instanceof IUserTsVariable);
        TsData p = mul ? TsData.multiply(pt, ps, pi) : TsData.add(pt, ps, pi);

        pt = mul ? TsData.multiply(pt, ut) : TsData.add(pt, ut);
        ps = mul ? TsData.multiply(ps, us) : TsData.add(ps, us);
        pi = mul ? TsData.multiply(pi, ui) : TsData.add(pi, ui);
        TsData pall = mul ? TsData.multiply(pt, ps, pi) : TsData.add(pt, ps, pi);
        TsData u = mul ? TsData.multiply(usa, user) : TsData.add(usa, user);

        // linearized series. detlin are deterministic effects removed before the decomposition,
        // detall are all the deterministic effects
        TsData detlin, detall;
        if (mul) {
            detlin = TsData.multiply(td, mh, p, uu);
            detall = TsData.multiply(detlin, u);
        } else {
            detlin = TsData.add(td, mh, p, uu);
            detall = TsData.add(detlin, u);
        }
        // forecasts, backcasts
        TsData nbcasts = null, nfcasts = null;
        TsData s = model.interpolatedSeries(false);

        if (nb > 0 || nf > 0) {
            DoubleSeq lin = model.linearizedSeries().getValues();
            SarimaModel arima = model.getModel().arima();
            FastArimaForecasts fcasts = new FastArimaForecasts();
            double mean = 0;
            if (model.getModel().isMean()) {
                mean = model.getConcentratedLikelihood().coefficient(0);
            }
            fcasts.prepare(arima, mean);

            if (nb > 0) {
                DoubleSeq tmp = fcasts.backcasts(lin, nb);
                nbcasts = TsData.ofInternal(bstart, tmp);
                if (mul) {
                    nbcasts = TsData.multiply(nbcasts.fastFn(z -> Math.exp(z)), detall);
                } else {
                    nbcasts = TsData.add(nbcasts, detall);
                }
            }
            if (nf > 0) {
                DoubleSeq tmp = fcasts.forecasts(lin, nf);
                nfcasts = TsData.ofInternal(fstart, tmp);
                if (mul) {
                    nfcasts = TsData.multiply(nfcasts.fastFn(z -> Math.exp(z)), detall);
                } else {
                    nfcasts = TsData.add(nfcasts, detall.drop(nb + n, 0));
                }
            }
            s = TsData.concatenate(nbcasts, s, nfcasts);
        }

        astep.a1(s)
                .a6(td)
                .a7(mh)
                .a8(pall)
                .a8t(pt)
                .a8s(ps)
                .a8i(pi)
                .a9(u)
                .a9sa(usa)
                .a9ser(user);

        return (mul ? TsData.divide(s, detlin) : TsData.subtract(s, detlin));
    }

    private X11Spec updateSpec(X11Spec spec, ModelEstimation model) {
        int nb = spec.getBackcastHorizon(), nf = spec.getForecastHorizon();
        int period = model.getAnnualFrequency();
        X11Spec.Builder builder = spec.toBuilder()
                .backcastHorizon(nb < 0 ? -nb * period : nb)
                .forecastHorizon(nf < 0 ? -nf * period : nf);

        if (spec.getMode() != DecompositionMode.PseudoAdditive) {
            boolean mul = model.isLogTransformation();
            builder.mode(mul ? DecompositionMode.Multiplicative : DecompositionMode.Additive);
        }
        return builder.build();
    }

    private final TsData op(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.subtract(l, r);
        } else {
            return TsData.divide(l, r);
        }
    }

    /**
     * Adds/multiplies two time series, following the decomposition mode.
     * (multiplies in the case of multiplicative decomposition)
     *
     * @param l The left operand
     * @param r The right operand
     *
     * @return A new time series is returned
     */
    private final TsData invOp(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.add(l, r);
        } else {
            return TsData.multiply(l, r);
        }
    }

//    private final TsData pseudoOp(TsData y, TsData t, TsData s) {
//        TsData sa = new TsData(y.getDomain());
//        int beg = t.getStart().minus(y.getStart()), end = t.getLength() + beg;
//        for (int i = 0; i < beg; ++i) {
//            double cur = s.get(i);
//            if (cur == 0) {
//                throw new X11Exception("Unexpected 0 in peudo-additive");
//            }
//            sa.set(i, y.get(i) / cur);
//        }
//        for (int i = beg; i < end; ++i) {
//            sa.set(i, y.get(i) - t.get(i - beg) * (s.get(i) - 1));
//        }
//        for (int i = end; i < sa.getLength(); ++i) {
//            double cur = s.get(i);
//            if (cur == 0) {
//                throw new X11Exception("Unexpected 0 in peudo-additive");
//            }
//            sa.set(i, y.get(i) / cur);
//        }
//        return sa;
//    }
    private X13Finals finals(DecompositionMode mode, X13Preadjustment astep, X11Results x11) {
        // add preadjustment
        TsData a1 = astep.getA1();
        TsData a8t = astep.getA8t();
        TsData a8i = astep.getA8i();
        TsData a8s = astep.getA8s();

        TsData d10 = x11.getD10();
        TsData d11 = x11.getD11();
        TsData d12 = x11.getD12();
        TsData d13 = x11.getD13();

        X13Finals.Builder decomp = X13Finals.builder();

        // add ps to d10
        TsData d10c = invOp(mode, d10, a8s);
        decomp.d10final(d10c);

        // add pt to trend
        TsData d12c = invOp(mode, d12, a8t);
        decomp.d12final(d12c);

        // add pi to irregular
        TsData d13c = invOp(mode, d13, a8i);
        decomp.d13final(d13c);

        // add pt, pi to d11
        TsData d11c = invOp(mode, d11, a8t);
        d11c = invOp(mode, d11c, a8i);
        //   d11c = toolkit.getContext().invOp(d11c, a8s);
        TsData a9sa = astep.getA9sa();
        d11c = invOp(mode, d11c, a9sa);
        decomp.d11final(d11c);

        TsData d16;
        if (spec.getMode() == DecompositionMode.PseudoAdditive) {
            TsData tmp = TsData.divide(a1, d12);
            tmp = TsData.subtract(tmp, d13);
            d16 = tmp.add(1).commit();
        } else {
            d16 = op(mode, a1, d11c);
        }
        decomp.d16(d16);
        decomp.d18(op(mode, d16, d10c));

//        int nf = toolkit.getContext().getForecastHorizon();
//        if (nf > 0) {
//            TsData a1a = atables.get(A1a, TsData.class);
//            TsData d16a;
//            if (toolkit.getContext().isPseudoAdditive()) {
//                d16a = a1a.div(d12).minus(d13).plus(1);
//            } else {
//                d16a = toolkit.getContext().op(a1a, d11c);
//            }
//            TsDomain fdomain = new TsDomain(sdomain.getEnd(), nf);
//            dtables.set(D10a, d10c.fittoDomain(fdomain));
//            dtables.set(D10aL, d10.fittoDomain(fdomain));
//            dtables.set(D11a, d11c.fittoDomain(fdomain));
//            dtables.set(D11aL, d11.fittoDomain(fdomain));
//            dtables.set(D12a, d12c.fittoDomain(fdomain));
//            dtables.set(D12aL, d12.fittoDomain(fdomain));
//            dtables.set(D16a, d16a);
//        } else {
//            int freq = toolkit.getContext().getFrequency();
//            TsDomain fdomain = new TsDomain(sdomain.getEnd(), freq);
//            TsData d10a = new TsData(fdomain);
//            for (int i = 0, k = sdomain.getLength() - freq; i < freq; ++i, ++k) {
//                d10a.set(i, (d10.get(k) * 3 - d10.get(k - freq)) / 2);
//            }
//            dtables.set(D10a, d10a);
//            dtables.set(D10aL, d10a);
//            // TsData a8s = atables.get(A8s, TsData.class);
//            TsData a6 = atables.get(A6, TsData.class);
//            TsData a7 = atables.get(A7, TsData.class);
//            TsData d16a = toolkit.getContext().invOp(d10a, a6);
//            d16a = toolkit.getContext().invOp(d16a, a7);
//            d16a = toolkit.getContext().invOp(d16a, a8s);
//            dtables.set(D16a, d16a);
//        }
//
//        int nb = toolkit.getContext().getBackcastHorizon();
//        //backcast is only calculated if there is a backcast horizon
//        if (nb > 0) {
//            TsDomain bdomain = new TsDomain(sdomain.getStart().minus(nb), nb);
//            TsData a1b = atables.get(A1b, TsData.class);
//            TsData d16b = toolkit.getContext().op(a1b, d11c);
//            dtables.set(D16b, d16b);
//            dtables.set(D10b, d10c.fittoDomain(bdomain));
//        }
        return decomp.build();

    }

}
