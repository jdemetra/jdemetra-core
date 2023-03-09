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

import demetra.DemetraVersion;
import demetra.information.InformationSet;
import demetra.arima.SarimaSpec;
import demetra.information.InformationSetSerializerEx;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionSpec;
import demetra.timeseries.TsDomain;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaSpecMapping {

    public static final InformationSetSerializerEx<RegArimaSpec, TsDomain> SERIALIZER_V3 = new InformationSetSerializerEx<RegArimaSpec, TsDomain>() {
        @Override
        public InformationSet write(RegArimaSpec object, TsDomain context, boolean verbose) {
            return RegArimaSpecMapping.write(object, context, verbose);
        }

        @Override
        public RegArimaSpec read(InformationSet info, TsDomain context) {
            return RegArimaSpecMapping.read(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public static final InformationSetSerializerEx<RegArimaSpec, TsDomain> SERIALIZER_LEGACY = new InformationSetSerializerEx<RegArimaSpec, TsDomain>() {
        @Override
        public InformationSet write(RegArimaSpec object, TsDomain context, boolean verbose) {
            return RegArimaSpecMapping.writeLegacy(object, context, verbose);
        }

        @Override
        public RegArimaSpec read(InformationSet info, TsDomain context) {
            return RegArimaSpecMapping.readLegacy(info, context);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public static final String METHOD = "tramo";
    public static final String FAMILY = "Modelling";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final String BASIC = "basic", TRANSFORM = "transform",
            AUTOMDL = "automdl", ARIMA = "arima",
            REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE = "esimate";

    public RegArimaSpec read(InformationSet info, TsDomain context) {
        if (info == null) {
            return RegArimaSpec.DEFAULT_ENABLED;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (desc != null && desc.equals(RegArimaSpec.DESCRIPTOR_LEGACY)) {
            return readLegacy(info, context);
        } else {
            return readV3(info, context);
        }
    }

    public RegArimaSpec readV3(InformationSet info, TsDomain context) {
        return RegArimaSpec.builder()
                .basic(BasicSpecMapping.read(info.getSubSet(BASIC)))
                .transform(TransformSpecMapping.read(info.getSubSet(TRANSFORM)))
                .arima(ArimaSpecMapping.read(info.getSubSet(ARIMA)))
                .autoModel(AutoModelSpecMapping.read(info.getSubSet(AUTOMDL)))
                .outliers(OutlierSpecMapping.read(info.getSubSet(OUTLIER)))
                .regression(RegressionSpecMapping.read(info.getSubSet(REGRESSION)))
                .estimate(EstimateSpecMapping.read(info.getSubSet(ESTIMATE)))
                .build();
    }

    public InformationSet write(RegArimaSpec spec, TsDomain context, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.set("algorithm", RegArimaSpecMapping.DESCRIPTOR_V3);
        InformationSet binfo = BasicSpecMapping.write(spec.getBasic(), verbose);
        if (binfo != null) {
            specInfo.set(BASIC, binfo);
        }
        InformationSet tinfo = TransformSpecMapping.write(spec.getTransform(), verbose);
        if (tinfo != null) {
            specInfo.set(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = ArimaSpecMapping.write(spec.getArima(), verbose);
        if (arimainfo != null) {
            specInfo.set(ARIMA, arimainfo);
        }
        InformationSet amiinfo = AutoModelSpecMapping.write(spec.getAutoModel(), verbose);
        if (amiinfo != null) {
            specInfo.set(AUTOMDL, amiinfo);
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

    public RegArimaSpec readLegacy(InformationSet info, TsDomain context) {
        RegArimaSpec.Builder builder = RegArimaSpec.builder();
        InformationSet binfo = info.getSubSet(BASIC);
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        InformationSet oinfo = info.getSubSet(OUTLIER);
        InformationSet ainfo = info.getSubSet(ARIMA);
        InformationSet amiinfo = info.getSubSet(AUTOMDL);
        InformationSet einfo = info.getSubSet(ESTIMATE);
        InformationSet rinfo = info.getSubSet(REGRESSION);
        if (binfo != null) {
            builder.basic(BasicSpecMapping.read(binfo));
        }
        if (tinfo != null) {
            builder.transform(TransformSpecMapping.read(tinfo));
        }
        if (oinfo != null) {
            builder.outliers(OutlierSpecMapping.read(oinfo));
        }
        SarimaSpec.Builder ab = SarimaSpec.builder() //                .validator(SarimaValidator.VALIDATOR)
                ;
        RegressionSpec.Builder rb = RegressionSpec.builder();
        if (ainfo != null) {
            ArimaSpecMapping.readLegacy(ainfo, ab, rb);
            builder.arima(ab.build());
        }
        if (amiinfo != null) {
            builder.autoModel(AutoModelSpecMapping.read(amiinfo));
        }
        if (einfo != null) {
            builder.estimate(EstimateSpecMapping.read(einfo));
        }
        if (rinfo != null) {
            RegressionSpecMapping.readLegacy(rinfo, context, rb);
            builder.regression(rb.build());
        }
        return builder.build();
    }

    public InformationSet writeLegacy(RegArimaSpec spec, TsDomain context, boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.set("algorithm", RegArimaSpecMapping.DESCRIPTOR_LEGACY);
        InformationSet binfo = BasicSpecMapping.write(spec.getBasic(), verbose);
        if (binfo != null) {
            specInfo.set(BASIC, binfo);
        }
        InformationSet tinfo = TransformSpecMapping.write(spec.getTransform(), verbose);
        if (tinfo != null) {
            specInfo.set(TRANSFORM, tinfo);
        }
        InformationSet arimainfo = ArimaSpecMapping.writeLegacy(spec.getArima(), spec.getRegression(), verbose);
        if (arimainfo != null) {
            specInfo.set(ARIMA, arimainfo);
        }
        InformationSet amiinfo = AutoModelSpecMapping.write(spec.getAutoModel(), verbose);
        if (amiinfo != null) {
            specInfo.set(AUTOMDL, amiinfo);
        }
        InformationSet outlierinfo = OutlierSpecMapping.write(spec.getOutliers(), verbose);
        if (outlierinfo != null) {
            specInfo.set(OUTLIER, outlierinfo);
        }
        InformationSet reginfo = RegressionSpecMapping.writeLegacy(spec.getRegression(), context, verbose);
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
