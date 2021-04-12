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
package demetra.regarima.io.protobuf;

import demetra.data.DoubleSeq;
import demetra.data.Iterables;
import demetra.data.Parameter;
import demetra.data.Range;
import demetra.modelling.StationaryTransformation;
import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaProtosUtility {

    public SarimaSpec convert(RegArimaProtos.SarimaSpec spec) {
        return SarimaSpec.builder()
                .period(spec.getPeriod())
                .d(spec.getD())
                .bd(spec.getBd())
                .phi(ToolkitProtosUtility.convert(spec.getPhiList()))
                .theta(ToolkitProtosUtility.convert(spec.getThetaList()))
                .bphi(ToolkitProtosUtility.convert(spec.getBphiList()))
                .btheta(ToolkitProtosUtility.convert(spec.getBthetaList()))
                .build();
    }

    public RegArimaProtos.SarimaSpec convert(SarimaSpec spec) {
        RegArimaProtos.SarimaSpec.Builder builder = RegArimaProtos.SarimaSpec.newBuilder()
                .setPeriod(spec.getPeriod())
                .setD(spec.getD())
                .setBd(spec.getBd());

        Parameter[] p = spec.getPhi();
        for (int i = 0; i < p.length; ++i) {
            builder.addPhi(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getTheta();
        for (int i = 0; i < p.length; ++i) {
            builder.addTheta(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getBphi();
        for (int i = 0; i < p.length; ++i) {
            builder.addBphi(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getBtheta();
        for (int i = 0; i < p.length; ++i) {
            builder.addBtheta(ToolkitProtosUtility.convert(p[i]));
        }
        return builder.build();
    }

}
