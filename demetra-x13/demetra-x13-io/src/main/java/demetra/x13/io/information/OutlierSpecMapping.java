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
package demetra.x13.io.information;

import demetra.information.InformationSet;
import demetra.regarima.OutlierSpec;
import demetra.regarima.SingleOutlierSpec;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.TransitoryChange;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
class OutlierSpecMapping {

    final String SPAN = "span",
            AO = "ao", LS = "ls", TC = "tc", SO = "so",
            IO = "io", SLS = "sls", WO = "wo", TLS = "tls",
            DEFCV = "defcv",
            METHOD = "method",
            LSRUN = "lsrun",
            TCRATE = "tcrate",
            MAXITER = "maxiter";

    void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SPAN), TimeSelector.class);
        dic.put(InformationSet.item(prefix, AO), Boolean.class);
        dic.put(InformationSet.item(prefix, LS), Boolean.class);
        dic.put(InformationSet.item(prefix, TC), Boolean.class);
        dic.put(InformationSet.item(prefix, SO), Boolean.class);
        dic.put(InformationSet.item(prefix, IO), Boolean.class);
        dic.put(InformationSet.item(prefix, SLS), Boolean.class);
        dic.put(InformationSet.item(prefix, WO), Boolean.class);
        dic.put(InformationSet.item(prefix, TLS), Boolean.class);
        dic.put(InformationSet.item(prefix, DEFCV), Double.class);
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, LSRUN), Integer.class);
        dic.put(InformationSet.item(prefix, TCRATE), Double.class);
        dic.put(InformationSet.item(prefix, MAXITER), Integer.class);
    }

    InformationSet write(OutlierSpec spec, boolean verbose) {
        if (!verbose && !spec.isUsed()) {
            return null;
        }
        InformationSet info = new InformationSet();
        TimeSelector span = spec.getSpan();
        if (verbose || span.getType() != TimeSelector.SelectionType.All) {
            info.add(SPAN, span);
        }
        Optional<SingleOutlierSpec> first = spec.getTypes().stream().filter(o -> o.getType().equals(AdditiveOutlier.CODE)).findFirst();
        if (first.isPresent()) {
            info.add(AO, first.get().getCriticalValue());
        }
        first = spec.getTypes().stream().filter(o -> o.getType().equals(LevelShift.CODE)).findFirst();
        if (first.isPresent()) {
            info.add(LS, first.get().getCriticalValue());
        }
        first = spec.getTypes().stream().filter(o -> o.getType().equals(TransitoryChange.CODE)).findFirst();
        if (first.isPresent()) {
            info.add(TC, first.get().getCriticalValue());
        }
        first = spec.getTypes().stream().filter(o -> o.getType().equals(PeriodicOutlier.CODE)).findFirst();
        if (first.isPresent()) {
            info.add(SO, first.get().getCriticalValue());
        }
        if (verbose || spec.getDefaultCriticalValue() != 0) {
            info.add(DEFCV, spec.getDefaultCriticalValue());
        }
        if (verbose || spec.getMethod() != OutlierSpec.Method.AddOne) {
            info.add(METHOD, spec.getMethod().name());
        }
        if (verbose || spec.getLsRun() != 0) {
            info.add(LSRUN, spec.getLsRun());
        }
        if (verbose || spec.getMonthlyTCRate() != OutlierSpec.DEF_TCRATE) {
            info.add(TCRATE, spec.getMonthlyTCRate());
        }
        if (verbose || spec.getMaxIter() != OutlierSpec.DEF_NMAX) {
            info.add(MAXITER, spec.getMaxIter());
        }
        return info;
    }

    OutlierSpec read(InformationSet info) {
        if (info == null) {
            return OutlierSpec.DEFAULT_UNUSED;
        }

        OutlierSpec.Builder builder = OutlierSpec.builder();

        TimeSelector span = info.get(SPAN, TimeSelector.class);
        if (span != null) {
            builder = builder.span(span);
        }
        Double ao = info.get(AO, Double.class);
        if (ao != null) {
            builder.type(new SingleOutlierSpec(AdditiveOutlier.CODE, ao));
        }
        Double ls = info.get(LS, Double.class);
        if (ls != null) {
            builder.type(new SingleOutlierSpec(LevelShift.CODE, ls));
        }
        Double tc = info.get(TC, Double.class);
        if (tc != null) {
            builder.type(new SingleOutlierSpec(TransitoryChange.CODE, tc));
        }
        Double so = info.get(SO, Double.class);
        if (so != null) {
            builder.type(new SingleOutlierSpec(PeriodicOutlier.CODE, so));
        }
        Double defcv = info.get(DEFCV, Double.class);
        if (defcv != null) {
            builder.defaultCriticalValue(defcv);
        }
        Double tcr = info.get(TCRATE, Double.class);
        if (tcr != null) {
            builder.monthlyTCRate(tcr);
        }
        String method = info.get(METHOD, String.class);
        if (method != null) {
            builder.method(OutlierSpec.Method.valueOf(method));
        }
        Integer lsrun = info.get(LSRUN, Integer.class);
        if (lsrun != null) {
            builder.lsRun(lsrun);
        }
        Integer nmax = info.get(MAXITER, Integer.class);
        if (nmax != null) {
            builder.maxIter(nmax);
        }
        return builder.build();
    }

}
