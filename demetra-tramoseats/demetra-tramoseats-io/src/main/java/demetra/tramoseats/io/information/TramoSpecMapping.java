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
package demetra.tramoseats.io.information;

import demetra.information.InformationSet;
import demetra.modelling.implementations.SarimaSpec;
import demetra.tramo.RegressionSpec;
import demetra.tramo.TramoSpec;
import java.util.Map;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSpecMapping {

    public static final String TRANSFORM = "transform",
            AUTOMDL = "automdl", ARIMA = "arima",
            REGRESSION = "regression", OUTLIER = "outlier", ESTIMATE = "esimate";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        EstimateSpecMapping.fillDictionary(InformationSet.item(prefix, ESTIMATE), dic);
        TransformSpecMapping.fillDictionary(InformationSet.item(prefix, TRANSFORM), dic);
        AutoModelSpecMapping.fillDictionary(InformationSet.item(prefix, AUTOMDL), dic);
        ArimaSpecMapping.fillDictionary(InformationSet.item(prefix, ARIMA), dic);
        OutlierSpecMapping.fillDictionary(InformationSet.item(prefix, OUTLIER), dic);
        RegressionSpecMapping.fillDictionary(InformationSet.item(prefix, REGRESSION), dic);
    }

    public TramoSpec read(InformationSet info) {
        TramoSpec.Builder builder = TramoSpec.builder();
        InformationSet tinfo = info.getSubSet(TRANSFORM);
        InformationSet oinfo = info.getSubSet(OUTLIER);
        InformationSet ainfo = info.getSubSet(ARIMA);
        InformationSet amiinfo = info.getSubSet(AUTOMDL);
        InformationSet einfo = info.getSubSet(ESTIMATE);
        InformationSet rinfo = info.getSubSet(REGRESSION);
        if (tinfo != null) {
            builder.transform(TransformSpecMapping.read(tinfo));
        }
        if (oinfo != null) {
            builder.outliers(OutlierSpecMapping.read(oinfo));
        }
        SarimaSpec.Builder ab = SarimaSpec.builder();
        RegressionSpec.Builder rb = RegressionSpec.builder();
        if (ainfo != null) {
            ArimaSpecMapping.read(ainfo, ab, rb);
            builder.arima(ab.build());
        }
        if (amiinfo != null) {
            builder.autoModel(AutoModelSpecMapping.read(amiinfo));
        }
        if (einfo != null) {
            builder.estimate(EstimateSpecMapping.read(einfo));
        }
        if (rinfo != null) {
            RegressionSpecMapping.read(rinfo, rb);
            builder.regression(rb.build());
        }
        return builder.build();
    }
}
