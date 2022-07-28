/*
 * Copyright 2022 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.benchmarking.io.information;

import demetra.DemetraVersion;
import demetra.data.AggregationType;
import demetra.data.Parameter;
import demetra.information.InformationSet;
import demetra.information.InformationSetSerializer;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.ssf.SsfInitialization;
import demetra.tempdisagg.univariate.TemporalDisaggregationSpec;
import demetra.timeseries.TimeSelector;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregationSpecMapping {

    public final String SPAN = "span", MODEL = "model", PARAMETER = "parameter", AGGTYPE = "aggregation",
            CONSTANT = "constant", TREND = "trend", ZEROINIT = "zeroinit", DIFFUSEREGS = "diffuseregs",
            EPS = "precision", LOG = "log", SSF = "ssfoption", FREQ = "defaultfrequency", ML = "ml", TRUNCATED = "truncatedrho";

    public static final InformationSetSerializer<TemporalDisaggregationSpec> SERIALIZER = new InformationSetSerializer<TemporalDisaggregationSpec>() {
        @Override
        public InformationSet write(TemporalDisaggregationSpec object, boolean verbose) {
            return TemporalDisaggregationSpecMapping.write(object, verbose);
        }

        @Override
        public TemporalDisaggregationSpec read(InformationSet info) {
            return TemporalDisaggregationSpecMapping.read(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public TemporalDisaggregationSpec read(InformationSet info) {
        if (info == null) {
            return TemporalDisaggregationSpec.CHOWLIN;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (!desc.isCompatible(TemporalDisaggregationSpec.DESCRIPTOR)) {
            throw new IllegalArgumentException();
        }
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder();
        TimeSelector sel = info.get(SPAN, TimeSelector.class);
        if (sel != null) {
            builder.estimationSpan(sel);
        }
        String n = info.get(MODEL, String.class);
        if (n != null) {
            builder.residualsModel(TemporalDisaggregationSpec.Model.valueOf(n));
        }
        Integer i = info.get(FREQ, Integer.class);
        if (i != null) {
            builder.defaultPeriod(i);
        }
        Parameter p = info.get(PARAMETER, Parameter.class);
        if (p != null) {
            builder.parameter(p);
        }
        n = info.get(AGGTYPE, String.class);
        if (n != null) {
            builder.aggregationType(AggregationType.valueOf(n));
        }
        Boolean b = info.get(CONSTANT, Boolean.class);
        if (b != null) {
            builder.constant(b);
        }
        b = info.get(TREND, Boolean.class);
        if (b != null) {
            builder.trend(b);
        }
        n = info.get(SSF, String.class);
        if (n != null) {
            builder.algorithm(SsfInitialization.valueOf(n));
        }
        b = info.get(ZEROINIT, Boolean.class);
        if (b != null) {
            builder.zeroInitialization(b);
        }
        b = info.get(LOG, Boolean.class);
        if (b != null) {
            builder.log(b);
        }
        b = info.get(DIFFUSEREGS, Boolean.class);
        if (b != null) {
            builder.diffuseRegressors(b);
        }
        b = info.get(ML, Boolean.class);
        if (b != null) {
            builder.maximumLikelihood(b);
        }
        Double t = info.get(TRUNCATED, Double.class);
        if (t != null) {
            builder.truncatedParameter(t);
        }
        Double e = info.get(EPS, Double.class);
        if (e != null) {
            builder.estimationPrecision(e);
        }
        return builder.build();
    }

    public InformationSet write(TemporalDisaggregationSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ProcSpecification.ALGORITHM, TemporalDisaggregationSpec.DESCRIPTOR);
        TimeSelector span = spec.getEstimationSpan();
        if (span.getType() != TimeSelector.SelectionType.All) {
            info.set(SPAN, span);
        }
        info.set(MODEL, spec.getResidualsModel().name());
        Parameter p = spec.getParameter();
        if (p != null && p.isDefined()) {
            info.set(PARAMETER, p);
        }
        info.set(AGGTYPE, spec.getAggregationType().name());
        info.set(CONSTANT, spec.isConstant());
        info.set(TREND, spec.isTrend());
        info.set(FREQ, spec.getDefaultPeriod());
        info.set(SSF, spec.getAlgorithm().name());
        if (spec.isZeroInitialization() || verbose) {
            info.set(ZEROINIT, spec.isZeroInitialization());
        }
        if (spec.isLog() || verbose) {
            info.set(LOG, spec.isLog());
        }
        if (spec.isDiffuseRegressors() || verbose) {
            info.set(DIFFUSEREGS, spec.isDiffuseRegressors());
        }
        if (!spec.isMaximumLikelihood() || verbose) {
            info.set(ML, spec.isMaximumLikelihood());
        }
        if (spec.getEstimationPrecision() != TemporalDisaggregationSpec.DEF_EPS || verbose) {
            info.set(EPS, spec.getEstimationPrecision());
        }
        if (spec.getTruncatedParameter() != 0 || verbose) {
            info.set(TRUNCATED, spec.getTruncatedParameter());
        }
        return info;
    }

}
