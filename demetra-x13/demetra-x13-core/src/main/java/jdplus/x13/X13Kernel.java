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
import demetra.processing.ProcessingLog;
import demetra.regarima.BasicSpec;
import demetra.sa.ComponentType;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.regression.ModellingContext;
import jdplus.x11.X11Kernel;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import demetra.x13.X13Finals;
import demetra.x13.X13Preadjustment;
import demetra.x13.X13Spec;
import jdplus.x13.regarima.FastArimaForecasts;
import jdplus.x13.regarima.RegArimaKernel;
import java.util.Arrays;
import jdplus.regarima.ami.ModellingUtility;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sa.modelling.RegArimaDecomposer;
import jdplus.sa.modelling.SaVariablesMapping;
import jdplus.sarima.SarimaModel;
import jdplus.x11.X11Utility;

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
        int nb = spec.getBackcastHorizon();
        if (nb < 0) {
            nb = -nb * s.getAnnualFrequency();
        }
        int nf = spec.getForecastHorizon();
        if (nf < 0) {
            nf = -nf * s.getAnnualFrequency();
        }
        X13Preadjustment.Builder builder = X13Preadjustment.builder();
        TsData alin = initialStep(preprocessing, nb, nf, builder);
        X13Preadjustment preadjustment = builder.build();
        // Step 3. X11
        X11Kernel x11 = new X11Kernel();
        X11Spec nspec = updateSpec(spec, preprocessing);
        X11Results xr = x11.process(alin, nspec);
        X13Finals finals = finals(nspec.getMode(), preadjustment, xr);
        return new X13Results(preprocessing, preadjustment, xr, finals);
    }

    private TsData initialStep(ModelEstimation model, int nb, int nf, X13Preadjustment.Builder astep) {
        boolean mul = model.isLogTransformation();
        TsData series = model.interpolatedSeries(false);
        int n = series.length();
        TsDomain sdomain = series.getDomain();
        TsDomain domain = sdomain.extend(nb, nf);
        TsPeriod bstart = domain.getStartPeriod(), start = sdomain.getStartPeriod(), fstart = sdomain.getEndPeriod();

        // Gets all regression effects
        TsData mh = model.getMovingHolidayEffect(domain);
        TsData td = model.getTradingDaysEffect(domain);

        TsData pt = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, false, v -> ModellingUtility.isOutlier(v));
        TsData ps = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, false, v -> ModellingUtility.isOutlier(v));
        TsData pi = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, false, v -> ModellingUtility.isOutlier(v));
        TsData ut = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Trend, false, v -> ModellingUtility.isUser(v));
        TsData us = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Seasonal, false, v -> ModellingUtility.isUser(v));
        TsData ui = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Irregular, false, v -> ModellingUtility.isUser(v));
        TsData usa = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.SeasonallyAdjusted, false, v -> ModellingUtility.isUser(v));
        TsData user = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Series, false, v -> ModellingUtility.isUser(v));
        TsData uu = RegArimaDecomposer.deterministicEffect(model, domain, ComponentType.Undefined, false, v -> ModellingUtility.isUser(v));
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
        }

        astep.a1(s)
                .a1a(nfcasts)
                .a1b(nbcasts)
                .a6(td)
                .a7(mh)
                .a8(pall)
                .a8t(pt)
                .a8s(ps)
                .a8i(pi)
                .a9(u)
                .a9sa(usa)
                .a9ser(user);

        s = TsData.concatenate(nbcasts, s, nfcasts);
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

    private TsData op(DecompositionMode mode, TsData l, TsData r) {
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
    private TsData invOp(DecompositionMode mode, TsData l, TsData r) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return TsData.add(l, r);
        } else {
            return TsData.multiply(l, r);
        }
    }

    private double mean(DecompositionMode mode) {
        if (mode != DecompositionMode.Multiplicative && mode != DecompositionMode.PseudoAdditive) {
            return 0;
        } else {
            return 1;
        }
    }

    private TsData correct(TsData s, TsData weights, TsData rs) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), rs.getValues());
        return TsData.ofInternal(s.getStart(), sc.commit());
    }

    private TsData correct(TsData s, TsData weights, double mean) {
        DoubleSeq sc = X11Utility.correctSeries(s.getValues(), weights.getValues(), mean);
        return TsData.ofInternal(s.getStart(), sc.commit());
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
        TsData a1a = astep.getA1a();
        TsData a8t = astep.getA8t();
        TsData a8i = astep.getA8i();
        TsData a8s = astep.getA8s();

        TsData d10 = x11.getD10();
        TsData d11 = x11.getD11();
        TsData d12 = x11.getD12();
        TsData d13 = x11.getD13();

        X13Finals.Builder decomp = X13Finals.builder();

        TsDomain fd = a1a == null ? null : a1a.getDomain();
        TsDomain d = a1.getDomain();
        // add ps to d10

        TsData d10c = invOp(mode, d10, a8s);
        if (fd != null) {
            decomp.d10a(TsData.fitToDomain(d10c, fd));
            d10c = TsData.fitToDomain(d10c, d);
        }
        decomp.d10final(d10c);

        // add pt to trend
        TsData d12c = invOp(mode, d12, a8t);
        if (fd != null) {
            decomp.d12a(TsData.fitToDomain(d12c, fd));
            d12c = TsData.fitToDomain(d12c, d);
        }
        decomp.d12final(d12c);

        // add pi to irregular
        TsData d13c = invOp(mode, d13, a8i);
        if (fd != null) {
             d13c = TsData.fitToDomain(d13c, d);
        }
        decomp.d13final(d13c);

        // add pt, pi to d11
        TsData d11c = invOp(mode, d11, a8t);
        d11c = invOp(mode, d11c, a8i);
        //   d11c = toolkit.getContext().invOp(d11c, a8s);
        TsData a9sa = astep.getA9sa();
        d11c = invOp(mode, d11c, a9sa);
        if (fd != null) {
            decomp.d11a(TsData.fitToDomain(d11c, fd));
            d11c = TsData.fitToDomain(d11c, d);
        }
        decomp.d11final(d11c);

        TsData a6=astep.getA6(), a7=astep.getA7();
        TsData d18=invOp(mode, a6, a7);
        
        TsData d16=invOp(mode, d10c, d18);
//        if (spec.getMode() == DecompositionMode.PseudoAdditive) {
//            TsData tmp = TsData.divide(a1, d12);
//            tmp = TsData.subtract(tmp, d13);
//            d16 = tmp.add(1).commit();
//        } else {
//            d16 = op(mode, a1, d11c);
//        }
        if (fd != null) {
            decomp.d16a(TsData.fitToDomain(d16, fd));
            d16 = TsData.fitToDomain(d16, d);
        }
        decomp.d16(d16);
//        TsData d18=op(mode, d16, d10c);
        if (fd != null) {
            decomp.d18a(TsData.fitToDomain(d18, fd));
            d18= TsData.fitToDomain(d18, d);
        }
        decomp.d18(d18);

        // remove pre-specified outliers
        TsData a1c = op(mode, a1, a8i);
        d11c = op(mode, d11c, a8i);

        TsData c17 = TsData.fitToDomain(x11.getC17(), d);

        TsData tmp = op(mode, a1, d13c);
        TsData e1 = correct(a1c, c17, tmp);
        TsData e2 = correct(d11c, c17, d12);
        TsData e3 = correct(TsData.fitToDomain(d13, d), c17, mean(mode));
        TsData e11 = correct(d11c, c17, invOp(mode, d12, op(mode, a1c, e1)));

        decomp.e1(e1);
        decomp.e2(e2);
        decomp.e3(e3);
        decomp.e11(e11);

        return decomp.build();

    }

}
