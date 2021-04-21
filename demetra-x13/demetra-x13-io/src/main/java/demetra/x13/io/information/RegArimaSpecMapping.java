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
import demetra.modelling.implementations.SarimaSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.regarima.RegArimaSpec;
import demetra.regarima.RegressionSpec;
import demetra.regarima.SarimaValidator;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaSpecMapping {

    public static final String METHOD = "tramo";
    public static final String FAMILY = "Modelling";
    public static final String VERSION_LEGACY = "0.1.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_LEGACY = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_LEGACY);

    public static final String VERSION_V3 = "3.0.0";
    public static final AlgorithmDescriptor DESCRIPTOR_V3 = new AlgorithmDescriptor(FAMILY, METHOD, VERSION_V3);

    public static final String BASIC = "basic", TRANSFORM = "transform",
            AUTOMDL = "automdl", ARIMA = "arima",
            REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE = "esimate";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        EstimateSpecMapping.fillDictionary(InformationSet.item(prefix, ESTIMATE), dic);
        TransformSpecMapping.fillDictionary(InformationSet.item(prefix, TRANSFORM), dic);
        BasicSpecMapping.fillDictionary(InformationSet.item(prefix, BASIC), dic);
        AutoModelSpecMapping.fillDictionary(InformationSet.item(prefix, AUTOMDL), dic);
        ArimaSpecMapping.fillDictionary(InformationSet.item(prefix, ARIMA), dic);
        OutlierSpecMapping.fillDictionary(InformationSet.item(prefix, OUTLIER), dic);
        RegressionSpecMapping.fillDictionary(InformationSet.item(prefix, REGRESSION), dic);
    }

    public RegArimaSpec read(InformationSet info) {
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

    public InformationSet write(RegArimaSpec spec, boolean verbose) {
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
        InformationSet reginfo = RegressionSpecMapping.write(spec.getRegression(), verbose);
        if (reginfo != null) {
            specInfo.set(REGRESSION, reginfo);
        }
        InformationSet estimateinfo = EstimateSpecMapping.write(spec.getEstimate(), verbose);
        if (estimateinfo != null) {
            specInfo.set(ESTIMATE, estimateinfo);
        }
        return specInfo;
    }

    public RegArimaSpec readLegacy(InformationSet info) {
        RegArimaSpec.Builder builder = RegArimaSpec.builder();
        InformationSet binfo = info.getSubSet(BASIC);
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        InformationSet oinfo = info.getSubSet(OUTLIER);
        InformationSet ainfo = info.getSubSet(ARIMA);
        InformationSet amiinfo = info.getSubSet(AUTOMDL);
        InformationSet einfo = info.getSubSet(ESTIMATE);
        InformationSet rinfo = info.getSubSet(REGRESSION);
        if (binfo != null) {
            builder.basic(BasicSpecMapping.read(tinfo));
        }
        if (tinfo != null) {
            builder.transform(TransformSpecMapping.read(tinfo));
        }
        if (oinfo != null) {
            builder.outliers(OutlierSpecMapping.read(oinfo));
        }
        SarimaSpec.Builder ab = SarimaSpec.builder()
//                .validator(SarimaValidator.VALIDATOR)
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
            RegressionSpecMapping.readLegacy(rinfo, rb);
            builder.regression(rb.build());
        }
        return builder.build();
    }

    public InformationSet writeLegacy(RegArimaSpec spec, boolean verbose) {
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
        InformationSet reginfo = RegressionSpecMapping.writeLegacy(spec.getRegression(), verbose);
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
