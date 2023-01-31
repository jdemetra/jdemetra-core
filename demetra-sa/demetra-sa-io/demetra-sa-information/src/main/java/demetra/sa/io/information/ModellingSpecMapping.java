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
package demetra.sa.io.information;

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.arima.SarimaSpec;
import demetra.information.InformationSetSerializerEx;
import demetra.modelling.regular.ModellingSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.timeseries.TsDomain;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingSpecMapping {

    public static final InformationSetSerializerEx<ModellingSpec, TsDomain> SERIALIZER = new InformationSetSerializerEx<ModellingSpec, TsDomain>() {
        @Override
        public InformationSet write(ModellingSpec object, TsDomain context, boolean verbose) {
            return ModellingSpecMapping.write(object, context, verbose);
        }

        @Override
        public ModellingSpec read(InformationSet info, TsDomain context) {
            return ModellingSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }

    };

    public static final String ENABLED = "enabled", SERIES = "series", TRANSFORM = "transform",
            ARIMA = "arima", REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE = "esimate";

    public ModellingSpec read(InformationSet info, TsDomain context) {
        if (info == null) {
            return ModellingSpec.DEFAULT;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc == null || !desc.equals(ModellingSpec.DESCRIPTOR)) {
            return null;
        }
        Boolean enabled = info.get(ENABLED, Boolean.class);
        return ModellingSpec.builder()
                .enabled(enabled == null || enabled)
                .series(SeriesSpecMapping.read(info.getSubSet(SERIES)))
                .transform(TransformSpecMapping.read(info.getSubSet(TRANSFORM)))
                .arima(ArimaSpecMapping.read(info.getSubSet(ARIMA)))
                .outliers(OutlierSpecMapping.read(info.getSubSet(OUTLIER)))
                .regression(RegressionSpecMapping.read(info.getSubSet(REGRESSION)))
                .estimate(EstimateSpecMapping.read(info.getSubSet(ESTIMATE)))
                .build();
    }

    public InformationSet write(ModellingSpec spec, TsDomain context, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.set(ProcSpecification.ALGORITHM, ModellingSpec.DESCRIPTOR);
        InformationSet tinfo = TransformSpecMapping.write(spec.getTransform(), verbose);
        if (tinfo != null) {
            specInfo.set(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = ArimaSpecMapping.write(spec.getArima(), verbose);
        if (arimainfo != null) {
            specInfo.set(ARIMA, arimainfo);
        }
        InformationSet outlierinfo = OutlierSpecMapping.write(spec.getOutliers(), verbose);
        if (outlierinfo != null) {
            specInfo.set(OUTLIER, outlierinfo);
        }
        InformationSet reginfo = RegressionSpecMapping.write(spec.getRegression(), context, verbose);
        if (reginfo != null) {
            specInfo.set(REGRESSION, reginfo);
        }
        InformationSet estimateinfo = EstimateSpecMapping.write(spec.getEstimate(), verbose);
        if (estimateinfo != null) {
            specInfo.set(ESTIMATE, estimateinfo);
        }
        return specInfo;
    }

}
